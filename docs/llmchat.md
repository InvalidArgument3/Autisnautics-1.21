# llmchat — in-game chat to LLM

Server-side mod (NeoForge 1.21.1) that lets players talk to a language model from chat:
type `@bot <question>`, the reply is broadcast to everyone. Clients install nothing —
the jar only does anything on the server.

Source: https://github.com/InvalidArgument3/llmchat (private, MIT; ported from
[Yaetrna/Minecraft-LLM-Chat-Integration](https://github.com/Yaetrna/Minecraft-LLM-Chat-Integration), Forge 1.20.1).

## How it works

1. `ServerChatEvent` fires for every player message. If it contains a trigger name
   (`@bot`, `@ai` — configurable, case-insensitive) the mod takes the rest of the line
   as the prompt. The player's own message is never cancelled.
2. The request runs on a background thread (`llmchat-api-1`), so a slow API never blocks
   a game tick. It posts to any OpenAI-compatible `/chat/completions` endpoint.
3. The reply is parsed, section signs (`§`) are stripped so the model cannot inject
   colour/formatting, and it is capped at `maxReplyChars`. Long answers are split at word
   boundaries into several chat lines, prefixed `<Bot>` then `...`.
4. The exchange is appended to a shared, bounded conversation history, so a follow-up
   question from anyone in the server keeps context (`historySize`, 0 disables).
5. Per-player cooldown (`cooldownSeconds`, default 15) stops someone spamming the API.

Endpoint and model are ordinary pack config (`config/llmchat-common.toml`): any
OpenAI-compatible server works — OpenRouter, OpenAI, DeepSeek, or a local llama-server.
The stack currently points at DeepSeek `deepseek-flash`.

## The API key is deliberately not in the config

`config/` is copied into pack exports, git and every backup, so a key there would leak.
`llmchat-common.toml` therefore has **no key field**. The key is resolved at runtime:

1. `LLMCHAT_API_KEY` environment variable on the server process ← what we use
2. `LLMCHAT_KEY_FILE` → path to a file containing the key
3. `connection.keyFile` (a **path** only, default `/etc/llmchat/api-key`)
4. none of the above → no `Authorization` header at all, which is what a local
   llama-server or Ollama wants

On CT109 the key lives in `/etc/llmchat/env` (mode 600, `root:root`) and reaches the
process via a systemd drop-in that sets `EnvironmentFile=-/etc/llmchat/env`. Set or
rotate it with:

```bash
umask 077; printf 'LLMCHAT_API_KEY=your-key-here\n' > /etc/llmchat/env
systemctl restart minecraft
```

The mod warns in the log if the key file is group/world readable or sits inside the
server directory (where a backup or export would swallow it), and it only ever logs the
last four characters. `/llmstatus` prints the source and that masked fingerprint.

## Ops commands (permission level 2)

| Command | What it does |
|---|---|
| `/llmstatus` | model, endpoint, key source + `****1234` fingerprint |
| `/llmreload` | re-read the key and the knowledge file without a restart |
| `/llmreset` | clear the shared conversation memory |
| `/llmtest <text>` | run a synthetic chat line through the real handler (console/RCON) |

`/llmtest @bot hello` is the fastest check — it exercises trigger parsing, the HTTP call,
the reply parser and the broadcast, and works from the server console or RCON.

## Config knobs worth knowing

| Key | Default | Notes |
|---|---|---|
| `connection.baseUrl` | DeepSeek | any OpenAI-compatible `/chat/completions` |
| `connection.defaultModel` | `deepseek-flash` | model slug for the endpoint |
| `triggers.triggerNames` | `["bot","ai"]` | what players type after `@` |
| `triggers.nameModelMap` | `[]` | `["bot=some/model"]` to give a name its own model |
| `behaviour.knowledgeFile` | `config/llmchat-knowledge.md` | extra system message; missing file is fine |
| `behaviour.historySize` | 24 | shared memory, 0 = off |
| `behaviour.maxTokens` / `maxReplyChars` | 400 / 1200 | keeps answers chat-sized |
| `limits.cooldownSeconds` | 15 | per player |

## Where it lives

- jar: `.pakku/overrides/mods/llmchat-1.0.0.jar` (hand-shipped, not a registry project)
- config: `config/llmchat-common.toml`
- on CT109: `mods/llmchat-1.0.0.jar`, `config/llmchat-common.toml`, plus
  `/etc/systemd/system/minecraft.service.d/llmchat.conf` and `/etc/llmchat/env`