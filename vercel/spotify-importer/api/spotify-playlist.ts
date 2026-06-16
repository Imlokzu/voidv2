import chromium from "@sparticuz/chromium";
import puppeteer from "puppeteer-core";

type Track = { title: string; artist: string };

type JsonResponse = {
  title: string;
  thumbnail: string | null;
  tracks: Track[];
  source: "spotify-web-api" | "dom-scroll";
};

const PLAYLIST_QUERY_HASH = "a65e12194ed5fc443a1cdebed5fabe33ca5b07b987185d63c72483867ad13cb4";
const SPOTIFY_CLIENT_ID = "d8a5ed958d274c2e8ee717e6a4b0971d";
const SPOTIFY_APP_VERSION = "1.2.92.73.g916d0757";

function badRequest(message: string) {
  return new Response(JSON.stringify({ error: message }), {
    status: 400,
    headers: { "content-type": "application/json; charset=utf-8" },
  });
}

function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { "content-type": "application/json; charset=utf-8" },
  });
}

function extractPlaylistId(url: string): string | null {
  try {
    const parsed = new URL(url);
    if (!/open\.spotify\.com$/i.test(parsed.hostname)) return null;
    const parts = parsed.pathname.split("/").filter(Boolean);
    const playlistIndex = parts.findIndex((part) => part === "playlist");
    if (playlistIndex === -1) return null;
    return parts[playlistIndex + 1] ?? null;
  } catch {
    return null;
  }
}

export const config = {
  runtime: "nodejs",
};

export default async function handler(request: Request): Promise<Response> {
  const url = new URL(request.url);
  const playlistUrl = url.searchParams.get("url")?.trim();
  if (!playlistUrl) return badRequest("Missing url query parameter");

  const playlistId = extractPlaylistId(playlistUrl);
  if (!playlistId) return badRequest("Invalid Spotify playlist URL");

  const browser = await puppeteer.launch({
    args: chromium.args,
    defaultViewport: { width: 1440, height: 2200, deviceScaleFactor: 1 },
    executablePath: await chromium.executablePath(),
    headless: true,
  });

  try {
    const page = await browser.newPage();
    page.setDefaultNavigationTimeout(60_000);
    await page.setUserAgent(
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"
    );

    await page.goto(`https://open.spotify.com/playlist/${playlistId}`, {
      waitUntil: "domcontentloaded",
    });

    await page.waitForFunction(() => document.readyState === "complete", {
      timeout: 20_000,
    }).catch(() => null);

    const metadata = await page.evaluate(() => {
      const title =
        document.querySelector('meta[property="og:title"]')?.getAttribute("content")?.trim() ||
        document.title.replace(/\s+\|\s+Spotify.*$/i, "").trim() ||
        "Spotify Playlist";
      const thumbnail =
        document.querySelector('meta[property="og:image"]')?.getAttribute("content")?.trim() || null;
      return { title, thumbnail };
    });

    const apiTracks = await page.evaluate(
      async ({ playlistId, queryHash, clientId, appVersion }) => {
        const now = Date.now().toString();

        const tokenResponse = await fetch(
          `https://open.spotify.com/api/token?reason=init&productType=web-player&totp=${now}&totpServer=${now}&totpVer=61`,
          {
            credentials: "include",
            headers: {
              accept: "application/json",
            },
          }
        );

        if (!tokenResponse.ok) {
          throw new Error(`Spotify access token request failed: ${tokenResponse.status}`);
        }

        const tokenJson = await tokenResponse.json();
        const accessToken =
          tokenJson?.accessToken ||
          tokenJson?.access_token ||
          tokenJson?.access_token?.accessToken ||
          null;

        if (!accessToken) {
          throw new Error("Spotify access token missing");
        }

        const clientTokenResponse = await fetch("https://clienttoken.spotify.com/v1/clienttoken", {
          method: "POST",
          headers: {
            "content-type": "application/json;charset=UTF-8",
          },
          body: JSON.stringify({
            client_data: {
              client_version: appVersion,
              client_id: clientId,
              js_sdk_data: {
                device_brand: "Google",
                device_model: "Vercel",
                os: "linux",
                os_version: "serverless",
                device_id: crypto.randomUUID(),
                device_type: "computer",
              },
            },
          }),
        });

        if (!clientTokenResponse.ok) {
          throw new Error(`Spotify client token request failed: ${clientTokenResponse.status}`);
        }

        const clientTokenJson = await clientTokenResponse.json();
        const clientToken =
          clientTokenJson?.granted_token?.token ||
          clientTokenJson?.token ||
          null;

        if (!clientToken) {
          throw new Error("Spotify client token missing");
        }

        const normalizeTracks = (payload: unknown) => {
          const results: Array<{ title: string; artist: string }> = [];
          const seen = new Set<string>();

          const visit = (node: any) => {
            if (!node || typeof node !== "object") return;

            const data = node?.itemV2?.data ?? node?.data ?? node;
            const title = typeof data?.name === "string" ? data.name.trim() : "";
            const artistNames =
              data?.artists?.items
                ?.map((item: any) => item?.profile?.name?.trim?.())
                ?.filter((value: unknown) => typeof value === "string" && value.length > 0) ??
              data?.firstArtist?.items
                ?.map((item: any) => item?.profile?.name?.trim?.())
                ?.filter((value: unknown) => typeof value === "string" && value.length > 0) ??
              [];

            if (title && artistNames.length) {
              const artist = artistNames.join(", ");
              const key = `${title}__${artist}`;
              if (!seen.has(key)) {
                seen.add(key);
                results.push({ title, artist });
              }
            }

            if (Array.isArray(node)) {
              node.forEach(visit);
              return;
            }

            Object.values(node).forEach(visit);
          };

          visit(payload);
          return results;
        };

        const tracks: Array<{ title: string; artist: string }> = [];
        const seen = new Set<string>();

        for (let offset = 0; offset < 5000; offset += 100) {
          const response = await fetch("https://api-partner.spotify.com/pathfinder/v2/query", {
            method: "POST",
            headers: {
              authorization: `Bearer ${accessToken}`,
              "client-token": clientToken,
              "app-platform": "WebPlayer",
              "spotify-app-version": appVersion,
              "content-type": "application/json;charset=UTF-8",
            },
            body: JSON.stringify({
              operationName: "fetchPlaylistContents",
              variables: {
                uri: `spotify:playlist:${playlistId}`,
                offset,
                limit: 100,
                includeEpisodeContentRatingsV2: false,
              },
              extensions: {
                persistedQuery: {
                  version: 1,
                  sha256Hash: queryHash,
                },
              },
            }),
          });

          if (!response.ok) {
            throw new Error(`Spotify playlist query failed at offset ${offset}: ${response.status}`);
          }

          const payload = await response.json();
          const batch = normalizeTracks(payload);

          if (!batch.length) break;

          let added = 0;
          for (const track of batch) {
            const key = `${track.title}__${track.artist}`;
            if (!seen.has(key)) {
              seen.add(key);
              tracks.push(track);
              added += 1;
            }
          }

          if (added === 0 || batch.length < 100) break;
        }

        return tracks;
      },
      {
        playlistId,
        queryHash: PLAYLIST_QUERY_HASH,
        clientId: SPOTIFY_CLIENT_ID,
        appVersion: SPOTIFY_APP_VERSION,
      }
    ).catch(() => [] as Track[]);

    if (apiTracks.length > 30) {
      const result: JsonResponse = {
        title: metadata.title,
        thumbnail: metadata.thumbnail,
        tracks: apiTracks,
        source: "spotify-web-api",
      };
      return json(result);
    }

    const domTracks = await page.evaluate(async () => {
      const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));
      const tracks = new Map<string, { title: string; artist: string }>();

      const scrapeRows = () => {
        const rowSelectors = [
          '[data-testid="tracklist-row"]',
          'div[role="row"]',
        ];

        for (const selector of rowSelectors) {
          const rows = Array.from(document.querySelectorAll(selector));
          for (const row of rows) {
            const anchors = Array.from(row.querySelectorAll("a"));
            const textParts = anchors
              .map((node) => node.textContent?.trim())
              .filter((value): value is string => Boolean(value));

            if (textParts.length >= 2) {
              const title = textParts[0];
              const artist = textParts.slice(1).join(", ");
              tracks.set(`${title}__${artist}`, { title, artist });
            }
          }
        }
      };

      let stableRounds = 0;
      let lastCount = 0;
      for (let i = 0; i < 40; i += 1) {
        scrapeRows();
        window.scrollBy(0, window.innerHeight * 0.9);
        await sleep(650);
        scrapeRows();

        if (tracks.size == lastCount) {
          stableRounds += 1;
        } else {
          stableRounds = 0;
          lastCount = tracks.size;
        }

        if (stableRounds >= 4) break;
      }

      return Array.from(tracks.values());
    });

    if (!domTracks.length) {
      return json(
        {
          error: "Spotify importer could not resolve playlist tracks",
          title: metadata.title,
          thumbnail: metadata.thumbnail,
        },
        502
      );
    }

    const result: JsonResponse = {
      title: metadata.title,
      thumbnail: metadata.thumbnail,
      tracks: domTracks,
      source: "dom-scroll",
    };
    return json(result);
  } finally {
    await browser.close();
  }
}
