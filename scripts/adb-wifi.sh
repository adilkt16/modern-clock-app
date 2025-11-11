#!/usr/bin/env bash
set -euo pipefail

# adb-wifi.sh : Helper script to pair / connect to an Android 11+ device over Wi‑Fi.
# Usage examples:
#   Pair (first time): ./scripts/adb-wifi.sh pair 10.42.0.55:40895 673940
#   Connect (after paired): ./scripts/adb-wifi.sh connect 10.42.0.55:42245
#   List mdns services: ./scripts/adb-wifi.sh discover
#   Install APK: ./scripts/adb-wifi.sh install app/build/outputs/apk/debug/app-debug.apk
#
# The script prefers the local bundled platform-tools if present in ~/android-platform-tools/platform-tools.

ADB_BIN="${ADB_BIN:-}" 
if [[ -z "$ADB_BIN" ]]; then
  if [[ -x "$HOME/android-platform-tools/platform-tools/adb" ]]; then
    ADB_BIN="$HOME/android-platform-tools/platform-tools/adb"
  else
    ADB_BIN="$(command -v adb || true)"
  fi
fi

if [[ -z "$ADB_BIN" ]]; then
  echo "Error: adb not found. Install platform-tools or set ADB_BIN." >&2
  exit 1
fi

cmd=${1:-}
case "$cmd" in
  pair)
    hostport=${2:-}
    code=${3:-}
    if [[ -z "$hostport" || -z "$code" ]]; then
      echo "Usage: $0 pair <ip:port> <pairing-code>" >&2
      exit 1
    fi
    "$ADB_BIN" pair "$hostport" "$code"
    ;;
  connect)
    hostport=${2:-}
    if [[ -z "$hostport" ]]; then
      echo "Usage: $0 connect <ip:port>" >&2
      exit 1
    fi
    "$ADB_BIN" connect "$hostport"
    "$ADB_BIN" devices -l | grep "$hostport" || echo "Warning: device not listed yet"
    ;;
  discover)
    "$ADB_BIN" mdns services
    ;;
  install)
    apk=${2:-}
    if [[ -z "$apk" || ! -f "$apk" ]]; then
      echo "Usage: $0 install <apk-path>" >&2
      exit 1
    fi
    "$ADB_BIN" install -r "$apk"
    ;;
  disconnect)
    hostport=${2:-}
    if [[ -n "$hostport" ]]; then
      "$ADB_BIN" disconnect "$hostport"
    else
      "$ADB_BIN" disconnect
    fi
    ;;
  shell)
    shift || true
    "$ADB_BIN" shell "$@"
    ;;
  *)
    cat <<USAGE
adb-wifi.sh - Android wireless debugging helper

Commands:
  pair <ip:port> <code>     Pair with device (Android 11+ Wireless debugging)
  connect <ip:port>         Connect to already paired device
  discover                  List mDNS adb-tls services
  install <apk>             Install / replace an APK over Wi‑Fi
  disconnect [ip:port]      Disconnect one or all devices
  shell <cmd...>            Run an adb shell command

Environment:
  ADB_BIN  Path to adb executable (auto-detected if unset)

Examples:
  $0 pair 10.42.0.55:40895 673940
  $0 discover
  $0 connect 10.42.0.55:42245
  $0 install app/build/outputs/apk/debug/app-debug.apk
USAGE
    ;;
esac
