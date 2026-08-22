# 📲 How to get the Aegis APK onto your phone

**Aegis is a native Android app** — it truly blocks apps (Accessibility Service) and shows a
full-screen **"Blocked by Aegis"** wall when a blocked app is opened. Because a real APK must be
compiled with the Android SDK, you build it once and install it. Pick whichever path fits you.

⚠️ **Netlify cannot host an APK-that-blocks-apps.** Netlify serves websites, and websites can't
touch other apps on the phone. The APK below is the version that actually blocks.

---

## ✅ Option 1 — Build the APK in the cloud (no PC setup needed) — RECOMMENDED

This uses **GitHub Actions** (free) to compile the APK for you, then you download it.

1. Create a free account at **github.com**.
2. Click **New repository** → name it `aegis-blocker` → **Create**.
3. Upload this whole project folder:
   - On the new repo page click **"uploading an existing file"**.
   - Drag in **all files/folders** from the `AppBlocker` folder (including the hidden
     `.github` folder — it holds the build recipe). If drag-drop skips `.github`, use
     GitHub Desktop or `git push` instead (see bottom).
   - Click **Commit changes**.
4. Go to the **Actions** tab → the **"Build Aegis APK"** workflow runs automatically
   (takes ~3–5 min). If it doesn't start, click it → **Run workflow**.
5. When it finishes (green ✓), open the run → scroll to **Artifacts** →
   download **`aegis-blocker-apk`**. Unzip it to get **`app-debug.apk`**.
6. **Transfer that `.apk` to your phone** (email it to yourself, Google Drive, or USB) and open it.

---

## ✅ Option 2 — Build in Android Studio (if you have a computer)

1. Install **Android Studio** (free) from developer.android.com.
2. **Open** the `AppBlocker` folder → let Gradle sync.
3. Plug in your phone (USB debugging on) or use an emulator → press ▶ **Run**.
   - Or menu **Build → Build Bundle(s)/APK(s) → Build APK(s)**, then find the APK it points to.

---

## 📥 Installing the APK on your phone

1. Open the `.apk` file on your phone.
2. Android will warn "install from unknown source" → tap **Settings → allow this source** →
   go back → **Install**. (This is normal for apps outside the Play Store.)
3. Open **Aegis**.

## 🔑 Turn on blocking (one-time permissions — Android requires you to do this manually)

The Home screen has a checklist. Grant these:

1. **Accessibility service** → find **"Aegis App Blocker"** → turn ON.
   *(This is what detects when a blocked app opens. Android won't let any app enable this for
   you — it's a security rule.)*
2. **Display over other apps** → allow. *(Lets the "Blocked by Aegis" screen appear on top.)*
3. **Notifications** → allow. *(Block/unblock alerts.)*

---

## 🕒 Setting up your schedules (like your example)

Go to the **Schedule** tab → **＋** and create windows, e.g.:

| Name | Start | End | Days | Apply to |
|---|---|---|---|---|
| **Morning** | 6:00 AM | 7:00 AM | Every day | Instagram, YouTube… |
| **Night time** | 6:00 PM | 8:00 PM | Every day | Instagram, YouTube… |

You can add **as many schedules as you want**, each with its own name, time window, days, and
list of blocked apps. Mark an app **"Always block"** in the Blocklist to block it 24/7.

Now, during a schedule window, opening a blocked app shows the **"Blocked by Aegis"** wall and
sends you back home, plus a notification. 🛡️

---

## Advanced: push with git instead of drag-drop
```bash
cd AppBlocker
git init && git add -A && git commit -m "Aegis blocker"
git branch -M main
git remote add origin https://github.com/YOUR_NAME/aegis-blocker.git
git push -u origin main
```
Then follow Option 1 from step 4.
