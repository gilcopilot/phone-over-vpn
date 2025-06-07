# OpenVPN SMS Handler Android App

This is an Android app that allows you to handle SMS and call requests via HTTP, and manage a VPN connection using OpenVPN. Built with Jetpack Compose and Kotlin.

## Features
- Connect/disconnect to OpenVPN server
- Receive HTTP requests to send SMS or initiate calls
- View logs and statistics in the app
- Modern Material3 UI

## Requirements
- Android Studio (Giraffe or newer recommended)
- Android SDK 34+
- Internet connection

## Setup
1. Open the `android-app` folder in Android Studio.
2. Sync Gradle to download dependencies.
3. Build and run the app on an emulator or device (API 24+).

## Permissions
The app requests the following permissions:
- SEND_SMS
- READ_SMS
- CALL_PHONE
- READ_PHONE_STATE
- INTERNET
- ACCESS_NETWORK_STATE

## HTTP API
The app starts a local HTTP server on port 8080. You can send POST requests to it with the following JSON body:

```
{
  "type": "SMS", // or "CALL"
  "phone_number": "+1234567890",
  "message": "Hello!" // Only for SMS
}
```

## Notes
- The OpenVPN client is OpenVPN library integration.
- The app must be running in the foreground to receive HTTP requests.

## License
MIT
