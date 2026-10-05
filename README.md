# PVP Activity

PVP Activity is an opt-in RuneLite plugin concept for helping PKers find active Wilderness worlds without publishing RuneScape usernames or exact coordinates.

Participating clients report only their own anonymous session state to a small aggregation API. The sidebar shows aggregate counts by world and broad combat-level bracket.

## V1 behavior

When the service is enabled, the plugin automatically reads the local player's:

- current world
- Wilderness status
- combat level, converted to a broad bracket

The plugin sends:

- a random session ID generated for the current RuneLite run
- world
- whether the local player is in the Wilderness
- combat bracket (`3-50`, `51-70`, `71-90`, `91-110`, `111-126`)

It does **not** send:

- RuneScape username
- exact coordinates
- equipment
- prayer state
- nearby-player information
- information about non-participating players

Only sessions currently reporting `inWilderness=true` are included in public activity totals.

## Opt-in and privacy

The third-party service is disabled by default. While disabled, the plugin does not contact the PVP Activity API.

When enabled, the user's IP address is necessarily visible to the server while making HTTP requests. The application does not use IP addresses as player identifiers or include them in the activity API response. The included backend keeps only short-lived rate-limit state in memory and does not persist IP addresses to a database.

Sessions expire automatically after 45 seconds without a heartbeat. Closing RuneLite, logging out, or disabling sharing also attempts to remove the session immediately.

See [PRIVACY.md](PRIVACY.md) for the full data-handling description.

## RuneLite review

This implementation is intentionally being presented transparently for a RuneLite ruling before a formal Plugin Hub submission. The proposed ruling request, including the exact behavior and exclusions, is available at [docs/RUNELITE_RULING_REQUEST.md](docs/RUNELITE_RULING_REQUEST.md).

## Project layout

```text
.
├── build.gradle
├── runelite-plugin.properties
├── PRIVACY.md
├── LICENSE
├── docs/
│   ├── architecture.md
│   └── RUNELITE_RULING_REQUEST.md
├── src/
│   ├── main/java/com/pvpactivity/
│   │   ├── PvpActivityPlugin.java
│   │   ├── PvpActivityConfig.java
│   │   ├── PvpActivityPanel.java
│   │   ├── PvpActivityApiClient.java
│   │   ├── WildernessService.java
│   │   └── model/
│   └── test/java/com/pvpactivity/PvpActivityPluginTest.java
├── server/
│   ├── server.js
│   ├── package.json
│   └── Dockerfile
└── .github/workflows/ci.yml
```

## RuneLite development

Requirements:

- JDK 11
- Gradle 8.x (CI currently uses Gradle 8.10.2)

Start the development RuneLite client:

```bash
gradle run
```

The Gradle project follows the current `runelite/example-plugin` structure and loads `PvpActivityPlugin` through `ExternalPluginManager` in developer mode.

## Run the backend locally

Requires Node.js 18 or newer. There are no third-party npm dependencies.

```bash
node server/server.js
```

The API listens on `http://127.0.0.1:8080` by default. The plugin's default API URL points there for local development.

Environment variables:

- `PORT` — API port, default `8080`
- `SESSION_TTL_MS` — heartbeat expiry, default `45000`

### Docker

```bash
docker build -t pvp-activity-api ./server
docker run --rm -p 8080:8080 pvp-activity-api
```

## API

### `POST /v1/heartbeat`

Example:

```json
{
  "sessionId": "11111111-1111-1111-1111-111111111111",
  "world": 324,
  "inWilderness": true,
  "combatBracket": "71-90",
  "timestamp": 0
}
```

### `GET /v1/activity`

Example response:

```json
{
  "worlds": [
    {
      "world": 324,
      "total": 3,
      "brackets": {
        "51-70": 1,
        "71-90": 2
      }
    }
  ]
}
```

### `DELETE /v1/session/{sessionId}`

Removes the anonymous session immediately.

### `GET /health`

Returns API health and current short-lived session count.

## Review status

This repository is an initial implementation intended to be submitted for RuneLite/Jagex review before public Plugin Hub distribution. If maintainers require changes to the PvP aggregation design, the implementation can be narrowed without changing the basic client/server structure.
