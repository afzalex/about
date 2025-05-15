#!/bin/bash

# Check if running on macOS
if [[ "$(uname)" != "Darwin" ]]; then
    echo "❌ This script can only run on macOS"
    exit 1
fi

# === CONFIGURATION ===
REPO_PATH="$PWD"
WATCH_DIR="$REPO_PATH/docs"
LOCK_FILE="/tmp/docx-sync.lock"
COOLDOWN=300  # 5 minutes in seconds
COMMIT_MSG="Auto-sync: updated *.template.docx files"
MAX_SECONDS=86400  # 24 hours in seconds

# === START ===
cd "$REPO_PATH" || exit 1

echo "🔍 Watching $WATCH_DIR for .template.docx changes..."

/opt/homebrew/bin/fswatch -0 -r "$WATCH_DIR" | while IFS= read -r -d '' file; do
  if [[ "$file" == *.template.docx && "$file" != *~\$* ]]; then

    echo "🔄 Checking for change in $file"

    # Debounce with lock file
    if [ -f "$LOCK_FILE" ]; then
      echo "⏳ Cooldown active... skipping"
      continue
    fi
    touch "$LOCK_FILE"

    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

    if [[ "$CURRENT_BRANCH" != "master" ]]; then
      echo "❌ Not on master branch. Aborting."
      rm "$LOCK_FILE"
      continue
    fi

    # Only proceed if there are changes in template docx
    if git diff --quiet -- docs/*.template.docx; then
      echo "⚠️ No changes in .template.docx files"
      rm "$LOCK_FILE"
      continue
    fi

    echo "📦 Committing changes to .template.docx files... "

    git add docs/*.template.docx
    LAST_COMMIT_MSG=$(git log -1 --pretty=%B)
    LAST_COMMIT_TIME=$(git log -1 --pretty=%ct)
    CURRENT_TIME=$(date +%s)
    SECONDS_SINCE_LAST_COMMIT=$(( CURRENT_TIME - LAST_COMMIT_TIME ))

    if [[ "$LAST_COMMIT_MSG" == "$COMMIT_MSG" && $SECONDS_SINCE_LAST_COMMIT -lt $MAX_SECONDS ]]; then
      echo "🔄 Amending previous sync commit (last commit was $SECONDS_SINCE_LAST_COMMIT seconds ago)..."
      git commit --amend -m "$COMMIT_MSG"
     git push --force
    else
      echo "🆕 Creating new commit (last commit was $SECONDS_SINCE_LAST_COMMIT seconds ago)..."
      git commit -m "$COMMIT_MSG"
      git push
    fi

    echo "✅ Sync done. Cooling down..."
    sleep "$COOLDOWN"
    rm "$LOCK_FILE"
  fi
done
