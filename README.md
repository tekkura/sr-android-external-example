# abcvlib external consumer

This minimal Android app consumes `abcvlib` as a public JitPack dependency. It uses
`OrientationData` to display the phone's live pitch; a checkout of the main
`sr-android` repository and robot hardware are not required.

## Requirements

- JDK 17
- Android SDK 36
- Android device running API 30 or newer for the live orientation example

## Dependency setup

Add JitPack to the repositories in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

This project records the public dependency in `gradle/libs.versions.toml`:

```toml
[versions]
smartphoneRobotAndroid = "v2.0.2"

[libraries]
smartphone-robot-android = { module = "com.github.oist:smartphone-robot-android", version.ref = "smartphoneRobotAndroid" }
```

The app consumes that dependency in `app/build.gradle.kts`:

```kotlin
implementation(libs.smartphone.robot.android)
```

TensorFlow Lite 2.15 publishes its API and GPU AARs with the same namespace. Android
Gradle Plugin 9 checks dependency namespaces for uniqueness by default, so this project
uses its compatibility flag in `gradle.properties`:

```properties
android.uniquePackageNames=false
```

This keeps the TensorFlow Lite GPU runtime available while consuming `abcvlib`. The
flag can be removed after the upstream TensorFlow dependencies use unique namespaces.
No GitHub Packages credentials or local Maven publication is needed.

## Build and run

On macOS or Linux:

```shell
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

Install the generated `app/build/outputs/apk/debug/app-debug.apk` on a compatible
phone. The app displays the pitch reported by `abcvlib` as the phone is tilted.
