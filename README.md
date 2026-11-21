# Instant Colour Clicker (Android Studio project)

This is a starter Android project, **not a prebuilt APK**. It uses Android's Accessibility screenshot and gesture APIs (Android 11 / API 30+).

## Build
1. Install Android Studio on a computer.
2. Open this folder in Android Studio and let Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Android Studio will show the generated APK location.

## Use
1. Install the APK on your Android phone.
2. Open the app and tap **Open Accessibility Settings**.
3. Enable **Instant Colour Clicker**.
4. Return to the app and tap **START AUTO CLICK**.
5. Switch to the screen you want to test. Open the app again and tap **STOP** to pause.

## Important limitations
- This first version estimates the centre of all bright non-black pixels on the screen. It may tap the wrong place if there are other bright UI elements, text, or buttons.
- It is not a precision game bot and does not guarantee a specific reaction time.
- Android may show a system warning because Accessibility access is powerful. Only enable it for this app if you understand and accept the permission.
- Use only on apps/screens you own or have permission to automate. Do not use it for payments, purchases, security prompts, or other sensitive actions.
- For a reliable version, the target area and colour threshold need to be tuned to the actual app/game.
