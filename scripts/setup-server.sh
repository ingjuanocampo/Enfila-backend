#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# Enfila Backend — One-time VPS Server Setup Script
# Run this ONCE on a fresh Ubuntu 22.04 / Debian 12 VPS as root or with sudo.
#
# Usage:
#   ssh root@YOUR_SERVER_IP
#   curl -fsSL https://raw.githubusercontent.com/ingjuanocampo/Enfila-backend/main/scripts/setup-server.sh | bash
#
# Or copy the script and run it:
#   scp scripts/setup-server.sh root@YOUR_SERVER_IP:/tmp/setup-server.sh
#   ssh root@YOUR_SERVER_IP "bash /tmp/setup-server.sh"
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

APP_DIR="/opt/enfila"
APP_USER="enfila"

echo "────────────────────────────────────────────────"
echo " Enfila Backend — VPS Setup"
echo "────────────────────────────────────────────────"

# ── 1. System update ──────────────────────────────────────────────────────────
echo "[1/7] Updating system packages..."
apt-get update -y
apt-get upgrade -y
apt-get install -y curl git ufw

# ── 2. Install Docker ─────────────────────────────────────────────────────────
echo "[2/7] Installing Docker..."
if ! command -v docker &>/dev/null; then
    curl -fsSL https://get.docker.com | sh
else
    echo "       Docker already installed — skipping."
fi

# Ensure Docker Compose plugin is available
if ! docker compose version &>/dev/null; then
    apt-get install -y docker-compose-plugin
fi

systemctl enable docker
systemctl start docker

# ── 3. Create application directory ──────────────────────────────────────────
echo "[3/7] Creating app directory at $APP_DIR..."
mkdir -p "$APP_DIR/nginx"

# ── 4. Create a dedicated non-root system user ────────────────────────────────
echo "[4/7] Creating system user '$APP_USER'..."
if ! id "$APP_USER" &>/dev/null; then
    useradd --system --no-create-home --shell /bin/false "$APP_USER"
fi
usermod -aG docker "$APP_USER"
chown -R "$APP_USER":"$APP_USER" "$APP_DIR"

# ── 5. Create the deploy SSH user for GitHub Actions ─────────────────────────
echo "[5/7] Setting up deploy SSH user..."
DEPLOY_USER="deploy"
if ! id "$DEPLOY_USER" &>/dev/null; then
    useradd --create-home --shell /bin/bash "$DEPLOY_USER"
fi
usermod -aG docker "$DEPLOY_USER"

# Set up SSH directory for the deploy user
DEPLOY_SSH_DIR="/home/$DEPLOY_USER/.ssh"
mkdir -p "$DEPLOY_SSH_DIR"
chmod 700 "$DEPLOY_SSH_DIR"
touch "$DEPLOY_SSH_DIR/authorized_keys"
chmod 600 "$DEPLOY_SSH_DIR/authorized_keys"
chown -R "$DEPLOY_USER":"$DEPLOY_USER" "$DEPLOY_SSH_DIR"

# Grant deploy user write access to app directory
chown -R "$DEPLOY_USER":"$DEPLOY_USER" "$APP_DIR"

echo ""
echo "  *** ACTION REQUIRED ***"
echo "  Add your GitHub Actions SSH public key to:"
echo "  $DEPLOY_SSH_DIR/authorized_keys"
echo ""
echo "  Generate the key pair on your local machine:"
echo "    ssh-keygen -t ed25519 -C 'github-actions-deploy' -f ~/.ssh/enfila_deploy"
echo "  Then copy the public key:"
echo "    cat ~/.ssh/enfila_deploy.pub"
echo "  And paste it into $DEPLOY_SSH_DIR/authorized_keys"
echo ""

# ── 6. Configure firewall ─────────────────────────────────────────────────────
echo "[6/7] Configuring UFW firewall..."
ufw --force reset
ufw default deny incoming
ufw default allow outgoing
ufw allow ssh      # port 22
ufw allow http     # port 80
ufw allow https    # port 443
ufw --force enable

# ── 7. Print next steps ───────────────────────────────────────────────────────
echo "[7/7] Setup complete!"
echo ""
echo "────────────────────────────────────────────────"
echo " Next steps:"
echo "────────────────────────────────────────────────"
echo ""
echo "  1. Copy production files to the server:"
echo "     scp docker-compose.prod.yml $DEPLOY_USER@YOUR_SERVER_IP:$APP_DIR/"
echo "     scp nginx/nginx.conf        $DEPLOY_USER@YOUR_SERVER_IP:$APP_DIR/nginx/"
echo "     scp init.sql                $DEPLOY_USER@YOUR_SERVER_IP:$APP_DIR/"
echo ""
echo "  2. Create .env from the template:"
echo "     ssh $DEPLOY_USER@YOUR_SERVER_IP"
echo "     cp $APP_DIR/.env.example $APP_DIR/.env"
echo "     nano $APP_DIR/.env   # fill in all secrets"
echo ""
echo "  3. Add GitHub Actions secrets in your repo:"
echo "     VPS_HOST                    → your server IP or domain"
echo "     VPS_USER                    → $DEPLOY_USER"
echo "     VPS_SSH_KEY                 → private key from ~/.ssh/enfila_deploy"
echo "     TWILIO_SID                  → from Twilio console"
echo "     TWILIO_TOKEN                → from Twilio console"
echo "     FIREBASE_SERVICE_ACCOUNT_JSON → contents of firebase-service-account.json"
echo "     GHCR_TOKEN                  → GitHub PAT with read:packages scope"
echo ""
echo "  4. Push to main to trigger the first automated deployment."
echo ""
