#!/usr/bin/env sh
set -eu

# Read-only, privacy-limited report for Haval H9 2025 head units.
# It intentionally avoids full getprop/package dumps, VIN, coordinates, accounts, and CAN traffic.

ADB_BIN="${ADB_BIN:-adb}"
APP_PACKAGE="${1:-com.h9smartdrive.app.debug}"

if ! "$ADB_BIN" get-state >/dev/null 2>&1; then
  echo "No Android device is connected through ADB." >&2
  exit 1
fi

echo "H9 Smart Drive 2025 diagnostic"
echo "Generated: $(date -u '+%Y-%m-%dT%H:%M:%SZ')"

for property in \
  ro.build.version.release \
  ro.build.version.sdk \
  ro.build.display.id \
  ro.product.manufacturer \
  ro.product.model \
  ro.product.name \
  ro.product.device \
  ro.product.cpu.abi
do
  value=$("$ADB_BIN" shell getprop "$property" | tr -d '\r')
  echo "$property=$value"
done

echo "features:"
"$ADB_BIN" shell pm list features | tr -d '\r' | sed -n '/automotive\|location.gps/p'

echo "known vehicle packages:"
for package_name in \
  com.gwm.android.adapter.server \
  com.beantechs.adapterservice.server \
  com.beantechs.inputservice \
  com.android.car \
  com.core.system.service \
  com.autolink.car.someip.service \
  com.autolink.tboxservice \
  com.autolink.clusterservice
do
  path=$("$ADB_BIN" shell pm path "$package_name" 2>/dev/null | tr -d '\r' || true)
  if [ -n "$path" ]; then
    echo "$package_name=installed"
  else
    echo "$package_name=missing"
  fi
done

echo "vehicle-related binder services:"
"$ADB_BIN" shell service list 2>/dev/null | tr -d '\r' | sed -n '/car\|vehicle\|bean\|adapter\|tbox\|cluster/p' || true

echo "app permissions and version:"
"$ADB_BIN" shell dumpsys package "$APP_PACKAGE" 2>/dev/null | tr -d '\r' | sed -n \
  '/versionName=/p;/versionCode=/p;/com.gwm.android.adapter.permission.GET_DATA/p;/android.car.permission/p' || true

echo "End of privacy-limited report."
