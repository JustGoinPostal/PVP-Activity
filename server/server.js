'use strict';

const http = require('http');
const { URL } = require('url');

const PORT = Number(process.env.PORT || 8080);
const SESSION_TTL_MS = Number(process.env.SESSION_TTL_MS || 45000);
const MAX_BODY_BYTES = 16 * 1024;
const RATE_LIMIT_WINDOW_MS = 60_000;
const RATE_LIMIT_MAX = 180;

const VALID_BRACKETS = new Set(['3-50', '51-70', '71-90', '91-110', '111-126', 'unknown']);
const sessions = new Map();
const rateLimits = new Map();

function json(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(payload),
    'Cache-Control': 'no-store'
  });
  res.end(payload);
}

function clientIp(req) {
  return req.socket && req.socket.remoteAddress ? req.socket.remoteAddress : 'unknown';
}

function allowed(req) {
  const ip = clientIp(req);
  const now = Date.now();
  let entry = rateLimits.get(ip);
  if (!entry || now - entry.startedAt >= RATE_LIMIT_WINDOW_MS) {
    entry = { startedAt: now, count: 0 };
    rateLimits.set(ip, entry);
  }
  entry.count += 1;
  return entry.count <= RATE_LIMIT_MAX;
}

function readJson(req) {
  return new Promise((resolve, reject) => {
    let total = 0;
    const chunks = [];
    req.on('data', chunk => {
      total += chunk.length;
      if (total > MAX_BODY_BYTES) {
        reject(new Error('body too large'));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on('end', () => {
      try {
        const text = Buffer.concat(chunks).toString('utf8');
        resolve(text ? JSON.parse(text) : {});
      } catch (err) {
        reject(err);
      }
    });
    req.on('error', reject);
  });
}

function validSessionId(value) {
  return typeof value === 'string' && /^[0-9a-fA-F-]{36}$/.test(value);
}

function cleanExpired(now = Date.now()) {
  for (const [id, session] of sessions.entries()) {
    if (now - session.lastSeen > SESSION_TTL_MS) {
      sessions.delete(id);
    }
  }

  for (const [ip, entry] of rateLimits.entries()) {
    if (now - entry.startedAt > RATE_LIMIT_WINDOW_MS * 2) {
      rateLimits.delete(ip);
    }
  }
}

function validateHeartbeat(body) {
  if (!body || !validSessionId(body.sessionId)) return 'invalid sessionId';
  if (!Number.isInteger(body.world) || body.world < 300 || body.world > 999) return 'invalid world';
  if (typeof body.inWilderness !== 'boolean') return 'invalid inWilderness';
  if (typeof body.combatBracket !== 'string' || !VALID_BRACKETS.has(body.combatBracket)) return 'invalid combatBracket';
  return null;
}

function aggregate() {
  cleanExpired();
  const worlds = new Map();

  for (const session of sessions.values()) {
    if (!session.inWilderness) continue;

    let world = worlds.get(session.world);
    if (!world) {
      world = { world: session.world, total: 0, brackets: {} };
      worlds.set(session.world, world);
    }

    world.total += 1;
    world.brackets[session.combatBracket] = (world.brackets[session.combatBracket] || 0) + 1;
  }

  return Array.from(worlds.values())
    .sort((a, b) => b.total - a.total || a.world - b.world);
}

const server = http.createServer(async (req, res) => {
  if (!allowed(req)) {
    json(res, 429, { error: 'rate limit exceeded' });
    return;
  }

  const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);

  if (req.method === 'GET' && url.pathname === '/health') {
    cleanExpired();
    json(res, 200, { ok: true, activeSessions: sessions.size });
    return;
  }

  if (req.method === 'GET' && url.pathname === '/v1/activity') {
    json(res, 200, { generatedAt: Date.now(), worlds: aggregate() });
    return;
  }

  if (req.method === 'POST' && url.pathname === '/v1/heartbeat') {
    try {
      const body = await readJson(req);
      const error = validateHeartbeat(body);
      if (error) {
        json(res, 400, { error });
        return;
      }

      sessions.set(body.sessionId, {
        world: body.world,
        inWilderness: body.inWilderness,
        combatBracket: body.combatBracket,
        lastSeen: Date.now()
      });

      json(res, 200, { ok: true, expiresInMs: SESSION_TTL_MS });
    } catch (err) {
      json(res, 400, { error: 'invalid JSON body' });
    }
    return;
  }

  const match = url.pathname.match(/^\/v1\/session\/([0-9a-fA-F-]{36})$/);
  if (req.method === 'DELETE' && match) {
    sessions.delete(match[1]);
    json(res, 200, { ok: true });
    return;
  }

  json(res, 404, { error: 'not found' });
});

const cleanupTimer = setInterval(cleanExpired, Math.max(5000, Math.floor(SESSION_TTL_MS / 2)));
cleanupTimer.unref();

server.listen(PORT, () => {
  console.log(`PVP Activity API listening on port ${PORT}`);
});
