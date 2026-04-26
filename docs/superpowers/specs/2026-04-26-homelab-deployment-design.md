# Homelab Deployment Design

**Date:** 2026-04-26
**Goal:** Deploy ai-agent to vm100 (book@192.168.4.106) at `ai-agent.bookmountain.work` with GitHub Actions CI/CD via self-hosted runner.

---

## Architecture

Two Docker containers managed by `docker-compose.yml`, built on vm100 by the self-hosted GitHub Actions runner.

```
Cloudflare → ai-agent.bookmountain.work
    → http://192.168.4.106:3082
        → ai-agent-frontend (nginx:80)
            → serves Vue static files
            → proxies /api/* → http://ai-agent-backend:8123
                → ai-agent-backend (Spring Boot:8123)
                    → host.docker.internal:5432 (shared-postgres)
```

### Services

| Service | Container name | Image | Exposed port |
|---|---|---|---|
| `ai-agent-frontend` | ai-agent-frontend | built from `./ai-agent-frontend` | `3082:80` (host) |
| `ai-agent-backend` | ai-agent-backend | built from `.` | internal only |

Both services share the `ai-agent-net` bridge network.
The backend uses `extra_hosts: ["host.docker.internal:host-gateway"]` to reach `shared-postgres` on the host.

### Database

- Host: `host.docker.internal:5432` (shared-postgres container on vm100)
- Database: `ai_agent` (must be created manually before first deploy)
- Credentials: injected via GitHub Secrets at deploy time

---

## Files Changed

### New: `docker-compose.yml`
- Defines `ai-agent-backend` and `ai-agent-frontend` services
- Loads secrets from `.env` (written by CI, never committed)
- Backend: no host port, internal only
- Frontend: port `3082:80`
- Network: `ai-agent-net`

### Modified: `ai-agent-frontend/nginx.conf`
- Change `proxy_pass` from `https://www.codefather.cn/api/` to `http://ai-agent-backend:8123`
- Update `proxy_set_header Host` to match

### Modified: `src/main/resources/application-prod.yaml`
- Replace all hardcoded values with environment variable references:
  - `spring.ai.openai.api-key` → `${OPENAI_API_KEY}`
  - `spring.datasource.url` → `${DB_URL}`
  - `spring.datasource.username` → `${DB_USERNAME}`
  - `spring.datasource.password` → `${DB_PASSWORD}`
  - `search-api.api-key` → `${SEARCH_API_KEY}`

### Modified: `Dockerfile`
- Fix jar name: `book-ai-agent-0.0.1-SNAPSHOT.jar` → `ai-agent-0.0.1-SNAPSHOT.jar`

### New: `.github/workflows/deploy.yml`
- Triggers: `push` to `main` and `workflow_dispatch` (manual button)
- Runs on: `self-hosted` runner (already configured on vm100)
- Steps:
  1. `actions/checkout@v4`
  2. Write `.env` file from GitHub Secrets
  3. `docker compose up --build -d`
  4. Remove `.env` (always, even on failure)

### Modified: `.gitignore`
- Add `.env`

---

## GitHub Secrets Required

| Secret name | Value |
|---|---|
| `OPENAI_API_KEY` | DashScope API key (sk-...) |
| `DB_URL` | `jdbc:postgresql://host.docker.internal:5432/ai_agent` |
| `DB_USERNAME` | postgres user for ai_agent db |
| `DB_PASSWORD` | postgres password |
| `SEARCH_API_KEY` | SearchAPI key |

---

## One-time vm100 Setup (manual)

```bash
# 1. Create the database in shared-postgres
docker exec shared-postgres psql -U postgres -c "CREATE DATABASE ai_agent;"

# 2. Add to /etc/cloudflared/config.yml (before the catch-all line):
#   - hostname: ai-agent.bookmountain.work
#     service: http://192.168.4.106:3082

sudo nano /etc/cloudflared/config.yml
sudo systemctl restart cloudflared
```

---

## Out of Scope

- `ai-image-search-mcp-server` — omitted for now, can be added as a third service later
- Ollama — remains `localhost:11434` in config, not containerised
