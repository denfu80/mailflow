# MailFlow - Quick Start Guide (Todo-Extractor Version) 🚀

## ✅ Was du testen kannst:

### 1. App installieren
```bash
./gradlew installDebug
```

### 2. Gmail Authentication & Konfiguration

1.  **App öffnen** und zu **Settings** navigieren.
2.  Bei der **"Gmail Account"**-Karte auf **"Sign In with Google"** klicken und den Anweisungen folgen.
3.  Im Feld **"Todo List Name"** den Namen der Zielliste für deine Todos eintragen (z.B. "Inbox").
4.  ✅ Du bist jetzt startklar!

### 3. End-to-End Test: Von der E-Mail zum Todo

1.  **Sende eine E-Mail an dich selbst**, die eine klare Aufgabe enthält. Zum Beispiel:
    *   **Betreff:** "Rechnung"
    *   **Inhalt:** "Bitte denk daran, die Stromrechnung bis Freitag zu bezahlen."

2.  **Synchronisiere die Mails:**
    *   Gehe zum **"Activity Log"** (Hauptbildschirm).
    *   Klicke auf den **"Sync Now"**-Button.

3.  **Überprüfe das Ergebnis:**
    *   Die App zeigt den Status des Syncs an.
    *   Nach kurzer Zeit sollte eine Benachrichtigung erscheinen: **"Neues Todo 'Stromrechnung bezahlen' erstellt."**
    *   Ein neuer Eintrag erscheint im Activity Log.
    *   **Überprüfe deine externe Todo-Anwendung:** In der Liste, die du konfiguriert hast, sollte ein neues Todo erschienen sein.

### 4. Was passiert beim Sync:

1.  **GmailSyncWorker** startet und sucht nach neuen, ungelesenen E-Mails.
2.  **EmailProcessingWorker** wird für jede neue E-Mail gestartet.
    -   Die E-Mail wird an die **Gemini KI** gesendet mit der Anweisung, Aufgaben zu extrahieren.
    -   Wenn eine Aufgabe gefunden wird, ruft die App deine **externe Todo-API** auf.
    -   Eine **Benachrichtigung** wird angezeigt.

## 🔑 Voraussetzungen

- ✅ **Gemini API Key** ist in `local.properties` konfiguriert.
- ✅ **OAuth2 Client ID** für die App ist in der Google Cloud Console korrekt eingerichtet.
- ✅ **(Für den Test):** Deine externe Todo-App muss über das Internet erreichbar sein und die API muss implementiert sein. In der Entwicklungsphase wird ein Dummy-Client verwendet, der die Aktionen nur loggt.

## 📱 UI Navigation

```
Activity Log (Hauptbildschirm)
├── Liste der letzten Sync-Aktivitäten
└── "Sync Now" Button

Settings
├── Gmail Account
│   ├── Sign In / Sign Out Button
│   └── E-Mail-Adresse des angemeldeten Kontos
├── Todo List Name (Eingabefeld)
└── Background Sync Status
```

## 🐛 Troubleshooting

### "Sign-in failed"
- **Problem:** Die SHA-1 Signatur der App stimmt nicht mit der in der Google Cloud Console hinterlegten überein.
- **Lösung:** Den korrekten SHA-1-Wert mit `./gradlew signingReport` ermitteln und in der Cloud Console eintragen.

### "No messages fetched" oder "Keine Todos erstellt"
- **Problem 1:** Es gibt keine neuen, ungelesenen E-Mails in deinem Gmail-Posteingang.
- **Lösung:** Sende eine neue Test-E-Mail an dich selbst.
- **Problem 2:** Die KI konnte keine klare Aufgabe in der E-Mail identifizieren.
- **Lösung:** Formuliere die Aufgabe in der Test-E-Mail klarer (z.B. "Erinnere mich daran, ...", "Aufgabe: ...").

### "API Error"
- **Problem:** Die App kann deine externe Todo-API nicht erreichen oder die Anfrage schlägt fehl.
- **Lösung:** Überprüfe die Logcat-Logs auf Netzwerkfehler. Stelle sicher, dass der API-Endpunkt korrekt und erreichbar ist.

## 📊 Logs & Debugging

### Logcat Filter:
```
MailFlow|GmailSync|EmailProcessing|TodoApiClient
```

### Wichtige Log Tags:
- `GmailSyncWorker`: Status der E-Mail-Synchronisation.
- `EmailProcessingWorker`: Status der KI-Verarbeitung.
- `TodoApiClient`: Zeigt die Anfragen an deine externe API (Request & Response).
- `NotificationManager`: Zeigt Benachrichtigungs-Events an.
