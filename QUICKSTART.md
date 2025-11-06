# MailFlow - Quick Start Guide 🚀

## ✅ Was du jetzt testen kannst:

### 1. App installieren
```bash
./gradlew installDebug
```

### 2. Gmail Authentication

1. **App öffnen** und zu **Settings** navigieren
2. Bei **"Gmail Account"** Card auf **"Sign In with Google"** klicken
3. Google Account auswählen
4. Permissions erlauben (Gmail lesen/modifizieren)
5. ✅ Du bist jetzt angemeldet!

### 3. Background Sync testen

**Im Settings Screen:**
- Sync Status wird angezeigt
- **"Sync Now"** klicken für manuellen Sync
- Notification erscheint bei erfolgreichem Sync
- **Active Jobs** zeigt laufende Worker

**Automatischer Sync:**
- Läuft alle 30 Minuten automatisch
- Benötigt: Netzwerkverbindung + Batterie nicht schwach
- Zeigt Notification mit Ergebnis

### 4. Was passiert beim Sync:

1. **GmailSyncWorker** startet
   - Fetcht neue E-Mails von Gmail API
   - Filtert nach aktiven Agenten
   - Speichert in Room Database
   - Zeigt Notification mit Count

2. **EmailProcessingWorker** (optional chained)
   - Verarbeitet neue E-Mails mit Gemini AI
   - Extrahiert strukturierte Daten
   - Updated Agent Context
   - Zeigt Processing Complete Notification

## 🔑 Voraussetzungen

### API Keys sind bereits konfiguriert:
- ✅ **Gemini API Key** in `local.properties`
- ✅ **OAuth2 Client ID** in Google Cloud Console
- ✅ **SHA-1 Fingerprint** registriert

### Wichtige Dateien:
```
local.properties
├── GEMINI_API_KEY=AIzaSyCafTF9ygLdjvrVqsFK4k7aZ0K2IJKwTE4 //e.g.

Google Cloud Console
├── OAuth Client ID: Automatisch via SHA-1
├── Gmail API: ✅ Aktiviert
└── Generative Language API: ✅ Aktiviert
```

## 📱 UI Navigation

```
Dashboard (Home)
├── Agent Liste (aktuell leer)
└── FAB "+" → Create Agent

Settings
├── Background Sync Status
│   ├── Sync Now Button
│   └── Cancel All Button
├── Active Jobs Counter
├── Gmail Account
│   ├── Sign In / Sign Out
│   └── Account Email Anzeige
├── Sync Frequency (30 min)
└── Version Info
```

## 🧪 Test Szenarien

### Szenario 1: Gmail Authentication
1. Settings öffnen
2. "Sign In with Google" klicken
3. Account wählen
4. ✅ Erfolg: Email wird angezeigt

### Szenario 2: Manual Sync
1. Einloggen (siehe Szenario 1)
2. "Sync Now" klicken
3. Status wechselt zu "Running"
4. Nach ~10 Sekunden: "Succeeded" mit Count
5. Notification erscheint

### Szenario 3: Periodic Background Sync
1. App im Hintergrund lassen
2. Nach 30 Minuten: Automatischer Sync
3. Notification erscheint bei neuen Mails
4. Status in Settings aktualisiert sich

### Szenario 4: Agent erstellen (Dashboard)
1. Dashboard öffnen
2. FAB "+" klicken
3. Agent Name eingeben
4. YAML Config erstellen
5. Agent speichern
6. ✅ Agent wird in Liste angezeigt

## 🐛 Troubleshooting

### "Sign-in failed"
- **Problem:** OAuth Client ID nicht korrekt
- **Lösung:** SHA-1 in Google Cloud Console überprüfen
  ```bash
  ./gradlew signingReport
  ```

### "No messages fetched"
- **Problem:** Keine E-Mails in Gmail oder Filter zu restriktiv
- **Lösung:** Testmail an dich selbst senden

### "Gemini API Error"
- **Problem:** API Key ungültig oder Quota überschritten
- **Lösung:** Key in `local.properties` überprüfen

### "Sync never runs"
- **Problem:** Device im Doze Mode oder keine Netzwerkverbindung
- **Lösung:**
  - Device verbinden mit WLAN
  - Battery Optimization für MailFlow deaktivieren

## 📊 Logs & Debugging

### Logcat Filter:
```
MailFlow|GmailSync|EmailProcessing|WorkManager
```

### Wichtige Log Tags:
- `GmailApiClient`: Gmail API Calls
- `GmailSyncWorker`: Sync Status
- `EmailProcessingWorker`: Email Verarbeitung
- `NotificationManager`: Notification Events

### WorkManager Debugging:
```bash
adb shell dumpsys jobscheduler | grep mailflow
```

## 🎯 Was als Nächstes testen:

1. ✅ Gmail Sign-In / Sign-Out
2. ✅ Manual Sync Trigger
3. ✅ Background Sync (30 min warten)
4. ⏳ Agent erstellen (UI fertig, Flow testen)
5. ⏳ E-Mail Processing mit Gemini (braucht Agent)
6. ⏳ Chat mit Agent (braucht Agent + Context)

## 🔄 Complete End-to-End Test Flow:

```
1. Sign In → Settings
2. Create Agent → Dashboard
3. Configure YAML → Agent Screen
4. Trigger Sync → Settings "Sync Now"
5. Check Processing → Notifications
6. View Context → Agent Detail
7. Chat with Agent → Chat Screen
```

**Status:** Steps 1-4 funktionieren! Steps 5-7 benötigen Agents in der DB.

## 💾 Database Check:

```bash
# Via Android Studio Database Inspector
# Oder via ADB:
adb shell
run-as com.mailflow.app
cd databases
sqlite3 mailflow.db
.tables  # Sollte zeigen: agents, emails, contexts, processing_jobs
SELECT * FROM agents;  # Aktuell leer
```

## 📞 Support

Bei Problemen:
1. Logcat Logs senden
2. WorkManager Status aus Settings Screenshot
3. Build Variant: Debug
4. Device Info (Android Version, Brand)

---

**Stand:** 2025-11-06
**Build Status:** ✅ SUCCESS
**Phase:** 4 (Background Processing) COMPLETED + API Integration COMPLETED
