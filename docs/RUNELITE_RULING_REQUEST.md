# RuneLite Ruling Request: PVP Activity

I am developing a RuneLite plugin called **PVP Activity** and would like a ruling on whether the concept is acceptable before submitting it to the Plugin Hub.

## Purpose

The plugin is intended to help PKers find other willing, participating PKers without repeatedly hopping worlds looking for activity.

## How it works

The plugin is completely opt-in. When a player enables sharing, their client sends the following information to a third-party aggregation server:

- an anonymous temporary session ID
- current RuneScape world
- whether the local player is currently in the Wilderness
- the local player's combat-level bracket

Current brackets:

- 3-50
- 51-70
- 71-90
- 91-110
- 111-126

Other users see only aggregate information such as:

```text
World 324 - 12 participating PKers

Lv 3-50      1
Lv 51-70     3
Lv 71-90     5
Lv 91-110    2
Lv 111-126   1
```

The plugin automatically stops counting a player when they leave the Wilderness, disable sharing, log out, or their session expires.

## Information not collected or transmitted

The plugin does not transmit or display:

- RuneScape usernames
- exact coordinates
- Wilderness level or sub-location
- equipment
- inventory
- prayer usage
- hitpoints
- skull status
- current opponent
- nearby players
- nearby player combat levels
- clan information
- information about players who do not have sharing enabled

The plugin only reports information about the local user who explicitly opted into sharing. It does not scan or report other players.

## Intended use

The intention is closer to voluntary PvP matchmaking than target scouting. A user could know that several opted-in players in a combat bracket are somewhere in the Wilderness on a particular world, but would not know who those players are or where in the Wilderness they are located.

## Third-party server

Sharing is disabled by default, and the configuration explains that enabling it sends data to a third-party server. The included backend uses short-lived anonymous sessions and automatically expires inactive sessions.

See `PRIVACY.md` for the exact data-handling description.

## Requested ruling

I understand RuneLite has restrictions involving PvP scouting, player-group summaries, level-based PvP information, and crowdsourcing player information.

Because this plugin only reports information voluntarily submitted by the player themselves and does not observe or report other players, I would like clarification on whether this use case is acceptable.

**Would an opt-in system where participating PKers voluntarily advertise only their world, Wilderness presence, and combat-level bracket be permitted on the Plugin Hub?**

If the concept is acceptable but one specific part is not, such as combat brackets, I am happy to modify the implementation based on maintainer guidance.

Repository: https://github.com/JustGoinPostal/PVP-Activity
