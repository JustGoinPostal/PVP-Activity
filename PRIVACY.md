# Privacy and Data Handling

PVP Activity is designed as an opt-in service. Sharing is disabled by default.

## Data sent by the RuneLite plugin

When sharing is enabled, the plugin sends only:

- a random session ID generated for the current RuneLite run
- current RuneScape world
- whether the local player is currently in the Wilderness
- the local player's broad combat-level bracket

The current combat brackets are:

- 3-50
- 51-70
- 71-90
- 91-110
- 111-126

## Data not sent by the plugin

The plugin does not transmit:

- RuneScape username
- exact coordinates
- Wilderness level or sub-location
- equipment
- inventory
- prayer state
- hitpoints
- skull status
- current opponent
- nearby-player information
- clan information
- information about non-participating players

## Server-side handling

The included backend keeps active sessions in memory only. Sessions expire automatically after the configured TTL, which defaults to 45 seconds without a heartbeat.

The backend uses the connecting network address temporarily for in-memory rate limiting. It does not use IP addresses as player identifiers and the included implementation does not persist IP addresses to a database.

Because the plugin communicates with a third-party server when sharing is enabled, the server hosting the API necessarily receives network connection metadata such as the connecting IP address at the transport layer.

## Public activity response

The public activity endpoint returns only aggregate counts by RuneScape world and combat-level bracket for opted-in sessions currently reporting that they are in the Wilderness.

No session IDs, usernames, IP addresses, or exact player locations are returned by the public activity endpoint.

## Disabling sharing

Turning sharing off causes the plugin to request immediate deletion of its current anonymous session. Logging out or closing RuneLite also attempts to remove the session. The server TTL is the final cleanup mechanism if an immediate removal request cannot be delivered.
