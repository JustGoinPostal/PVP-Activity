# Architecture

PVP Activity has two intentionally small components.

## RuneLite client

The RuneLite plugin reads only the installing player's own state. When the user enables the service, a short-lived random session ID is paired with the local world, Wilderness flag, and broad combat bracket. The plugin never needs the player's RuneScape name or exact coordinates.

A 10-second heartbeat keeps the session current. Logging out, disabling the service, or shutting down RuneLite attempts an immediate session deletion; the backend TTL is the fallback for crashes or lost connectivity.

The sidebar periodically retrieves aggregate world activity and renders totals and bracket counts.

## Aggregation API

The Node.js API stores session state only in memory. It accepts heartbeats, expires stale sessions, aggregates only sessions currently marked as being in the Wilderness, and returns per-world totals.

The API has no account system and no persistent player database in V1. IP addresses are visible at the network layer and are used transiently for in-memory request rate limiting, but are not returned in API responses or used as player identifiers.

## Data flow

```text
RuneLite client
  -> POST /v1/heartbeat
       anonymous session ID
       world
       Wilderness flag
       broad combat bracket

Aggregation API
  -> filters expired/out-of-Wilderness sessions
  -> groups by world and combat bracket

RuneLite client
  <- GET /v1/activity
       aggregate counts only
```

## Review-friendly boundaries

The V1 deliberately excludes usernames, exact locations, nearby-player scanning, equipment, prayers, and automated game input. If RuneLite review requires a narrower feature set, combat brackets or other aggregate fields can be removed without redesigning the transport or session lifecycle.
