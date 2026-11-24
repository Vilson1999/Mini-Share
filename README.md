<div align="center">
  <table style="width:100%; border:0; border-radius:20px; background:linear-gradient(135deg, #172554 0%, #4f46e5 52%, #0891b2 100%);">
    <tr>
      <td align="center" style="border:0; padding:32px 16px; color:#ffffff;">
        <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Mini Share app icon" width="96" height="96" />
        <h1 style="color:#ffffff; margin:12px 0 0;">Mini Share</h1>
        <p style="margin:8px 0 0;"><strong>Android</strong> · Kotlin</p>
      </td>
    </tr>
  </table>
</div>

<p align="center">
  <strong>Developer:</strong> Vilson Sonkar<br />
  <strong>Platform:</strong> Android<br />
  <strong>Language:</strong> Kotlin
</p>

<p align="center">Ultra-light, offline peer-to-peer file sharing over Wi-Fi Direct and Bluetooth discovery.</p>

## App Details

### Send
Choose files on your device, discover nearby peers, and send selected files directly to a connected device.

### Receive
Make your device visible to nearby Mini Share peers and prepare to receive incoming files. Received files are saved to the **Downloads/Mini Share** folder when supported by the device.

### Transfer
Track transfer progress, speed, and status as files move. Transfers use a direct socket connection between Wi-Fi Direct peers and support cancellation.

### Wi-Fi Direct
Discover nearby devices and connect without a router or internet connection to create a local peer-to-peer link for sharing.

### P2P Sharing
Exchange files directly between connected devices instead of uploading them to a cloud service. Bluetooth discovery helpers are included in the app.

## Screenshots

<table>
  <tr>
    <td><img src="public/assets/aistudio/Screenshot_20261113-092221_4c35ff80.png" alt="Mini Share screenshot 1" width="120" /></td>
    <td><img src="public/assets/aistudio/Screenshot_20261113-092234_f7fdad1b.png" alt="Mini Share screenshot 2" width="120" /></td>
    <td><img src="public/assets/aistudio/Screenshot_20261115-220244_65ec2d17.png" alt="Mini Share screenshot 3" width="120" /></td>
    <td><img src="public/assets/aistudio/Screenshot_20261122-210849_edb7dd6c.png" alt="Mini Share screenshot 4" width="120" /></td>
    <td><img src="public/assets/aistudio/Screenshot_20261122-211936_2b4c466a.png" alt="Mini Share screenshot 5" width="120" /></td>
    <td><img src="public/assets/aistudio/Screenshot_20261122-211949_21eb1aad.png" alt="Mini Share screenshot 6" width="120" /></td>
    <td><img src="public/assets/aistudio/screenshot-home.svg" alt="Mini Share home screen with Send and Receive options" width="120" /></td>
    <td><img src="public/assets/aistudio/screenshot-searching.svg" alt="Mini Share searching screen" width="120" /></td>
    <td><img src="public/assets/aistudio/screenshot-receive.svg" alt="Mini Share receive screen" width="120" /></td>
  </tr>
</table>

## Technology

- **Primary language:** Kotlin
- **UI:** Jetpack Compose and Material 3
- **Platform:** Android
- **Connectivity:** Android Wi-Fi Direct APIs and Bluetooth discovery helpers
- **Minimum Android version:** Android 9 (API 28)
- **Target Android version:** Android 13+ build target (API 36)
- **Package:** `com.minishare.vilson`
- **App icon:** Custom adaptive launcher icon with a two-device sharing mark

## Getting Started

1. Clone this repository and open it in Android Studio.
2. Allow Gradle to sync the project.
3. Connect an Android device that supports Wi-Fi Direct.
4. Build and run the `app` configuration.
5. Grant the requested nearby-device and file-access permissions when prompted. Enable Wi-Fi and use the app's **Send** or **Receive** flow to find a peer.

## Project Structure

```text
app/src/main/java/com/example/
├── model/       # Transfer data models
├── p2p/         # Wi-Fi Direct management and Bluetooth discovery
├── transfer/    # File transfer engine
├── ui/          # Compose screens and components
└── viewmodel/   # App state and UI coordination
```

## Build Configuration

The Android app uses a minimum SDK of 28 and a target SDK of 36. See `app/build.gradle.kts` for the complete Android build and dependency configuration.

## License

This project is licensed under the **GNU General Public License v3.0**. See [LICENSE](LICENSE) for details.
