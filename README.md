# 📱 React Native Nitro Advertising ID [![npm version](https://img.shields.io/npm/v/react-native-nitro-advertising-id.svg)](https://www.npmjs.com/package/react-native-nitro-advertising-id)

🔹 **React Native Nitro Advertising ID** provides seamless access to the device's advertising identifier (IDFA on iOS, GAID on Android) in your React Native applications. Built with the power of Nitro Modules for high performance.

---

## 🚀 Features

- **📲 Cross-Platform Support**: Works on both iOS (IDFA) and Android (GAID).
- **⚡ Optimized Performance**: Built with Nitro for low-latency native calls.
- **🔒 Privacy Compliant**: Handles permission requests via App Tracking Transparency (iOS) and Google Play Services (Android).
- **🔄 Simple API**: Just two methods — request permission and get the advertising ID.

---

## 🛠️ Prerequisites

Before getting started, ensure you have:

- React Native version `0.75+`
- [`react-native-nitro-modules`](https://www.npmjs.com/package/react-native-nitro-modules)
- **iOS 14+** (App Tracking Transparency support)
- New architecture support

---

## 📦 Installation

Run the following command to install the package:

```bash
npm install react-native-nitro-advertising-id react-native-nitro-modules

# or

yarn add react-native-nitro-advertising-id react-native-nitro-modules
```

### iOS Setup

```bash
cd ios && pod install
```

Edit **`Info.plist`**. Add the following item (Set **Value** as desired):

| Key | Type | Value |
| --- | ---- | ----- |
| _Privacy - NSUserTrackingUsageDescription_ | `String` | _CHANGEME: This app needs access to your advertising identifier to provide personalized ads._ |

### Android Setup

No additional setup required. The library uses Google Play Services Ads Identifier which is automatically linked.

---

## 🎯 API Reference

### 📜 Permission Statuses

Permission checks and requests resolve into one of the following statuses:

| **Return Value** | **Description** |
| --- | --- |
| **`"authorized"`** | The user has granted tracking permission. |
| **`"denied"`** | The user has denied tracking permission. |
| **`"restricted"`** | Tracking is restricted (e.g., parental controls). |
| **`"notDetermined"`** | The user has not been asked for permission yet (iOS). |
| **`"granted"`** | Permission has been granted (Android). |
| **`"undetermined"`** | Permission status has not been determined yet (Android). |
| **`"unknown"`** | The permission status could not be determined. |

---

## 📌 Example Usage

### 🔄 Requesting Permission

Request tracking permission from the user before accessing the advertising ID:

```ts
import { NitroAdvertisingIdHybridObject } from 'react-native-nitro-advertising-id';

const requestPermission = async () => {
  try {
    const status = await NitroAdvertisingIdHybridObject.requestPermission();
    console.log('Permission status:', status);
  } catch (error) {
    console.error('Error requesting permission:', error);
  }
};
```

### 📲 Getting the Advertising ID

Retrieve the device's advertising identifier:

```ts
import { NitroAdvertisingIdHybridObject } from 'react-native-nitro-advertising-id';

const getAdId = () => {
  try {
    const advertisingId = NitroAdvertisingIdHybridObject.getAdvertisingId();
    console.log('Advertising ID:', advertisingId);
  } catch (error) {
    console.error('Error getting advertising ID:', error);
  }
};
```

---

## 🤝 Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## 📄 License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
