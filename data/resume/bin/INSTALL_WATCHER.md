# Docx Sync Watcher Installation Guide

This guide will help you set up the docx sync watcher as a background service on macOS.

## Prerequisites

- macOS operating system
- Git installed
- `fswatch` installed (install via Homebrew: `brew install fswatch`)

## Installation Methods

You can choose either method:

### Method 1: Using Launch Agent (Recommended)

#### 1. Create a Launch Agent

Create a Launch Agent plist file to run the script as a background service:

```bash
# Create LaunchAgents directory if it doesn't exist
mkdir -p ~/Library/LaunchAgents

# Create the plist file
vim ~/Library/LaunchAgents/com.afzal.docxsync.plist
```

Paste the following content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN"
 "http://www.apple.com/DTDs/PropertyList-1.0.dtd">

<plist version="1.0">
  <dict>
    <key>Label</key>
    <string>com.afzal.docxsync</string>

    <key>ProgramArguments</key>
    <array>
      <string>/bin/bash</string>
      <string>/Users/mohammadafzal/sources/about/data/resume/bin/watch-docx-sync.sh</string>
    </array>

    <key>RunAtLoad</key>
    <true/>

    <key>KeepAlive</key>
    <true/>

    <key>StandardOutPath</key>
    <string>/Users/mohammadafzal/sources/about/data/resume/build/docxsync.log</string>

    <key>StandardErrorPath</key>
    <string>/Users/mohammadafzal/sources/about/data/resume/build/docxsync-error.log</string>

    <key>WorkingDirectory</key>
    <string>/Users/mohammadafzal/sources/about/data/resume</string>
  </dict>
</plist>
```

#### 2. Load the Service

Load the Launch Agent to start the service:

```bash
launchctl load ~/Library/LaunchAgents/com.afzal.docxsync.plist
```

### Method 2: Using macOS Automator

1. Open Automator
2. Create a new Application
3. Add a "Run Shell Script" action
4. Paste the following script:
```bash
cd /Users/mohammadafzal/sources/about/data/resume
./bin/watch-docx-sync.sh
```
5. Save the application (e.g., as "DocxSync.app")
6. Add the application to your Login Items:
   - System Settings → General → Login Items
   - Click "+" and select your saved application

## Managing the Service

### View Logs

The script's output is logged to:
- Standard output: `/Users/mohammadafzal/sources/about/data/resume/build/docxsync.log`
- Error output: `/Users/mohammadafzal/sources/about/data/resume/build/docxsync-error.log`

You can monitor the logs using:
```bash
tail -f /Users/mohammadafzal/sources/about/data/resume/build/docxsync.log
```

### Managing Launch Agent Service

#### Check Service Status
```bash
# List all loaded services
launchctl list | grep docxsync

# Check if the service is running
ps aux | grep watch-docx-sync
```

#### Stop the Service
```bash
# Unload the service (stops it immediately)
launchctl unload ~/Library/LaunchAgents/com.afzal.docxsync.plist

# Verify it's stopped
launchctl list | grep docxsync
```

#### Restart the Service
```bash
# Unload first
launchctl unload ~/Library/LaunchAgents/com.afzal.docxsync.plist

# Then load again
launchctl load ~/Library/LaunchAgents/com.afzal.docxsync.plist
```

#### Remove the Service
```bash
# Stop the service
launchctl unload ~/Library/LaunchAgents/com.afzal.docxsync.plist

# Remove the plist file
rm ~/Library/LaunchAgents/com.afzal.docxsync.plist

# Verify it's removed
launchctl list | grep docxsync
```

### Managing Automator Service

#### Stop the Service
- Remove the application from Login Items:
  - System Settings → General → Login Items
  - Select the app and click "-"
- Quit the application from the Dock

#### Restart the Service
- Add the application back to Login Items
- Launch the application from Applications folder

## Features

- Automatically watches for changes in `.template.docx` files
- Commits changes to git repository
- Amends previous commit if within 30 seconds
- Creates new commit if more than 30 seconds have passed
- 5-second cooldown between commits
- Runs only on macOS
- Logs all activity to the build directory

## Troubleshooting

1. Check if the service is running (Launch Agent Method):
```bash
launchctl list | grep docxsync
```

2. Check the logs for errors:
```bash
cat /Users/mohammadafzal/sources/about/data/resume/build/docxsync-error.log
```

3. Verify the repository path:
```bash
pwd  # Use this output to verify the path in plist
```

4. If the service won't stop:
```bash
# Force stop the process
pkill -f watch-docx-sync

# Then unload the service
launchctl unload ~/Library/LaunchAgents/com.afzal.docxsync.plist
```
