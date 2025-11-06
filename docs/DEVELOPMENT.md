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

## Phase 3: Presentation Layer & UI Components (IN PROGRESS)

### Completed Tasks

#### ✅ P3-T1: Material 3 Design System & Theme

**Files Created:**
- `presentation/theme/Color.kt` - Color palette for light/dark themes
- `presentation/theme/Type.kt` - Material 3 typography system
- `presentation/theme/Theme.kt` - Theme composable with dynamic color support
- `presentation/theme/Spacing.kt` - Consistent spacing values

**Features:**
- Dynamic color support (Android 12+)
- Dark/Light theme with system preference
- Material 3 typography scale
- Centralized spacing system

#### ✅ P3-T2: Reusable UI Components (Atomic Design)

**Atoms:**
- `LoadingIndicator.kt` - Loading state component
- `EmptyState.kt` - Empty state with message and subtitle
- `ErrorState.kt` - Error display with retry action

**Molecules:**
- `AgentCard.kt` - Clickable agent card with status badge
- `StatusBadge.kt` - Active/Inactive status indicator
- `ChatBubble.kt` - User/Agent chat message bubble

**Design Principles:**
- Atomic Design Pattern (Atoms → Molecules → Organisms)
- Material 3 components
- Easily customizable via parameters
- Theme-aware styling

#### ✅ P3-T3: Navigation Setup

**Files:**
- `navigation/Screen.kt` - Sealed class navigation routes
- `navigation/NavGraph.kt` - Compose Navigation setup

**Routes:**
- `Dashboard` - Main screen with agent list
- `CreateAgent` - Agent creation flow
- `AgentDetail/{agentId}` - Agent configuration
- `Chat/{agentId}` - Chat interface
- `Settings` - App settings

**Features:**
- Type-safe navigation with sealed classes
- Parameter passing via route arguments
- Centralized navigation graph

#### ✅ P3-T4: Dashboard Screen with MVVM

**Files:**
- `screens/dashboard/DashboardUiState.kt` - UI state model
- `screens/dashboard/DashboardViewModel.kt` - Business logic
- `screens/dashboard/DashboardScreen.kt` - Compose UI

**Architecture:**
- MVVM pattern with StateFlow
- Reactive UI updates via Flow
- Loading/Error/Empty state handling
- Pull-to-refresh support (planned)

**State Management:**
- `DashboardUiState` - Immutable UI state
- `AgentUiModel` - UI-specific agent model
- Flow-based reactive updates
- Error handling with user feedback

#### ✅ P3-T5: Create Agent Screen

**Files:**
- `screens/createagent/CreateAgentUiState.kt` - Form state
- `screens/createagent/CreateAgentViewModel.kt` - Validation logic
- `screens/createagent/CreateAgentScreen.kt` - Form UI

**Features:**
- Name validation (min 3 characters)
- Description field
- YAML configuration editor with monospace font
- Real-time validation feedback
- Loading state during agent creation
- Success/Error handling with Snackbar

#### ✅ P3-T6: Chat Screen

**Files:**
- `screens/chat/ChatUiState.kt` - Chat state
- `screens/chat/ChatViewModel.kt` - Message handling
- `screens/chat/ChatScreen.kt` - Chat UI

**Features:**
- Message list with ChatBubble components
- User/AI message differentiation
- Text input with send button
- Auto-scroll to latest message
- Empty state for new conversations
- Placeholder AI responses (Gemini integration in Phase 4)

#### ✅ P3-T7: Settings Screen

**Files:**
- `screens/settings/SettingsScreen.kt` - Settings UI
- `components/molecules/SettingsItem.kt` - Reusable setting row

**Features:**
- Account section with Gmail placeholder
- App settings (sync frequency, notifications)
- About section with version
- Grouped settings with dividers
- Click handlers for future functionality

#### ✅ P3-T8: Pull-to-Refresh

**Implementation:**
- `PullToRefreshBox` in Dashboard
- Integrated with ViewModel refresh logic
- Loading indicator during refresh
- Smooth animation

### Phase 3 Complete Summary

**Total Screens Implemented:** 4
- Dashboard (with pull-to-refresh)
- Create Agent (with validation)
- Chat (with messaging UI)
- Settings (placeholder)

**Navigation:** Fully functional with type-safe routing

**Architecture:** Clean MVVM pattern throughout

## Next Steps

### Phase 4: Data Layer Implementation

Tasks to implement:
1. Room Database implementation
2. Gmail API client
3. Gemini API client
4. Repository implementations
5. WorkManager background sync

## Known Issues

None at this time. All Phase 2 tasks completed successfully.

## Build Status

✅ All modules build successfully
✅ Presentation layer complete
✅ App runs and displays UI
✅ No dependency conflicts
✅ Clean architecture maintained

**App is now runnable!** Start the app in Android Studio to see:
- Dashboard with empty state
- Create Agent flow
- Chat interface (placeholder AI)
- Settings screen

Last Updated: 2025-11-06
