# PVP Activity Beta Installation

PVP Activity is currently distributed as a **sideloaded RuneLite beta plugin**. It has not been approved or listed on the RuneLite Plugin Hub.

## Important

- The plugin does **not** scan or report other players around you.
- Sharing is **OFF by default**.
- When you opt in, the plugin sends an anonymous session ID, your current world, whether you are in the Wilderness, and your combat-level bracket to the PVP Activity server.
- Your IP address is visible to the server as part of the network connection.
- The hosted service uses HTTPS.
- The service endpoint is managed internally by the plugin and is not configurable by users.
- Sideloading the plugin does not mean RuneLite or Jagex has approved the plugin or its features.

## RuneLite sideload requirement

Current RuneLite only loads JARs from the sideload directory when the client itself is running in developer mode.

Put the beta JAR in:

### Windows

```text
%USERPROFILE%\.runelite\sideloaded-plugins\
```

### macOS / Linux

```text
~/.runelite/sideloaded-plugins/
```

Create the folder if it does not already exist.

The normal RuneLite launcher does not provide the same sideload behavior as a direct development launch. The reliable supported-development approach is to run the RuneLite client directly/from a RuneLite development build with:

- JVM assertions enabled: `-ea`
- client argument: `--developer-mode`
- optional client argument: `--debug`

Once RuneLite is running in developer mode and the JAR is in the sideload directory, search the plugin list for **PVP Activity** and enable it.

## Using PVP Activity

1. Log into Old School RuneScape.
2. Open PVP Activity settings.
3. Turn on **Enable service & share activity** if you want to participate.
4. Open the PVP Activity sidebar panel.
5. While you have an active sharing session, the panel shows your world, combat level, Wilderness status, sharing status, server status, and aggregated opted-in Wilderness activity by world/combat bracket.

Activity access is reciprocal: if you are not actively sharing, the service does not return the aggregate activity feed to your client.

## Privacy behavior

The client does not intentionally send your RuneScape username, exact coordinates, gear, prayer state, or information about non-participating players.

Backend sessions expire automatically if heartbeats stop.

## Updating

GitHub code changes do not automatically update the hosted backend or an installed JAR. Beta builds and backend deployments are versioned and promoted manually.
