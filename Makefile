.PHONY: setup build install

# Build a debug APK
build:
	./gradlew assembleDebug

# Build and Install a debug APK on a connected device
install:
	./gradlew installDebug

install-apk:
	adb install app/build/outputs/apk/debug/app-debug.apk

list-emulators:
	/opt/ext/android-sdk/emulator/emulator  -list-avds

start-emulator:
	/opt/ext/android-sdk/emulator/emulator -avd Pixel3_API_25_N_7.1_no_store

run:
	adb shell am start -n igrek.todotree/.activity.MainActivity

