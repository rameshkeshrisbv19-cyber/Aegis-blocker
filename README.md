# 🛡️ Aegis Blocker

A modern, native **Android app & website blocker** built with **Kotlin + Jetpack Compose**.
It blocks apps using an **Accessibility Service** (no root), blocks websites through a **local
VPN DNS filter**, runs everything on a **timetable/schedule**, and fires **notifications** when
things get blocked or unblocked — all wrapped in a unique dark, animated "deep-space violet" UI.

---

## ✨ Features

| Feature | How it works |
|---|---|
| **Block any app** | An Accessibility Service watches the foreground app. If it's on your blocklist right now, Aegis sends you home and shows a full-screen animated block wall. |
| **Block websites** | A local `VpnService` inspects outgoing DNS queries and drops the ones resolving blocked domains — no traffic leaves your device. |
| **Timetable / schedule** | Create windows like *"Study — 9:00 AM to 5:00 PM, weekdays"* and attach any apps/sites. Supports windows that cross midnight. |
| **Always-block** | Flag a target to be blocked 24/7, ignoring schedules. |
| **Notifications** | High-priority alerts on **block** ("🛡️ Blocked Instagram") and **unblock** ("✅ Unblocked Instagram"). Exact alarms fire the transitions precisely. |
| **Activity feed** | A running log of every block/unblock event. |
| **Clean, unique UI** | Jetpack Compose, glassmorphism cards, sweep-gradient hero, pulsing block screen, animated tab transitions. |
| **Survives reboot** | A `BootReceiver` re-arms schedule alarms after restart. |

---

## 🏗️ Project structure

```
app/src/main/java/com/aegis/appblocker/
├─ MainActivity.kt              # Compose entry + bottom-nav scaffold
├─ AppBlockerApp.kt             # Application; creates notification channels
├─ data/                        # Room DB, entities, DAO, repository + schedule logic
├─ service/
│  ├─ AppBlockAccessibilityService.kt   # Detects & blocks foreground apps
│  ├─ WebBlockVpnService.kt             # Local VPN DNS filter for websites
│  └─ DnsParser.kt                      # Extracts hostname from DNS packets
├─ block/BlockScreenActivity.kt # Full-screen animated "blocked" wall
├─ schedule/                    # Exact-alarm scheduling, boot receiver, diff logic
├─ ui/                          # ViewModel, theme, shared components
│  └─ screens/                  # Home, Blocklist, Schedule, Activity
└─ util/                        # Notifications + permission helpers
```

---

## ▶️ How to build & run

You need **Android Studio** (Hedgehog or newer) and an Android device/emulator (min SDK 24).

1. **Open the project**
   - Android Studio → *Open* → select the `AppBlocker` folder.
   - Let Gradle sync (it uses the included wrapper, Gradle 8.7).
   - Android Studio auto-creates `local.properties` with your SDK path. (See
     `local.properties.example` if you build from the command line.)

2. **Build & install**
   - Press ▶ **Run**, or from a terminal:
     ```bash
     ./gradlew installDebug      # macOS/Linux
     gradlew.bat installDebug    # Windows
     ```

3. **Grant permissions in-app** (the Home screen has a checklist):
   - **Accessibility** → enable "Aegis App Blocker" (required to block apps).
   - **Display over other apps** → allow (so the block wall appears on top).
   - **Notifications** → allow when prompted (Android 13+).
   - **Website filter** toggle → approve the VPN connection request.

4. **Use it**
   - **Blocklist** tab → add apps or websites (mark "always block" if you want).
   - **Schedule** tab → create timetable windows and attach targets.
   - Try opening a blocked app during an active window → you'll be blocked + notified.

---

## ⚠️ Important notes & Android limitations

- **No root required.** App blocking uses the Accessibility API, the standard Play-Store-friendly
  approach. Users must manually enable the service (Android security requirement — it cannot be
  auto-enabled).
- **The VPN DNS filter is intentionally compact/educational.** It drops DNS queries for blocked
  domains (which blocks most sites) but does not do full packet forwarding/caching. For a
  production filter, extend `WebBlockVpnService` to forward non-blocked UDP/TCP traffic through a
  protected socket, and cache resolutions. Modern apps using DoH/DoT may bypass DNS filtering.
- **Play Store policy:** apps using `QUERY_ALL_PACKAGES` and Accessibility for blocking must
  declare their use; digital-wellbeing/parental-control is an accepted use case. `SYSTEM_ALERT_WINDOW`
  + Accessibility is standard for this category.
- **Battery optimization:** for rock-solid schedule alarms, ask users to exempt the app from
  battery optimization (can be added as another checklist item).

---

## 🎨 Design language

- **Palette:** deep-space background `#0A0E1A`, violet primary `#6C63FF`, cyan `#00E0C7`,
  purple accent `#9D4EDD`, danger rose `#FF5C7A`.
- **Motion:** infinite sweep-gradient ring on the hero, pulsing shield on the block screen,
  animated tab crossfades, `animateContentSize` on cards.
- **Components:** reusable frosted `GlassCard`, gradient action buttons, pill badges.

Built as a complete, buildable Android Studio project. Enjoy guarding your attention! 🛡️
