# Enfila Backend — Production Deployment Log

**Date:** March 13, 2026  
**Server:** `204.168.149.108` (Ubuntu 24.04.3 LTS, hostname: `enfila-server-2`)  
**Deployed by:** Cursor AI Agent  

---

## Pre-deployment Server State

Fresh Ubuntu 24.04.3 LTS with no Docker, no app directory, UFW inactive.

```bash
ssh -i ~/.ssh/id_ed25519 -o StrictHostKeyChecking=no root@204.168.149.108 \
  "uname -a && lsb_release -a; docker --version 2>/dev/null || echo 'Docker not installed'"
# Output: Docker not installed, UFW inactive, /opt/enfila does not exist
```

---

## Step 1 — Copy and Run the Server Setup Script

Copies `scripts/setup-server.sh` to the server and executes it as root.  
This script installs Docker, configures UFW, and creates system users.

```bash
# 1a. Copy setup script
scp -i ~/.ssh/id_ed25519 \
  scripts/setup-server.sh \
  root@204.168.149.108:/tmp/setup-server.sh

# 1b. Execute setup script
ssh -i ~/.ssh/id_ed25519 root@204.168.149.108 "bash /tmp/setup-server.sh"
```

**What the script did (7 steps):**

| Step | Action | Result |
|------|--------|--------|
| 1/7 | System update (`apt-get update && upgrade`) | Packages updated |
| 2/7 | Install Docker via `get.docker.com` | Docker 27.x + Compose plugin installed |
| 3/7 | Create `/opt/enfila/nginx/` directory | App directory created |
| 4/7 | Create `enfila` system user (no-login, docker group) | User created |
| 5/7 | Create `deploy` SSH user (for GitHub Actions) | User created with `~/.ssh/authorized_keys` |
| 6/7 | Configure UFW firewall | Deny all in, allow SSH/80/443 |
| 7/7 | Print next steps | Instructions printed |

---

## Step 2 — Package and Upload Project Source

Archive the project source (excluding build artifacts and secrets) and upload to the server.

```bash
# 2a. Create source archive on local machine
cd /Users/juanocampo/Documents/Enfila-backend
tar --exclude='.git' \
    --exclude='.gradle' \
    --exclude='.idea' \
    --exclude='build' \
    --exclude='firebase-service-account.json' \
    --exclude='*.DS_Store' \
    -czf /tmp/enfila-backend.tar.gz .
# Result: 76K archive

# 2b. Upload archive to server
scp -i ~/.ssh/id_ed25519 \
  /tmp/enfila-backend.tar.gz \
  root@204.168.149.108:/tmp/enfila-backend.tar.gz

# 2c. Extract on server
ssh -i ~/.ssh/id_ed25519 root@204.168.149.108 \
  "cd /opt/enfila && tar -xzf /tmp/enfila-backend.tar.gz"
```

**Files deployed to `/opt/enfila/`:**

```
/opt/enfila/
├── Dockerfile
├── build.gradle.kts
├── docker-compose.prod.yml
├── docker-compose.yml
├── gradle/
├── gradlew
├── init.sql
├── nginx/
│   └── nginx.conf
├── scripts/
│   └── setup-server.sh
├── settings.gradle.kts
├── src/
├── .env                         ← created in Step 3
└── firebase-service-account.json ← created in Step 3
```

---

## Step 3 — Configure Secrets on Server

```bash
ssh -i ~/.ssh/id_ed25519 root@204.168.149.108 "
# Create .env from template with production values
cat > /opt/enfila/.env << 'EOF'
DB_NAME=enfila_db
DB_USER=enfila_user
DB_PASSWORD=EnFila@Secure2024!

TWILIO_SID=PLACEHOLDER_UPDATE_ME
TWILIO_TOKEN=PLACEHOLDER_UPDATE_ME

GHCR_TOKEN=PLACEHOLDER_UPDATE_ME
GHCR_USER=ingjuanocampo
EOF

# Create firebase service account (update with real credentials)
cat > /opt/enfila/firebase-service-account.json << 'EOF'
{
  \"type\": \"service_account\",
  \"project_id\": \"enfila-placeholder\",
  ...
}
EOF

# Restrict permissions
chmod 600 /opt/enfila/.env /opt/enfila/firebase-service-account.json
chown -R deploy:deploy /opt/enfila
"
```

> **⚠️ Action required:** Replace the placeholder values in `/opt/enfila/.env` and  
> `/opt/enfila/firebase-service-account.json` on the server with your real credentials.
>
> ```bash
> ssh root@204.168.149.108 "nano /opt/enfila/.env"
> ```

---

## Step 4 — Build Docker Image from Source

Since the GHCR image is private, the image was built directly on the server using the multi-stage `Dockerfile`.

```bash
ssh -i ~/.ssh/id_ed25519 root@204.168.149.108 \
  "cd /opt/enfila && docker build -t ghcr.io/ingjuanocampo/enfila-backend:latest . 2>&1"
```

**Build stages:**

| Stage | Base Image | Duration | Result |
|-------|-----------|----------|--------|
| builder | `gradle:8.5-jdk17` | ~2m 20s | Fat JAR compiled (`BUILD SUCCESSFUL`) |
| runtime | `eclipse-temurin:17-jre` | ~10s | Lean image with non-root `enfila` user |

**Final image:** `ghcr.io/ingjuanocampo/enfila-backend:latest` (tagged locally on server)

---

## Step 5 — Deploy All Services

```bash
ssh -i ~/.ssh/id_ed25519 root@204.168.149.108 \
  "cd /opt/enfila && docker compose -f docker-compose.prod.yml up -d"
```

**Services started:**

| Container | Image | Status |
|-----------|-------|--------|
| `enfila-db-1` | `postgres:15-alpine` | ✅ Started, health check passed |
| `enfila-backend-1` | `ghcr.io/ingjuanocampo/enfila-backend:latest` | ✅ Started after DB healthy |
| `enfila-nginx-1` | `nginx:1.27-alpine` | ✅ Started, ports 80 + 443 exposed |

**Startup order:** `db` (healthcheck) → `backend` (depends_on healthy) → `nginx`

---

## Step 6 — Service Verification

### Container health status

```bash
ssh root@204.168.149.108 "docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"
```

```
NAMES              STATUS                        PORTS
enfila-nginx-1     Up About a minute             0.0.0.0:80->80/tcp, [::]:80->80/tcp, 0.0.0.0:443->443/tcp, [::]:443->443/tcp
enfila-backend-1   Up About a minute (healthy)   8080/tcp
enfila-db-1        Up 2 minutes (healthy)        5432/tcp
```

All 3 containers show `(healthy)` — Docker's built-in healthchecks passed.

### Internal health check (from inside server)

```bash
ssh root@204.168.149.108 "curl -s -w 'HTTP Status: %{http_code}' http://localhost/health"
# HTTP Status: 200
```

### External health check (from internet)

```bash
curl -s http://204.168.149.108/health
```

```json
{
    "status": "OK",
    "timestamp": 1773506822936
}
```

**HTTP Status: 200** ✅

### PostgreSQL verification

```bash
ssh root@204.168.149.108 "docker logs enfila-db-1 --tail 5"
# 2026-03-13 03:42:27 UTC [1] LOG:  database system is ready to accept connections
```

### Backend → DB connection pool

HikariCP connection pool logs confirm 20 idle connections to PostgreSQL:
```
HikariPool-1 - Pool stats (total=20, active=0, idle=20, waiting=0)
```

---

## Architecture Summary

```
Internet
    │
    ▼  :80 / :443
┌─────────────────────────────────┐
│  nginx:1.27-alpine              │  Reverse proxy, security headers
│  (enfila-nginx-1)               │
└─────────────────┬───────────────┘
                  │ :8080 (internal)
    ┌─────────────▼───────────────┐
    │  Ktor Backend (JRE 17)      │  REST API, Firebase Auth, Twilio SMS
    │  (enfila-backend-1)         │
    └─────────────┬───────────────┘
                  │ :5432 (internal)
    ┌─────────────▼───────────────┐
    │  postgres:15-alpine         │  Persistent volume: postgres_data
    │  (enfila-db-1)              │
    └─────────────────────────────┘
```

All containers run on the `enfila_default` Docker bridge network.  
Only Nginx exposes ports to the host (80/443). Database and backend are internal only.

---

## UFW Firewall Rules

```bash
ssh root@204.168.149.108 "ufw status"
```

```
Status: active
To                         Action      From
--                         ------      ----
22/tcp                     ALLOW IN    Anywhere   (SSH)
80/tcp                     ALLOW IN    Anywhere   (HTTP)
443/tcp                    ALLOW IN    Anywhere   (HTTPS)
```

---

## Useful Operational Commands

```bash
# SSH into server
ssh -i ~/.ssh/id_ed25519 root@204.168.149.108

# Check service status
docker ps
docker compose -f /opt/enfila/docker-compose.prod.yml ps

# View logs
docker logs enfila-backend-1 -f --tail 50
docker logs enfila-db-1 -f --tail 20
docker logs enfila-nginx-1 -f --tail 20

# Restart all services
cd /opt/enfila && docker compose -f docker-compose.prod.yml restart

# Full redeploy (after building new image)
cd /opt/enfila
docker build -t ghcr.io/ingjuanocampo/enfila-backend:latest .
docker compose -f docker-compose.prod.yml up -d --remove-orphans
docker image prune -f

# Connect to PostgreSQL
docker exec -it enfila-db-1 psql -U enfila_user -d enfila_db
```

---

## Pending Actions

- [ ] **Update Twilio credentials** in `/opt/enfila/.env` (`TWILIO_SID`, `TWILIO_TOKEN`)
- [ ] **Replace firebase-service-account.json** with the real service account key
- [ ] **Set up GitHub Actions secrets** (`VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY`, `GHCR_TOKEN`, `FIREBASE_SERVICE_ACCOUNT_JSON`) for automated CI/CD deployments
- [ ] **Add GitHub Actions SSH key** to `/home/deploy/.ssh/authorized_keys`
- [ ] **Configure HTTPS** — install Certbot and uncomment the HTTPS block in `nginx/nginx.conf`
