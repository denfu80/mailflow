# Google Cloud Console Setup Guide

## Schritt 1: Google Cloud Projekt erstellen

1. Öffne [Google Cloud Console](https://console.cloud.google.com)
2. Klicke oben links auf das Projekt-Dropdown
3. Klicke auf "Neues Projekt"
4. Name: `MailFlow`
5. Klicke "Erstellen"

## Schritt 2: Gmail API aktivieren

1. Im Suchfeld oben: "Gmail API"
2. Klicke auf "Gmail API"
3. Klicke "Aktivieren"

## Schritt 3: Generative Language API (Gemini) aktivieren

1. Im Suchfeld: "Generative Language API"
2. Klicke auf "Generative Language API"
3. Klicke "Aktivieren"

## Schritt 4: OAuth 2.0 Consent Screen konfigurieren

1. Links im Menü: "APIs & Services" → "OAuth-Zustimmungsbildschirm"
2. Wähle: **Extern** (für Tests außerhalb deiner Organisation)
3. Klicke "Erstellen"

### App-Informationen:
- **App-Name**: MailFlow
- **Nutzer-Support-E-Mail**: Deine E-Mail
- **App-Logo**: Optional
- **Autorisierte Domains**: Leer lassen für jetzt
- **Entwickler-Kontakt**: Deine E-Mail

4. Klicke "Speichern und fortfahren"

### Scopes hinzufügen:
5. Klicke "Scopes hinzufügen oder entfernen"
6. Suche und wähle folgende Scopes:
   - `https://www.googleapis.com/auth/gmail.readonly` (Mails lesen)
   - `https://www.googleapis.com/auth/gmail.modify` (Mails modifizieren)
   - Optional: `https://www.googleapis.com/auth/gmail.send` (Mails senden)

7. Klicke "Aktualisieren" → "Speichern und fortfahren"

### Testnutzer hinzufügen:
8. Füge deine Gmail-Adresse als Testnutzer hinzu
9. Klicke "Speichern und fortfahren"

## Schritt 5: SHA-1 Fingerprint für Android abrufen

Im Terminal (im Projekt-Verzeichnis):

```bash
# Debug Keystore SHA-1 (für Entwicklung)
./gradlew signingReport
```

**Kopiere den SHA-1 Fingerprint aus dem Output:**
```
Variant: debug
Config: debug
Store: /Users/YOUR_USER/.android/debug.keystore
Alias: AndroidDebugKey
MD5: XX:XX:...
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD  ← DIESEN!
SHA-256: ...
```

## Schritt 6: OAuth 2.0 Client ID erstellen

1. Links im Menü: "APIs & Services" → "Anmeldedaten"
2. Klicke oben: "+ Anmeldedaten erstellen" → "OAuth-Client-ID"
3. Anwendungstyp: **Android**

### Android-Konfiguration:
- **Name**: MailFlow Android Debug
- **Paketname**: `com.mailflow.app`
- **SHA-1-Zertifikatsfingerabdruck**: (Den kopierten SHA-1 einfügen)

4. Klicke "Erstellen"

### Für Release Version (später):
Wiederhole Schritt 6 mit:
- **Name**: MailFlow Android Release
- **SHA-1**: Release Keystore SHA-1 (wenn du einen Release Key hast)

## Schritt 7: Gemini API Key erstellen

1. Öffne [Google AI Studio](https://aistudio.google.com/apikey)
2. Klicke "Create API Key"
3. Wähle dein Projekt: **MailFlow**
4. Kopiere den API Key

**WICHTIG**: Speichere den Key sicher! Er wird nur einmal angezeigt.

## Schritt 8: Gemini API Key in local.properties speichern

Öffne `local.properties` im Projekt-Root und füge hinzu:

```properties
sdk.dir=/Users/I526162/Library/Android/sdk
GEMINI_API_KEY=dein_gemini_api_key_hier
```

**WICHTIG**: `local.properties` ist in `.gitignore` und wird NICHT committed!

## Schritt 9: Gmail OAuth - Keine weitere Konfiguration nötig!

✅ **Die OAuth Credentials (Client ID) werden automatisch verwendet!**

Google Play Services findet die richtige Client ID basierend auf:
1. ✅ Paketname: `com.mailflow.app`
2. ✅ App-Signatur (SHA-1): `85:33:23:B1:1C:D6:C5:49:4E:81:0D:0C:B7:44:6C:5D:2D:E9:ED:0A`

**Du musst KEINE Datei (wie `google-services.json`) hinzufügen!**
**Du musst die Client ID NICHT hardcoded in die App einbetten!**

### Für Gmail API brauchst du nur:
- Google Sign-In Library (kommt später in Phase 1)
- Gmail API Client Library (bereits in Dependencies)

## Verifizierung

### Test Gmail API:
```bash
# Im Browser (ersetze CLIENT_ID und YOUR_EMAIL):
https://accounts.google.com/o/oauth2/v2/auth?
  client_id=YOUR_CLIENT_ID.apps.googleusercontent.com&
  redirect_uri=urn:ietf:wg:oauth:2.0:oob&
  response_type=code&
  scope=https://www.googleapis.com/auth/gmail.readonly&
  access_type=offline&
  login_hint=YOUR_EMAIL@gmail.com
```

### Test Gemini API:
```bash
curl "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=YOUR_API_KEY" \
  -H 'Content-Type: application/json' \
  -d '{"contents":[{"parts":[{"text":"Hello Gemini!"}]}]}'
```

## Troubleshooting

### "Access blocked: Authorization Error"
- Prüfe, ob deine E-Mail als Testnutzer hinzugefügt ist
- OAuth Consent Screen Status: "Testing" oder "In production"

### "Invalid SHA-1"
- Stelle sicher, dass du den richtigen SHA-1 verwendest (Debug vs Release)
- Führe `./gradlew signingReport` erneut aus

### "API has not been used in project"
- Warte 1-2 Minuten nach API-Aktivierung
- Stelle sicher, dass das richtige Projekt ausgewählt ist

## Nächste Schritte

Nach erfolgreichem Setup:
1. ✅ Gmail API aktiviert
2. ✅ Gemini API Key erstellt
3. ✅ OAuth Credentials konfiguriert
4. → Weiter mit Phase 1: Data Layer Implementation

## Wichtige Links

- [Google Cloud Console](https://console.cloud.google.com)
- [Google AI Studio](https://aistudio.google.com)
- [Gmail API Docs](https://developers.google.com/gmail/api)
- [Gemini API Docs](https://ai.google.dev/gemini-api/docs)
- [OAuth 2.0 Android](https://developers.google.com/identity/protocols/oauth2/native-app)
