# VPN V2Ray for Android

**VPN V2Ray** is an Android client forked from [v2rayNG](https://github.com/2dust/v2rayNG). It uses the V2RayNG application framework and supports compatible V2Ray/Xray configuration links and subscription URLs. GitHub Actions builds installable **debug APK** artifacts for each commit to `main`.

> This project supplies a client application only. You must provide your own lawful server configuration or subscription. Do not use the software to violate applicable law, network rules, or the rights of others.

## Build an APK with GitHub Actions

Open the repository's **Actions** tab, select **Build VPN V2Ray APK**, and select **Run workflow**. When the run completes, download the `VPNV2Ray-debug-APKs` artifact. It contains universal and ABI-specific installable debug APKs. The universal APK is appropriate for most physical Android devices. Because it is a debug artifact, it is not intended for app-store distribution.

| APK file type | Appropriate device |
| --- | --- |
| `*universal*.apk` | Most Android phones and tablets |
| `*arm64-v8a*.apk` | Modern 64-bit ARM Android devices |
| `*armeabi-v7a*.apk` | Older 32-bit ARM Android devices |
| `*x86_64*.apk` or `*x86*.apk` | Android emulators and compatible Intel devices |

## Local development

Clone the repository with submodules, then open `V2rayNG/` in Android Studio. The workflow automatically installs Java 21, Android SDK Platform 37, Build Tools 37.0.0, NDK 29.0.14206865, the `hev-socks5-tunnel` native libraries, and `libv2ray.aar`.

```bash
git clone --recurse-submodules https://github.com/waigyitinsein-code/VpnV2ray.git
cd VpnV2ray
```

## App identity

| Setting | Value |
| --- | --- |
| App name | VPN V2Ray |
| Android application ID | `com.waigyitinsein.vpnv2ray` |
| Initial fork version | `1.0.0` |
| Minimum Android version | Android 7.0 (API 24) |

## Upstream and licensing

This repository is a fork of [2dust/v2rayNG](https://github.com/2dust/v2rayNG) and retains its source code and GPL-3.0 license. The original upstream README is preserved in [`README_UPSTREAM.md`](README_UPSTREAM.md). The Android core library is downloaded at build time from [2dust/AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite), at the tag referenced by this source tree. Preserve upstream copyright notices, license files, and corresponding source obligations when distributing modified versions.

## Upgrade from upstream

To bring in upstream fixes, add the upstream repository as a remote, merge or rebase deliberately, and review build and license changes before publishing a new APK.

```bash
git remote add upstream https://github.com/2dust/v2rayNG.git
git fetch upstream
git merge upstream/master
```
