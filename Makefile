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

.PHONY: build test deploy shot logs check-host screens screens-app screens-design

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
