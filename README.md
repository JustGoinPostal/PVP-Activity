# PVP Activity

Opt-in RuneLite PvP activity finder. Participating users anonymously publish their current world, Wilderness status, and combat-level bracket to a small aggregation service. The public panel shows aggregate activity only; RuneScape usernames and exact coordinates are not submitted.

## Status

Initial development build. This project is intended to be submitted to RuneLite for review before distribution.

## Privacy

Sharing is disabled by default. When enabled, the plugin submits an anonymous session ID, world, Wilderness status, and combat-level bracket to the configured third-party API. The API does not require RuneScape usernames or exact player coordinates.

## Layout

- RuneLite plugin: `src/main/java/com/pvpactivity`
- Development launcher: `src/test/java/com/pvpactivity/PvpActivityPluginTest.java`
- Aggregation backend: `server/server.js`

## Development

Requires JDK 11. Run the development client with `./gradlew run` after the Gradle wrapper files are present.

The backend can be started with Node.js using `node server/server.js`. It defaults to port `8080`.
