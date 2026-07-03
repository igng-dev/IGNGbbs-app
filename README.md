# IGNGBBS Android

Android client for IGNGBBS.

## Project Layout

- `Android/`: Android Studio / Gradle project

## Requirements

- Android Studio with a local Android SDK
- JDK 11

## Local Setup

1. Open `Android/` in Android Studio.
2. Let Android Studio generate `local.properties` for your SDK path.
3. Sync Gradle and run the app.

## Build

Debug build:

```bash
cd Android
./gradlew assembleDebug
```

On Windows:

```powershell
cd Android
.\gradlew.bat assembleDebug
```

## Runtime Configuration

The app reads service endpoints from Gradle properties and falls back to the
public production endpoints:

- `IGNG_BBS_BASE_URL`
- `IGNG_SSO_BASE_URL`

Example:

```powershell
cd Android
.\gradlew.bat assembleDebug -PIGNG_BBS_BASE_URL=https://www.igngbbs.net -PIGNG_SSO_BASE_URL=https://sso.igng.net
```

## Release Signing

Release builds use a local keystore stored inside the project folder for backup
and long-term app update compatibility. The keystore and signing properties are
kept out of Git on purpose.

- Keystore path: `Android/signing/release.keystore`
- Local config: `Android/release-signing.properties`

Expected properties:

```properties
storeFile=signing/release.keystore
storePassword=your-store-password
keyAlias=release
keyPassword=your-key-password
```

Build a signed release APK:

```powershell
cd Android
.\gradlew.bat assembleRelease
```

## Repository Hygiene

- Local SDK paths, build outputs, packaged APK/AAB files, signing files, and
  private deployment notes are intentionally excluded from version control.
- Private infrastructure credentials and LAN deployment details are not part of
  this public repository.

## License

MIT
