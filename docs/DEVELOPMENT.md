# MailFlow Development Documentation

## Project Structure

MailFlow follows Clean Architecture with a multi-module setup:

```
MailFlow/
├── app/                    # Application module
├── presentation/           # UI Layer (Jetpack Compose)
├── domain/                 # Business Logic (Use Cases)
├── data/                   # Data Layer (APIs, Database)
└── core/                   # Shared Utilities
```

## Build Commands

```bash
./gradlew build                    # Build entire project
./gradlew :domain:build            # Build domain module only
./gradlew clean build              # Clean build
./gradlew test                     # Run all tests
```

## Phase 2: Domain Layer Implementation

### Completed Tasks

#### ✅ P2-T1: Domain Models with Value Objects and Sealed Classes

**Files Created:**
- `domain/model/AgentConfiguration.kt` - Configuration and email filters
- `domain/model/ProcessingResult.kt` - Sealed classes for results and states
- `domain/model/ChatMessage.kt` - Chat system models

**Key Features:**
- `ProcessingResult<T>` sealed class for type-safe result handling
- `SyncResult` with Success/PartialSuccess/Failure states
- `EmailFilters` with intelligent matching logic
- `ProcessingStatus` enum for job tracking

#### ✅ P2-T2: CreateAgentUseCase with Validation

**File:** `domain/usecase/CreateAgentUseCase.kt`

**Validation Rules:**
- Name cannot be empty
- Name must be at least 3 characters
- YAML configuration must be valid
- No duplicate agent names (case-insensitive)

**Dependencies:**
- `AgentRepository` for persistence
- `ValidateYamlUseCase` for YAML validation

#### ✅ P2-T3: SyncEmailsUseCase

**File:** `domain/usecase/SyncEmailsUseCase.kt`

**Features:**
- Sync emails for single agent or all active agents
- Gmail API integration via `GmailService` interface
- Agent-based email filtering
- Detailed sync statistics (fetched, processed, errors)
- Batch processing support

**Return Types:**
- `SyncResult.Success` - All emails synced successfully
- `SyncResult.PartialSuccess` - Some errors occurred
- `SyncResult.Failure` - Complete failure

#### ✅ P2-T4: ProcessEmailUseCase

**File:** `domain/usecase/ProcessEmailUseCase.kt`

**Features:**
- Single email processing with AI analysis
- Batch processing of unprocessed emails
- Context extraction and storage
- Gemini AI integration via `GeminiService` interface
- Automatic context updates in database

**Processing Flow:**
1. Load agent configuration and existing context
2. Build AI prompt with email content
3. Analyze email with Gemini
4. Parse and extract structured data
5. Update context in database
6. Mark email as processed

#### ✅ P2-T5: ChatWithAgentUseCase

**File:** `domain/usecase/ChatWithAgentUseCase.kt`

**Features:**
- Single-shot chat responses
- Streaming chat support (via Flow)
- Chat session management
- Context injection from database
- Chat history support (last 10 messages)

**Methods:**
- `invoke(agentId, userMessage, chatHistory)` - Get chat response
- `invokeStreaming(...)` - Stream AI responses
- `getChatSession(agentId)` - Load chat session with context

## Dependencies

### Domain Module

```kotlin
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    testImplementation(libs.junit)
}
```

### Key Libraries
- **Kotlin Coroutines** - Async programming
- **javax.inject** - Dependency injection annotations
- **JUnit** - Unit testing

## Architecture Principles

### Domain Layer Rules

1. **No Android Dependencies** - Pure Kotlin only
2. **Repository Interfaces** - Define contracts, not implementations
3. **Use Cases** - One responsibility per use case
4. **Type Safety** - Use sealed classes for results
5. **Testability** - Easy to mock and test

### Service Interfaces

The domain layer defines service interfaces that are implemented in the data layer:

```kotlin
interface GmailService {
    suspend fun fetchNewMessages(): List<EmailMessage>
}

interface GeminiService {
    suspend fun analyzeEmail(prompt: String): String
    suspend fun chat(prompt: String, context: Map<String, Any>): String
}
```

## Testing Strategy

### Unit Tests (Planned for Phase 5)

- **Use Case Tests** - Mock repositories and services
- **Validation Tests** - Test input validation
- **Business Logic Tests** - Test filtering, matching
- **Error Handling Tests** - Test failure scenarios

### Test Coverage Goal
- Domain layer: > 80%
- Use cases: 100%

## Next Steps

### Phase 3: Presentation Layer & UI Components

Tasks to implement:
1. Design System & Theme Setup
2. Navigation Setup
3. Dashboard Screen
4. Agent Creation Screen
5. Chat Interface Screen
6. ViewModels & State Management
7. Settings Screen

## Known Issues

None at this time. All Phase 2 tasks completed successfully.

## Build Status

✅ Domain module builds successfully
✅ All modules compile without errors
✅ No dependency conflicts

Last Updated: 2025-11-06
