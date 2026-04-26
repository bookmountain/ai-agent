# Homelab Deployment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deploy ai-agent (Spring Boot backend + Vue frontend) to vm100 at `ai-agent.bookmountain.work` with GitHub Actions CI/CD via self-hosted runner.

**Architecture:** Two Docker containers (`ai-agent-backend`, `ai-agent-frontend`) on a shared `ai-agent-net` bridge network. nginx serves the Vue SPA and proxies `/api/*` to the backend internally. The self-hosted runner on vm100 builds and starts both containers on every push to main.

**Tech Stack:** Spring Boot 3.5 / Java 21, Vue 3 / nginx, Docker Compose, GitHub Actions (self-hosted runner), PostgreSQL 17 (shared-postgres on host)

---

## File Map

| File | Action | Responsibility |
|---|---|---|
| `Dockerfile` | Modify | Fix incorrect jar filename |
| `.gitignore` | Modify | Remove `application-prod.yaml` entry; add `.env` |
| `src/main/resources/application-prod.yaml` | Modify + unignore | Replace hardcoded secrets with `${ENV_VAR}` so it's safe to commit |
| `ai-agent-frontend/nginx.conf` | Modify | Proxy `/api/*` to `http://ai-agent-backend:8123` instead of codefather.cn |
| `docker-compose.yml` | Create | Define both services, network, env_file |
| `.github/workflows/deploy.yml` | Create | Self-hosted runner CI/CD workflow |

---

## Task 1: Fix Dockerfile and gitignore

**Files:**
- Modify: `Dockerfile:14`
- Modify: `.gitignore:37-38`

- [ ] **Step 1: Fix the jar name in Dockerfile**

The CMD references `book-ai-agent-0.0.1-SNAPSHOT.jar` but Maven produces `ai-agent-0.0.1-SNAPSHOT.jar` (matches `<artifactId>ai-agent</artifactId>` in pom.xml).

Replace line 14 of `Dockerfile`:
```dockerfile
# Before
CMD ["java", "-jar", "/app/target/book-ai-agent-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]

# After
CMD ["java", "-jar", "/app/target/ai-agent-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]
```

- [ ] **Step 2: Update .gitignore**

Replace the entire `### Custom ###` section (currently lines 37–43) with (removes `application-prod.yaml`, adds `.env`):
```
### Custom ###
application-local.yaml
.env
src/main/resources/mcp-servers.json
src/main/java/com/book/aiagent/demo/invoke/TestApiKey.java
tmp
.claude/
```

- [ ] **Step 3: Commit**

```bash
git add Dockerfile .gitignore
git commit -m "fix: correct jar name in Dockerfile, update gitignore for deployment"
```

---

## Task 2: Update application-prod.yaml to use env vars

**Files:**
- Modify: `src/main/resources/application-prod.yaml`

The file is currently gitignored and contains hardcoded credentials. We replace all secrets with `${ENV_VAR}` and commit the file.

- [ ] **Step 1: Replace the entire file content**

`src/main/resources/application-prod.yaml`:
```yaml
spring:
  application:
    name: ai-agent
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://dashscope-intl.aliyuncs.com/compatible-mode
      chat:
        options:
          model: qwen-max
      embedding:
        options:
          model: text-embedding-v4
    ollama:
      base-url: http://localhost:11434
      chat:
        model: gemma3:1b
    vectorstore:
      pgvector:
        index-type: HNSW
        dimensions: 1024
        distance-type: COSINE_DISTANCE
        max-document-batch-size: 10
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
server:
  port: 8123
  servlet:
    context-path: /api
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs
  group-configs:
    - group: 'default'
      paths-to-match: '/**'
      packages-to-scan: com.book.aiagent.controller
knife4j:
  enable: true
  setting:
    language: en
search-api:
  api-key: ${SEARCH_API_KEY}
app:
  rag:
    pgvector:
      enabled: true
      initialize-schema: true
      seed-on-startup: false
      seed-if-empty-only: true
logging:
  level:
    org.springframework.ai: INFO
```

- [ ] **Step 2: Stage and commit**

```bash
git add src/main/resources/application-prod.yaml
git commit -m "fix: replace hardcoded secrets with env vars in prod config"
```

Expected: the file appears as a new tracked file (it was previously gitignored). Verify with `git show HEAD --stat` — should include `application-prod.yaml`.

---

## Task 3: Fix nginx.conf to proxy to backend container

**Files:**
- Modify: `ai-agent-frontend/nginx.conf:15-35`

The current proxy_pass points to `https://www.codefather.cn/api/`. It must point to the backend container on the Docker network.

Note on proxy_pass: using `http://ai-agent-backend:8123` (no trailing slash, no URI component) tells nginx to forward the request URI unchanged. So `GET /api/chat/stream` → `http://ai-agent-backend:8123/api/chat/stream`, which matches Spring Boot's `/api` context-path.

- [ ] **Step 1: Replace the entire nginx.conf**

`ai-agent-frontend/nginx.conf`:
```nginx
server {
    listen       80;
    server_name  localhost;

    root   /usr/share/nginx/html;

    location / {
        index  index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    location ^~ /api/ {
        proxy_pass http://ai-agent-backend:8123;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_set_header Connection "";
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_cache off;
        chunked_transfer_encoding off;
        proxy_read_timeout 600s;

        proxy_intercept_errors off;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        access_log off;
        add_header Cache-Control "public";
    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add ai-agent-frontend/nginx.conf
git commit -m "fix: proxy /api/* to ai-agent-backend container"
```

---

## Task 4: Create docker-compose.yml

**Files:**
- Create: `docker-compose.yml`

- [ ] **Step 1: Create the file**

`docker-compose.yml` (project root):
```yaml
services:
  ai-agent-backend:
    build: .
    container_name: ai-agent-backend
    env_file: .env
    networks:
      - ai-agent-net
    extra_hosts:
      - "host.docker.internal:host-gateway"
    restart: unless-stopped

  ai-agent-frontend:
    build: ./ai-agent-frontend
    container_name: ai-agent-frontend
    ports:
      - "3082:80"
    depends_on:
      - ai-agent-backend
    networks:
      - ai-agent-net
    restart: unless-stopped

networks:
  ai-agent-net:
    driver: bridge
```

Key decisions:
- `env_file: .env` — backend reads secrets from `.env` (written by CI, never committed)
- `extra_hosts` — lets the backend container resolve `host.docker.internal` to the host IP (required on Linux Docker to reach `shared-postgres`)
- Backend has no `ports:` — only reachable via the `ai-agent-net` network by nginx
- `depends_on` ensures backend starts before frontend (but does not wait for healthy — Spring Boot starts fast enough)

- [ ] **Step 2: Validate the compose file**

```bash
docker compose config
```

Expected: YAML printed with no errors. If `docker` is not available locally, skip — CI will catch it.

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml
git commit -m "feat: add docker-compose for homelab deployment"
```

---

## Task 5: Create GitHub Actions workflow

**Files:**
- Create: `.github/workflows/deploy.yml`

- [ ] **Step 1: Create the workflows directory and file**

```bash
mkdir -p .github/workflows
```

`.github/workflows/deploy.yml`:
```yaml
name: Deploy to vm100

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: self-hosted

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Write .env
        run: |
          cat > .env << 'EOF'
          OPENAI_API_KEY=${{ secrets.OPENAI_API_KEY }}
          DB_URL=${{ secrets.DB_URL }}
          DB_USERNAME=${{ secrets.DB_USERNAME }}
          DB_PASSWORD=${{ secrets.DB_PASSWORD }}
          SEARCH_API_KEY=${{ secrets.SEARCH_API_KEY }}
          EOF

      - name: Deploy
        run: docker compose up --build -d

      - name: Remove .env
        if: always()
        run: rm -f .env
```

Notes:
- `runs-on: self-hosted` — targets the runner already configured on vm100
- `workflow_dispatch` — enables the "Run workflow" button in the GitHub Actions UI
- `if: always()` on the cleanup step ensures `.env` is deleted even if the deploy fails
- `--build` rebuilds images on every deploy; `--detach` keeps containers running after the workflow finishes

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/deploy.yml
git commit -m "feat: add GitHub Actions deploy workflow for vm100"
```

---

## Task 6: Push and configure GitHub Secrets

- [ ] **Step 1: Push all commits to main**

```bash
git push origin main
```

- [ ] **Step 2: Add GitHub Secrets**

Go to: `https://github.com/<your-username>/ai-agent/settings/secrets/actions`

Add these 5 repository secrets:

| Name | Value |
|---|---|
| `OPENAI_API_KEY` | Your DashScope key (starts with `sk-`) |
| `DB_URL` | `jdbc:postgresql://host.docker.internal:5432/ai_agent` |
| `DB_USERNAME` | `postgres` (or your dedicated user) |
| `DB_PASSWORD` | Your postgres superuser password |
| `SEARCH_API_KEY` | Your SearchAPI key |

- [ ] **Step 3: Run one-time vm100 setup**

SSH into vm100 and run:

```bash
# Create the database (only needed once)
docker exec shared-postgres psql -U postgres -c "CREATE DATABASE ai_agent;"

# Add cloudflare entry — open the file and add the two lines before the catch-all
sudo nano /etc/cloudflared/config.yml
```

Add these two lines **before** the `- service: http_status:404` line:
```yaml
  - hostname: ai-agent.bookmountain.work
    service: http://192.168.4.106:3082
```

```bash
# Restart cloudflared to pick up the new entry
sudo systemctl restart cloudflared
```

- [ ] **Step 4: Trigger the first deploy**

Either push a commit to main, or go to:
`https://github.com/<your-username>/ai-agent/actions` → "Deploy to vm100" → "Run workflow"

- [ ] **Step 5: Verify the deployment**

```bash
# On vm100 — check containers are running
docker ps | grep ai-agent

# Expected output (two containers):
# ... ai-agent-backend ...  Up ...
# ... ai-agent-frontend ...  Up ...  0.0.0.0:3082->80/tcp

# Check backend logs for startup errors
docker logs ai-agent-backend --tail 50

# Check frontend is serving
curl http://localhost:3082
# Expected: HTML response with <!DOCTYPE html>

# Check API proxy works
curl http://localhost:3082/api/health
# Expected: 200 OK (HealthController endpoint)
```
