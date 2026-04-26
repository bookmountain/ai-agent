# Deployment Guide

This project runs on a homelab VM (`vm100` at `192.168.4.106`) behind a Cloudflare tunnel, deployed via GitHub Actions with a self-hosted runner.

**Live:** https://ai-agent.bookmountain.work

---

## Architecture

```
Internet → Cloudflare tunnel
  → ai-agent.bookmountain.work
    → vm100:3082
      → ai-agent-frontend (nginx)
          • serves Vue SPA (/)
          • proxies /api/* → ai-agent-backend:8123 (internal)
            → ai-agent-backend (Spring Boot)
              → shared-postgres:5432 (host)
```

Both containers share the `ai-agent-net` Docker network. The backend is not exposed on any host port — only reachable by nginx on the internal network.

---

## CI/CD Flow

Every push to `master` triggers the GitHub Actions workflow (`.github/workflows/deploy.yml`):

1. The self-hosted runner on vm100 checks out the code
2. Writes a `.env` file from GitHub Secrets (deleted after deploy)
3. Runs `docker compose up --build -d` — rebuilds both images and restarts containers
4. Cleans up `.env`

You can also trigger a deploy manually: GitHub → Actions → "Deploy to vm100" → **Run workflow**.

---

## One-Time Setup

### 1. Self-hosted runner

On vm100, create a runner directory for this repo:

```bash
mkdir /actions-runner-ai-agent && cd /actions-runner-ai-agent

curl -o actions-runner-linux-x64-2.334.0.tar.gz -L \
  https://github.com/actions/runner/releases/download/v2.334.0/actions-runner-linux-x64-2.334.0.tar.gz
tar xzf ./actions-runner-linux-x64-2.334.0.tar.gz

# Get your token from:
# github.com/bookmountain/ai-agent/settings/actions/runners → New self-hosted runner
./config.sh --url https://github.com/bookmountain/ai-agent --token <TOKEN>

# Install as a service so it survives reboots
sudo ./svc.sh install
sudo ./svc.sh start
```

Verify it's running:
```bash
sudo ./svc.sh status
# Runner should appear as Idle at github.com/bookmountain/ai-agent/settings/actions/runners
```

> Note: the token is single-use and expires quickly. If `config.sh` rejects it, generate a fresh one from GitHub.

### 2. PostgreSQL database

The project uses the `shared-postgres` container already running on vm100. Create a dedicated database:

```bash
docker exec shared-postgres psql -U postgres -c "CREATE DATABASE ai_agent;"
```

### 3. Cloudflare tunnel

Add this entry to `/etc/cloudflared/config.yml` **before** the `http_status:404` catch-all:

```yaml
  - hostname: ai-agent.bookmountain.work
    service: http://192.168.4.106:3082
```

Restart cloudflared:

```bash
sudo systemctl restart cloudflared
```

### 4. GitHub Secrets

Go to `github.com/bookmountain/ai-agent/settings/secrets/actions` and add:

| Secret | Description |
|---|---|
| `OPENAI_API_KEY` | DashScope API key (used as OpenAI-compatible endpoint) |
| `DB_URL` | `jdbc:postgresql://host.docker.internal:5432/ai_agent` |
| `DB_USERNAME` | PostgreSQL username (e.g. `postgres`) |
| `DB_PASSWORD` | PostgreSQL password |
| `SEARCH_API_KEY` | SearchAPI key for web search tool |

---

## First Deploy

After completing the setup above, trigger the first deploy:

```bash
git push origin master
# or: GitHub → Actions → "Deploy to vm100" → Run workflow
```

### Verify

```bash
# On vm100
docker ps | grep ai-agent
# Expected: ai-agent-backend and ai-agent-frontend both Up

docker logs ai-agent-backend --tail 50
# Expected: Spring Boot started on port 8123

curl http://localhost:3082
# Expected: HTML with <!DOCTYPE html>

curl http://localhost:3082/api/health
# Expected: 200 OK
```

---

## Ongoing Operations

**Redeploy:** push to `master` or click Run workflow in GitHub Actions.

**View logs:**
```bash
docker logs ai-agent-backend -f
docker logs ai-agent-frontend -f
```

**Stop:**
```bash
cd /path/to/runner/workspace && docker compose down
```

**Check runner status:**
```bash
cd /actions-runner-ai-agent && sudo ./svc.sh status
```

---

## Local Development

The backend reads `application-local.yaml` (gitignored — create your own copy from `application-prod.yaml` with real values). The frontend proxies to `http://localhost:8123` in dev mode automatically.

```bash
# Backend
mvn spring-boot:run

# Frontend
cd ai-agent-frontend
npm install
npm run dev   # http://localhost:3000
```
