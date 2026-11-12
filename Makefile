JAVA_HOME ?= $(HOME)/.local/jdk-17
ANDROID_HOME ?= $(HOME)/Android/Sdk
export JAVA_HOME
export ANDROID_HOME
export ANDROID_SDK_ROOT := $(ANDROID_HOME)
export PATH := $(JAVA_HOME)/bin:$(ANDROID_HOME)/platform-tools:$(PATH)

TV_HOST := $(shell sed -n 's/^tv\.host=//p' local.properties 2>/dev/null)
TV := $(TV_HOST):5555
APK := app/build/outputs/apk/debug/app-debug.apk
PKG := co.jp.tv

.PHONY: build test deploy aot startup shot logs check-host screens screens-app screens-design

build:
	./gradlew :app:assembleDebug

test:
	./gradlew :app:testDebugUnitTest

check-host:
	@test -n "$(TV_HOST)" || { echo "Missing tv.host in local.properties (e.g. tv.host=192.168.1.4)"; exit 1; }

deploy: check-host build
	adb connect $(TV)
	adb -s $(TV) install -r $(APK)
	-adb -s $(TV) shell pm grant $(PKG) android.permission.READ_EXTERNAL_STORAGE
	adb -s $(TV) shell cmd package set-home-activity $(PKG)/.MainActivity
	adb -s $(TV) shell input keyevent KEYCODE_HOME

# After a deploy and one launch (ProfileInstaller writes the profile in the background), AOT-compile the
# app with it so the next cold start skips the JIT warm-up (Android 9 does not do it on install).
aot: check-host
	adb -s $(TV) shell cmd package compile -f -m speed-profile $(PKG)

# Cold start timeline: JpStartup marks (debug build) and the system's own start/fully-drawn times.
startup: check-host
	adb -s $(TV) shell am force-stop $(PKG)
	adb -s $(TV) logcat -c
	adb -s $(TV) shell am start -W -n $(PKG)/.MainActivity
	sleep 8
	adb -s $(TV) logcat -d -s JpStartup ActivityTaskManager:I ActivityManager:I | rg 'JpStartup|Fully drawn|Displayed'

shot: check-host
	@mkdir -p shots
	@f=shots/$$(date +%Y%m%d-%H%M%S).png; adb -s $(TV) exec-out screencap -p > $$f && echo "saved $$f"

logs: check-host
	adb -s $(TV) logcat --pid=$$(adb -s $(TV) shell pidof $(PKG))

# Off-device loop: no TV needed. Renders to screens/ (gitignored); design references to screens/design/.
screens: screens-app screens-design

screens-app:
	@mkdir -p screens
	./gradlew :app:testDebugUnitTest -Pscreens -Proborazzi.test.record=true --rerun-tasks --tests 'co.jp.tv.screens.*'

screens-design:
	tools/design-shots/render.sh
