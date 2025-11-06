# MailFlow - Complete End-to-End Test Guide 🧪

## ✅ Vollständiger Test Flow (jetzt möglich!)

### Prerequisites
```bash
# Build & Install
./gradlew installDebug

# Voraussetzungen:
- ✅ Gmail Account
- ✅ Gemini API Key in local.properties
- ✅ OAuth2 konfiguriert in Google Cloud
- ✅ Device/Emulator mit Android 8.0+
```

---

## 🔄 Complete User Journey

### **Step 1: Gmail Authentication** (Settings)

1. **App öffnen** → Automatisch auf Dashboard
2. **Settings Icon** (oben rechts) klicken
3. Zu **"Gmail Account" Card** scrollen
4. **"Sign In with Google"** Button klicken
5. **Google Account** auswählen
6. **Permissions** erlauben:
   - ✅ Gmail lesen
   - ✅ Gmail modifizieren
7. **Erfolg**: Email wird angezeigt in Card

**Expected Result:**
```
✅ Status: "Signed in as: your.email@gmail.com"
✅ "Sign Out" Button erscheint
✅ Keine Fehlermeldung
```

---

### **Step 2: Create First Agent** (Dashboard → Create)

1. **Zurück zum Dashboard** (Back Button)
2. **FAB "+"** (unten rechts) klicken
3. **Create Agent Screen** öffnet sich

#### Form ausfüllen:

**Agent Name:**
```
Invoice Processor
```

**Description:**
```
Automatically processes invoice emails and extracts payment information
```

**YAML Configuration:**
```yaml
agent:
  name: "Invoice Processor"
  description: "Extracts invoice data from emails"

filters:
  senders:
    - "invoices@company.com"
  subjects:
    - "Invoice"
    - "Bill"
  hasAttachments: true

gemini_prompt: "Analyze this invoice email. Extract: invoice number, amount, due date, vendor name, and payment instructions. Format as JSON."

context_schema:
  invoice_number: string
  amount: string
  due_date: string
  vendor_name: string
  payment_instructions: string
```

4. **"Create Agent"** Button klicken
5. **Loading Indicator** erscheint kurz
6. **Automatischer Redirect** zurück zum Dashboard

**Expected Result:**
```
✅ Agent Card erscheint in Dashboard
✅ Name: "Invoice Processor"
✅ Description sichtbar
✅ Status: Active (grün)
✅ Keine Fehlermeldung
```

---

### **Step 3: Trigger Manual Sync** (Settings)

1. **Settings** öffnen (Icon oben rechts)
2. Zu **"Background Sync" Card** scrollen
3. **"Sync Now"** Button klicken

**What happens:**
```
1. Status wechselt zu: "Syncing emails..."
2. CircularProgressIndicator erscheint
3. GmailSyncWorker startet
4. Fetcht Mails von Gmail API
5. Filtert nach "Invoice Processor" Agent
6. Speichert gefundene Mails in Room DB
```

**Expected Result (nach ~10 Sekunden):**
```
✅ Status: "Last sync: X fetched, Y processed"
✅ Notification erscheint: "Email sync complete"
✅ Active Jobs Counter updated
```

**In Logcat:**
```
MailFlow: GmailSyncWorker started
GmailApiClient: Fetching messages...
GmailApiClient: Found 15 messages
MailFlow: Filtered 3 messages for agent: Invoice Processor
: Saved 3 messages to database
NotificationManager: Showing sync notification
```

---

### **Step 4: Automatic Background Processing**

Nach Manual Sync triggert automatisch (via WorkManager Chain):

**EmailProcessingWorker:**
```
1. Lädt unprocessed Emails für "Invoice Processor"
2. Für jede Email:
   - Gemini API Call mit YAML gemini_prompt
   - Parst JSON Response
   - Extrahiert Felder (invoice_number, amount, etc.)
   - Speichert in AgentContext Table
3. Markiert Emails als processed
```

**Expected Result:**
```
✅ Notification: "Processing complete: X emails processed"
✅ Context Data in Database
✅ Emails marked as processed
```

**Database Check:**
```sql
-- Via Android Studio Database Inspector
SELECT * FROM agents;
-- Zeigt: Invoice Processor (id=1, isActive=1)

SELECT * FROM emails WHERE agentId = 1;
-- Zeigt: 3 gefilterte Emails

SELECT * FROM agent_contexts WHERE agentId = 1;
-- Zeigt: Extracted data (invoice_number, amount, due_date, etc.)
```

---

### **Step 5: Chat with Agent** (Dashboard → Chat)

1. **Dashboard öffnen**
2. **Agent Card klicken** ("Invoice Processor")
3. **Agent Detail Screen** öffnet sich
4. **"Chat"** Button klicken (oder FAB)

**Chat Screen:**

**User fragt:**
```
What invoices did I receive today?
```

**Agent antwortet (via Gemini + Context):**
```
Based on your emails, you received 3 invoices today:

1. Invoice #INV-2024-001
   - Vendor: Acme Corp
   - Amount: $1,250.00
   - Due: December 15, 2025

2. Invoice #INV-2024-002
   - Vendor: Tech Supplies Inc
   - Amount: $450.00
   - Due: December 20, 2025

3. Invoice #INV-2024-003
   - Vendor: Office Solutions
   - Amount: $890.00
   - Due: December 10, 2025

Total: $2,590.00
```

**Expected Result:**
```
✅ Context wird geladen aus Database
✅ Gemini Chat API nutzt Context
✅ Antwort erscheint in Chat UI
✅ Markdown Rendering funktioniert
```

---

### **Step 6: Automatic Periodic Sync** (Background)

**Nach 30 Minuten:**
```
1. WorkManager triggert GmailSyncWorker automatisch
2. Constraint Check:
   - ✅ Network connected
   - ✅ Battery not low
3. Sync läuft wie in Step 3
4. Notification bei Erfolg/Fehler
```

**Expected Result:**
```
✅ Notification: "Email sync complete: X new emails"
✅ Neue Emails in Database
✅ EmailProcessingWorker chain startet
✅ Status in Settings updated
```

---

## 🐛 Troubleshooting Guide

### Problem: "Sign-in failed"

**Check:**
```bash
# 1. SHA-1 Fingerprint korrekt?
./gradlew signingReport
# Output SHA-1 muss in Google Cloud Console sein

# 2. OAuth Consent Screen konfiguriert?
# - Status: Testing oder Production
# - Test Users: Dein Gmail Account

# 3. Permissions scope korrekt?
# - gmail.readonly
# - gmail.modify
```

**Logs:**
```
Logcat Filter: GmailAuth|GoogleSignIn
```

---

### Problem: "No messages fetched"

**Reasons:**
1. **Keine Emails in Gmail** → Testmail senden
2. **Filter zu restriktiv** → Agent filters checken
3. **API Quota exceeded** → Gmail API Quota in Google Cloud

**Logs:**
```
Logcat Filter: GmailApiClient|GmailSync
```

**Manual Check:**
```kotlin
// In Settings add Debug Button:
viewModel.testGmailConnection()

// Logs:
GmailApiClient: Testing connection...
GmailApiClient: Authenticated: true
GmailApiClient: Fetching test messages...
GmailApiClient: Found 50 messages
```

---

### Problem: "Gemini API Error"

**Check:**
```bash
# 1. API Key gültig?
cat local.properties | grep GEMINI

# 2. API aktiviert in Google Cloud?
# https://console.cloud.google.com/apis/library/generativelanguage.googleapis.com

# 3. Quota überschritten?
# https://console.cloud.google.com/apis/api/generativelanguage.googleapis.com/quotas
```

**Logs:**
```
Logcat Filter: GeminiClient|EmailProcessing
```

**Test Gemini:**
```kotlin
// Create simple test agent:
gemini_prompt: "Say hello"
// Should return: "Hello! How can I help you?"
```

---

### Problem: "Agent not showing in Dashboard"

**Check Database:**
```sql
-- Android Studio → App Inspection → Database Inspector
SELECT * FROM agents;

-- If empty:
-- 1. Create Agent failed? → Check Logcat
-- 2. Repository error? → Check DAO logs
-- 3. Flow not collected? → ViewModel issue
```

**Logs:**
```
Logcat Filter: AgentRepository|DashboardViewModel
```

---

### Problem: "Background Sync never runs"

**Reasons:**
1. **Device in Doze Mode** → Disable battery optimization
2. **No network** → Connect to WiFi
3. **WorkManager cancelled** → Check Settings "Cancel All"

**Debug WorkManager:**
```bash
# Via ADB:
adb shell dumpsys jobscheduler | grep mailflow

# Expected:
# com.mailflow.app/.GmailSyncWorker: scheduled=true
```

**Force Run:**
```bash
# In Settings → "Sync Now" triggers immediately
# Or via WorkManager test:
adb shell am broadcast -a androidx.work.diagnostics.REQUEST_DIAGNOSTICS
```

---

## 📊 Success Metrics

### After Complete E2E Test:

**Database State:**
```
✅ agents table: 1 row (Invoice Processor)
✅ emails table: 3+ rows (filtered emails)
✅ agent_contexts table: 15+ rows (extracted fields)
✅ processing_jobs table: 1+ rows (completed jobs)
```

**Notifications:**
```
✅ "Sign in successful"
✅ "Agent created successfully"
✅ "Email sync complete: X fetched, Y processed"
✅ "Processing complete: Z emails processed"
```

**UI State:**
```
✅ Dashboard shows agent card
✅ Settings shows signed-in email
✅ Sync status shows last run
✅ Chat screen shows context-aware responses
```

**Logs (no errors):**
```
✅ No "Failed to" messages
✅ No exception stack traces
✅ All API calls successful
✅ WorkManager jobs completed
```

---

## 🎯 Test Scenarios

### Scenario 1: Invoice Processing (Complete Flow)
**Duration:** ~5 minutes
1. Create "Invoice Processor" agent
2. Send test invoice email to yourself
3. Trigger manual sync
4. Check notification
5. Chat: "What invoices did I receive?"
6. ✅ Agent lists extracted invoice data

---

### Scenario 2: Newsletter Summarizer
```yaml
agent:
  name: "Newsletter Digest"
  description: "Summarizes daily newsletters"

filters:
  senders:
    - "newsletter@substack.com"
    - "digest@medium.com"
  subjects: []
  hasAttachments: false

gemini_prompt: "Summarize this newsletter in 3 bullet points. Focus on key insights and actionable takeaways."

context_schema:
  title: string
  summary: string
  key_points: list
  date_received: string
```

**Test:**
1. Subscribe to newsletter
2. Create agent
3. Sync emails
4. Chat: "Summarize today's newsletters"

---

### Scenario 3: Customer Support Tracker
```yaml
agent:
  name: "Support Ticket Tracker"
  description: "Tracks customer support requests"

filters:
  senders:
    - "support@mycompany.com"
  subjects:
    - "Ticket"
    - "Support"
  hasAttachments: false

gemini_prompt: "Extract ticket information: ticket number, customer name, issue type, priority level, and status."

context_schema:
  ticket_number: string
  customer_name: string
  issue_type: string
  priority: string
  status: string
```

---

## 📈 Performance Benchmarks

**Expected Times:**
```
Gmail Sign-In: < 5s
Agent Creation: < 2s
Manual Sync (100 emails): < 15s
Email Processing (10 emails): < 30s
Chat Response: < 3s
Dashboard Load: < 1s
```

**Resource Usage:**
```
Memory: < 100MB
Battery (background sync): < 2% per day
Network (per sync): < 5MB
Storage (1000 emails): < 50MB
```

---

## ✅ Checklist for Full E2E Test

- [ ] Gmail Sign-In successful
- [ ] Agent created and visible in Dashboard
- [ ] Manual Sync fetches emails
- [ ] Notification appears after sync
- [ ] Emails filtered correctly by agent
- [ ] Email Processing extracts data
- [ ] Context stored in database
- [ ] Chat uses agent context
- [ ] Background sync runs automatically (wait 30min)
- [ ] Pull-to-refresh works in Dashboard
- [ ] Agent can be edited/deleted
- [ ] Sign Out works
- [ ] Re-sign In persists agents

---

**Test Status:** ✅ ALL SYSTEMS READY
**Last Updated:** 2025-11-06
**Build:** DEBUG - SUCCESS
**Coverage:** End-to-End Flow Complete
