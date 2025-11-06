# MailFlow Chat - Test Guide 💬

## ✅ Chat ist jetzt funktional!

Das Chat Feature nutzt:
- ✅ **Agent Context** aus der Database (alle extrahierten Email-Daten)
- ✅ **Gemini AI** für intelligente Antworten
- ✅ **Chat History** für Konversations-Context
- ✅ **Error Handling** mit Snackbar

---

## 🧪 Chat Test Szenarien

### **Voraussetzung: Agent mit Daten**

Du brauchst einen Agent, der bereits Emails verarbeitet hat:

```bash
# 1. Install & Run
./gradlew installDebug

# 2. Setup:
# - Sign In mit Gmail ✅
# - Create Agent ✅
# - Sync Now → Emails fetchen ✅
# - Wait ~30 sec → EmailProcessingWorker extrahiert Daten ✅
```

---

## 📱 Test Flow

### **Szenario 1: Invoice Agent Chat**

#### Setup:
```yaml
# Agent: "Invoice Processor"
gemini_prompt: "Extract invoice details"
context_schema:
  invoice_number: string
  amount: string
  due_date: string
  vendor_name: string
```

#### Nach Sync & Processing:
```sql
-- Database sollte zeigen:
SELECT * FROM agent_contexts WHERE agentId = 1;

-- Beispiel Daten:
| id | agentId | key              | value           |
|----|---------|------------------|-----------------|
| 1  | 1       | invoice_number   | INV-2024-001    |
| 2  | 1       | amount           | $1,250.00       |
| 3  | 1       | due_date         | 2025-12-15      |
| 4  | 1       | vendor_name      | Acme Corp       |
```

#### Chat Test:

**User fragt:**
```
What invoices did I receive?
```

**Agent antwortet (nutzt Context!):**
```
Based on your emails, you received the following invoice:

Invoice Number: INV-2024-001
Vendor: Acme Corp
Amount: $1,250.00
Due Date: December 15, 2025

This invoice is currently unpaid. Would you like me to help you
track or remind you about the payment?
```

**Follow-up:**
```
User: When is it due?
Agent: The invoice is due on December 15, 2025. That's in 9 days
       from today.

User: What's the total amount?
Agent: The total amount for invoice INV-2024-001 is $1,250.00.
```

---

### **Szenario 2: Newsletter Digest Agent**

#### Setup:
```yaml
# Agent: "Newsletter Digest"
gemini_prompt: "Summarize newsletter in 3 bullet points"
context_schema:
  title: string
  summary: string
  key_points: list
  source: string
```

#### Chat Test:

**User fragt:**
```
Summarize today's newsletters
```

**Agent antwortet:**
```
Here's a summary of today's newsletters:

📰 TechCrunch Daily (techcrunch.com)
• AI startup raises $50M Series A
• New Android 15 features announced
• Meta launches new AR glasses

📰 The Pragmatic Engineer (newsletter.pragmaticengineer.com)
• Career growth strategies for senior engineers
• Remote work trends in 2025
• Salary negotiation tips

Would you like more details on any of these topics?
```

---

### **Szenario 3: Support Ticket Tracker**

#### Setup:
```yaml
# Agent: "Support Tracker"
gemini_prompt: "Extract ticket details"
context_schema:
  ticket_number: string
  customer_name: string
  issue_type: string
  priority: string
  status: string
```

#### Chat Test:

**User fragt:**
```
Show me all high priority tickets
```

**Agent antwortet:**
```
Here are the high priority support tickets:

🔴 Ticket #12345
Customer: John Smith
Issue: Payment processing failure
Status: Open
Priority: High

🔴 Ticket #12348
Customer: Sarah Johnson
Issue: Account access locked
Status: In Progress
Priority: High

Total: 2 high priority tickets require attention.
```

---

## 🔍 Was passiert im Hintergrund?

### **1. Context Loading** (beim Chat Start)
```kotlin
// ChatViewModel init
contextRepository.getContextByAgent(agentId)
// → Lädt ALLE extrahierten Daten für diesen Agent

// Beispiel Context:
{
  "invoice_number": "INV-2024-001",
  "amount": "$1,250.00",
  "due_date": "2025-12-15",
  "vendor_name": "Acme Corp"
}
```

### **2. Prompt Building** (bei jeder User Message)
```kotlin
// ChatWithAgentUseCase.buildChatPrompt()

You are an AI assistant for the agent: Invoice Processor

Agent Configuration:
[YAML Config mit gemini_prompt]

Current Context (data extracted from emails):
invoice_number: INV-2024-001
amount: $1,250.00
due_date: 2025-12-15
vendor_name: Acme Corp

Chat History:
User: What invoices did I receive?
Assistant: [previous response]

User: [NEW MESSAGE]

Please provide a helpful response based on the agent's context.
```

### **3. Gemini API Call**
```kotlin
geminiService.chat(prompt, contextMap)
// → Gemini sieht den vollen Context!
// → Antwortet basierend auf echten Email-Daten
```

---

## 🧪 Debug Chat Feature

### **Check if Context is loaded:**

**In Chat init:**
```
Logcat Filter: ChatViewModel|ContextRepository

Expected:
ChatViewModel: Loading agent context for ID: 1
ContextRepository: Found 4 context entries
ChatViewModel: Context loaded: invoice_number, amount, due_date, vendor_name
```

### **Check Gemini Call:**

```
Logcat Filter: GeminiService|ChatWithAgent

Expected:
ChatWithAgentUseCase: Building prompt with 4 context entries
GeminiServiceImpl: Sending chat request
GeminiClient: Generating content...
GeminiClient: Response received (234 chars)
```

### **If no context found:**

```sql
-- Check Database:
SELECT * FROM agent_contexts WHERE agentId = 1;

-- If empty:
-- 1. Emails wurden noch nicht verarbeitet
-- 2. EmailProcessingWorker muss noch laufen
-- 3. Gemini Extraction failed

-- Check emails table:
SELECT id, subject, processed FROM emails WHERE agentId = 1;

-- If processed = 0:
-- → Processing Worker hasn't run yet
-- → Trigger manually or wait for chain
```

---

## 🎯 Erweiterte Chat Features (bereits implementiert!)

### **1. Chat History**
- Letzte 10 Messages werden an Gemini gesendet
- Kontext-bewusste Antworten
- "User: ..." und "Assistant: ..." Format

### **2. Error Handling**
- Snackbar zeigt Fehler
- "Failed to send message" bei Gemini Error
- "Agent not found" bei ungültiger ID
- "Message cannot be empty" Validation

### **3. Loading States**
- `isSending = true` während Gemini Call
- TextField disabled während Send
- Loading indicator in Chat (optional)

---

## 🔧 Troubleshooting

### "Agent antwortet nicht context-aware"

**Problem:** Keine Context Daten in Database

**Solution:**
```bash
# 1. Check ob Emails processed sind:
# Database Inspector → emails table → processed = 1

# 2. Wenn processed = 0:
# - EmailProcessingWorker muss laufen
# - Check WorkManager in Settings
# - Trigger manual: viewModel.scheduleEmailProcessing(agentId)

# 3. Check Gemini Prompt:
# - YAML gemini_prompt muss klar sein
# - Context Schema muss defined sein
```

### "Chat zeigt nur Placeholder Response"

**Problem:** UseCase nicht verbunden

**✅ FIXED!** ChatViewModel nutzt jetzt:
```kotlin
chatWithAgentUseCase(agentId, message, history)
// → Lädt Context
// → Ruft Gemini auf
// → Returned echte AI Response
```

### "Error: Agent not found"

**Problem:** Navigation Parameter fehlt

**Check:**
```kotlin
// In DashboardScreen:
onAgentClick = { agentId ->
    navController.navigate("chat/$agentId")
}

// agentId muss String sein!
```

---

## 📊 Expected Results

### **After 1st Message:**
```
User: "Hello"
Agent: "Hello! I'm the Invoice Processor agent. I help you track
       and manage invoice emails. I currently have information about
       1 invoice from Acme Corp. How can I help you today?"
```

### **With Context Data:**
```
User: "What invoices do I have?"
Agent: [Lists all invoices from context with details]

User: "Which one is due first?"
Agent: "The Acme Corp invoice (INV-2024-001) is due on December 15,
       2025, which is the earliest due date."
```

### **Without Context Data (no emails processed):**
```
User: "What invoices do I have?"
Agent: "I don't have any invoice data yet. Please sync your emails
       first so I can extract and track invoice information for you."
```

---

## 🎯 Complete Test Checklist

- [ ] Create agent with clear gemini_prompt
- [ ] Sync emails (Settings → "Sync Now")
- [ ] Wait for processing (~30 sec)
- [ ] Check database has context entries
- [ ] Navigate to Chat from Agent Card
- [ ] Send message: "What data do you have?"
- [ ] ✅ Agent lists all context fields!
- [ ] Send follow-up question
- [ ] ✅ Agent remembers previous messages!
- [ ] Ask specific question about data
- [ ] ✅ Agent answers from real email data!

---

## 🚀 Advanced Chat Features (Optional Future)

Already supported in ChatWithAgentUseCase:
- ✅ Streaming responses (invokeStreaming)
- ✅ Chat history (last 10 messages)
- ✅ Context injection
- ✅ Agent configuration in prompt

Can be added later:
- ⏳ Save chat history to DB
- ⏳ Export chat transcript
- ⏳ Voice input
- ⏳ Image/attachment support

---

**Status:** ✅ **FULLY FUNCTIONAL**
**Context:** ✅ **LOADED FROM DATABASE**
**AI:** ✅ **GEMINI WITH CONTEXT**
**History:** ✅ **PRESERVED IN SESSION**

**READY TO CHAT!** 💬🚀
