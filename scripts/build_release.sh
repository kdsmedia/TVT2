#!/usr/bin/env bash
set -u
ROOT=$(cd "$(dirname "$0")" && pwd)
cd "$ROOT" || exit 1
LOG=/tmp/build_release.log
: > "$LOG"
echo "[1/5] Setup JDK..." | tee -a "$LOG"
if ! command -v java >/dev/null 2>&1; then
    sudo apt-get update -qq >> "$LOG" 2>&1
    sudo apt-get install -y -qq openjdk-21-jdk-headless unzip zip >> "$LOG" 2>&1
fi
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=/opt/android-sdk
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin"
echo "[2/5] Setup Android SDK..." | tee -a "$LOG"
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    sudo mkdir -p "$ANDROID_HOME/cmdline-tools"
    sudo chown -R "$(id -u):$(id -g)" "$ANDROID_HOME"
    cd /tmp
    if [ ! -f cmdtools.zip ]; then
        curl -sL --max-time 300 -o cmdtools.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    fi
    cd "$ANDROID_HOME/cmdline-tools"
    unzip -q -o /tmp/cmdtools.zip
    mv cmdline-tools latest
fi
if [ ! -d "$ANDROID_HOME/platforms/android-36" ]; then
    yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses >> "$LOG" 2>&1 || true
    yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --install "platform-tools" "platforms;android-36" "build-tools;36.0.0" >> "$LOG" 2>&1 || true
fi
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
echo "[3/5] Building APK + AAB (first run downloads dependencies)..." | tee -a "$LOG"
./gradlew --no-daemon assembleRelease bundleRelease --stacktrace >> "$LOG" 2>&1
RC=$?
if [ "$RC" -ne 0 ]; then
    echo "BUILD FAILED rc=$RC" | tee -a "$LOG"
    tail -80 "$LOG"
    exit "$RC"
fi
echo "[4/5] Copying artifacts..." | tee -a "$LOG"
mkdir -p ALTOMEDIA/release
cp -v app/build/outputs/apk/release/app-release.apk "ALTOMEDIA/release/TVT-v1.0.0-release.apk" >> "$LOG" 2>&1
cp -v app/build/outputs/bundle/release/app-release.aab "ALTOMEDIA/release/TVT-v1.0.0-release.aab" >> "$LOG" 2>&1
echo "[5/5] Git commit + push..." | tee -a "$LOG"
git add -A
git commit -m "Add signed release APK and AAB v1.0.0 artifacts" >> "$LOG" 2>&1 || true
git push origin master >> "$LOG" 2>&1
ls -la ALTOMEDIA/release/ | tee -a "$LOG"
echo "RELEASE BUILD OK"
