#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
GITHUB_REPO="InvalidArgument3/Autisnautics2"

TEMP_DIR="./temp_server_update"
OVERRIDES_BACKUP="./overrides_backup_$(date +%Y%m%d_%H%M%S)"
PRESERVE_DIR="./preserved_local_$(date +%Y%m%d_%H%M%S)"

# Files to preserve from local server (never overwrite from update)
LOCAL_ONLY_FILES=(
  "server_starter.conf"
  "server.properties"
  "eula.txt"
  "ops.json"
  "whitelist.json"
  "banned-ips.json"
  "banned-players.json"
  "usercache.json"
)

echo -e "${GREEN}Starting server pack update...${NC}"

# Fetch the latest release info to get the actual serverpack filename
echo -e "${GREEN}Fetching latest release information...${NC}"
RELEASE_JSON=$(curl -s "https://api.github.com/repos/${GITHUB_REPO}/releases/latest")

if [ -z "$RELEASE_JSON" ]; then
  echo -e "${RED}Failed to fetch latest release information!${NC}"
  exit 1
fi

# Extract the serverpack filename from the release assets
SERVER_ZIP=$(echo "$RELEASE_JSON" | grep -oP '"name":\s*"\K[^"]*serverpack[^"]*\.zip' | head -1)

if [ -z "$SERVER_ZIP" ]; then
  echo -e "${RED}No serverpack zip file found in the latest release!${NC}"
  echo -e "${YELLOW}Available assets:${NC}"
  echo "$RELEASE_JSON" | grep -oP '"name":\s*"\K[^"]*'
  exit 1
fi

echo -e "${GREEN}Found server pack: ${SERVER_ZIP}${NC}"
DOWNLOAD_URL="https://github.com/${GITHUB_REPO}/releases/latest/download/${SERVER_ZIP}"

# Function to cleanup on exit
cleanup() {
  echo -e "${YELLOW}Cleaning up temporary files...${NC}"
  rm -rf "$TEMP_DIR"
}
trap cleanup EXIT

# Create temporary directory
mkdir -p "$TEMP_DIR"

# Download the server pack
echo -e "${GREEN}Downloading server pack from GitHub...${NC}"
if ! curl -L -o "$TEMP_DIR/$SERVER_ZIP" "$DOWNLOAD_URL"; then
  echo -e "${RED}Failed to download server pack!${NC}"
  exit 1
fi

# Extract the downloaded zip
echo -e "${GREEN}Extracting server pack...${NC}"
if ! unzip -q "$TEMP_DIR/$SERVER_ZIP" -d "$TEMP_DIR/extracted"; then
  echo -e "${RED}Failed to extract server pack!${NC}"
  exit 1
fi

# Check if overrides folder exists in extracted content
if [ ! -d "$TEMP_DIR/extracted/overrides" ]; then
  echo -e "${YELLOW}No overrides folder found, checking root-level format...${NC}"
  if [ -d "$TEMP_DIR/extracted/mods" ] || [ -d "$TEMP_DIR/extracted/config" ]; then
    mkdir -p "$TEMP_DIR/extracted/overrides"
    find "$TEMP_DIR/extracted/" -maxdepth 1 -mindepth 1 ! -name "overrides" -exec mv {} "$TEMP_DIR/extracted/overrides/" \;
  else
    echo -e "${RED}Unsupported serverpack format; no overrides and no root-level server files found.${NC}"
    exit 1
  fi
fi

# Preserve local-only files before update
mkdir -p "$PRESERVE_DIR"
for file in "${LOCAL_ONLY_FILES[@]}"; do
  if [ -f "./$file" ]; then
    cp "./$file" "$PRESERVE_DIR/$file"
  fi
done

# Backup existing folders that will be replaced
FOLDERS_TO_REPLACE=("config" "defaultconfigs" "kubejs" "mods" "resourcepacks" "shaderpacks")
BACKUP_NEEDED=false

for folder in "${FOLDERS_TO_REPLACE[@]}"; do
  if [ -d "./$folder" ]; then
    BACKUP_NEEDED=true
    break
  fi
done

if [ "$BACKUP_NEEDED" = true ]; then
  echo -e "${YELLOW}Backing up existing folders to ${OVERRIDES_BACKUP}${NC}"
  mkdir -p "$OVERRIDES_BACKUP"
  for folder in "${FOLDERS_TO_REPLACE[@]}"; do
    if [ -d "./$folder" ]; then
      cp -r "./$folder" "$OVERRIDES_BACKUP/"
    fi
  done
fi

# Remove existing folders that need to be replaced
for folder in "${FOLDERS_TO_REPLACE[@]}"; do
  if [ -d "./$folder" ]; then
    echo -e "${YELLOW}Removing existing $folder folder...${NC}"
    rm -rf "./$folder"
  fi
done

# Copy new content from overrides directly to server root
echo -e "${GREEN}Copying new content to server root...${NC}"
cp -r "$TEMP_DIR/extracted/overrides/"* "./"

# Restore local-only files so custom runtime/server settings survive updates
for file in "${LOCAL_ONLY_FILES[@]}"; do
  if [ -f "$PRESERVE_DIR/$file" ]; then
    cp "$PRESERVE_DIR/$file" "./$file"
  fi
done

# Set appropriate permissions
chmod -R 755 ./config ./defaultconfigs ./kubejs ./mods ./resourcepacks ./shaderpacks 2>/dev/null || true
chmod +x ./start_server.sh 2>/dev/null || true
chmod +x ./UPDATEFROMGITHUB.sh 2>/dev/null || true

echo -e "${GREEN}Server pack update completed successfully!${NC}"
echo -e "${YELLOW}Backup of previous overrides saved to: ${OVERRIDES_BACKUP}${NC}"

# Show what was updated
echo -e "${GREEN}Updated folders in server root:${NC}"
for folder in "${FOLDERS_TO_REPLACE[@]}"; do
  if [ -d "./$folder" ]; then
    echo -e "  ✓ $folder"
  fi
done

if [ -d "$PRESERVE_DIR" ]; then
  rm -rf "$PRESERVE_DIR"
fi
