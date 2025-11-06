# MailFlow - Android Mail-Agent-System

## 📋 Projekt-Übersicht

**MailFlow** ist eine native Android App, die als intelligentes Mail-Processing-System fungiert. Die App ermöglicht es Benutzern, dynamische AI-Agenten zu erstellen, die eingehende Gmail-Nachrichten automatisch verarbeiten, analysieren und strukturierte Kontexte pflegen. Jeder Agent kann über ein Chat-Interface für natürliche Interaktionen genutzt werden.

## 🎯 Kernfunktionalitäten

### Mail-Processing Pipeline
- Automatische Gmail-Synchronisation im Hintergrund
- Dynamische Agent-Konfiguration via YAML-Files
- Google Gemini AI-Integration für Mail-Analyse
- Strukturierte Kontext-Speicherung pro Agent

### Agent-System
- Benutzer können neue Agenten über UI erstellen
- Jeder Agent hat spezifische Email-Filter (Absender, Betreff, Anhänge)
- Konfigurierbare Gemini-Prompts für individuelle Verarbeitung
- Lokale YAML-Context-Files pro Agent

### Chat-Interface
- Dedicated Chat-Screen pro Agent
- Interaction mit Agent-spezifischen Kontexten
- Gemini-basierte Konversation über gesammelte Mail-Daten
- Zukunft: Todo-Erstellung, Kalender-Integration

## 🏗️ Technische Architektur

### Clean Architecture Pattern
```
Presentation Layer (UI)
├── Jetpack Compose UI
├── ViewModels mit StateFlow
└── Navigation Component

Domain Layer (Business Logic)
├── Use Cases
├── Repository Interfaces
└── Domain Models

Data Layer
├── Room Database (lokale Speicherung)
├── Gmail API Client
├── Gemini API Client
└── Repository Implementations
```

### Multi-Module Setup
```
app/
├── presentation/     - UI Layer (Compose Screens, ViewModels)
├── domain/          - Business Logic (Use Cases, Models)
├── data/            - Data Sources (APIs, Database)
└── core/            - Shared Utilities, Extensions
```

## 📱 UI/UX Konzept

### Screen-Flow
1. **Dashboard** - Übersicht aller Agenten mit Status
2. **Agent Management** - Erstellen/Bearbeiten von Agenten
3. **Agent Configuration** - YAML-Editor für Agent-Setup
4. **Chat Interface** - Conversational UI pro Agent
5. **Settings** - App-Konfiguration, Gmail-Auth

### Design System
- Material Design 3 mit Jetpack Compose
- Dark/Light Theme Support
- Responsive Layout für verschiedene Screen-Größen
- Accessibility-optimiert

## 🔧 Tech Stack & Dependencies

### Core Android
- **Kotlin** - Programmiersprache
- **Jetpack Compose** - Moderne UI-Entwicklung
- **Navigation Compose** - App-Navigation
- **ViewModel & StateFlow** - State Management

### Dependency Injection
- **Hilt** - Dependency Injection Framework
- **Hilt Navigation Compose** - ViewModel Integration

### Database & Storage
- **Room Database** - Lokale SQLite-Abstraktion
- **DataStore** - Settings & Preferences
- **YAML Parser** - Agent-Konfiguration (SnakeYAML Android)

### Background Processing
- **WorkManager** - Reliable Background Tasks
- **Coroutines** - Asynchrone Programmierung
- **Flow** - Reactive Streams

### API Integration
- **Gmail API** - Mail-Zugriff (Google APIs Client)
- **Google Sign-In** - OAuth2 Authentication
- **Gemini API** - AI-Processing (Generative AI SDK)
- **Retrofit** - HTTP Client für APIs
- **OkHttp** - Network Layer

### Testing
- **JUnit** - Unit Testing
- **Mockk** - Mocking Framework
- **Compose UI Testing** - UI Tests
- **Room Testing** - Database Tests

## 📊 Datenmodell

### Entities
- **MailAgent** - Agent-Konfiguration und Metadaten
- **EmailMessage** - Verarbeitete Mail-Daten
- **AgentContext** - Strukturierte Kontext-Einträge
- **ProcessingJob** - Background-Task Status

### Data Flow
```
Gmail API → Background Sync → Agent Processing →
Gemini Analysis → Context Update → UI Refresh
```

## 🔐 Sicherheit & Permissions

### Android Permissions
- INTERNET - API-Zugriff
- WAKE_LOCK - Background Processing
- RECEIVE_BOOT_COMPLETED - Auto-Start nach Reboot

### OAuth2 Scopes
- Gmail Read/Modify - Mail-Zugriff
- UserInfo Profile - Benutzer-Identifikation

### Data Protection
- Lokale Verschlüsselung sensibler Daten
- Sichere Token-Speicherung mit EncryptedSharedPreferences
- HTTPS-only API Communication

## 🚀 Development Workflow

### Build Configuration
- **Gradle Kotlin DSL** - Build Scripts
- **Version Catalogs** - Dependency Management
- **Build Variants** - Debug/Release/Staging
- **ProGuard/R8** - Code Obfuscation

### CI/CD Pipeline (Zukunft)
- GitHub Actions für Automated Testing
- Automated Build & Deployment
- Code Quality Checks (Detekt, Ktlint)

## 📈 Skalierbarkeit & Erweiterungen

### Phase 1: Core Functionality
- Basic Agent Creation & Chat
- Gmail Integration & Background Sync
- Gemini AI Processing

### Phase 2: Advanced Features
- Todo-System Integration
- Kalender-Anbindung
- Advanced Analytics Dashboard
- Multi-Account Support

### Phase 3: Enterprise Features
- Team-Sharing von Agenten
- Cloud-Backup & Sync
- Advanced Security Features
- API für Third-Party Integration

## 🎯 Success Metrics

### Technical KPIs
- App Startup Time < 2s
- Background Sync Reliability > 99%
- Chat Response Time < 3s
- Memory Usage < 100MB

### User Experience
- Agent Creation Flow < 5 Schritte
- Intuitive Chat Interface
- Offline Context Access
- Battery-effiziente Background Tasks

## 📊 Current Status

### ✅ Phase 0: Project Foundation & Setup (COMPLETED)
- Multi-module architecture setup
- Gradle dependencies configured
- Hilt dependency injection ready
- Google APIs configured

### ✅ Phase 1: Data Layer Implementation (COMPLETED)
- Room database schema defined
- Repository interfaces implemented
- Gmail API client integration ready
- Gemini API client integration ready

### ✅ Phase 2: Domain Layer & Business Logic (COMPLETED)
- Domain models with value objects and sealed classes
- CreateAgentUseCase with validation
- SyncEmailsUseCase with filtering
- ProcessEmailUseCase with AI integration
- ChatWithAgentUseCase with streaming support

**Current Phase:** Ready for Phase 3 (Presentation Layer)

## 📝 Nächste Schritte

### Android Studio Setup
1. Neues Android Studio Projekt erstellen
2. Gradle Dependencies konfigurieren
3. Multi-Module Struktur aufsetzen
4. Google APIs Setup (Gmail, Gemini)
5. Room Database Schema definieren
6. Basis UI-Komponenten implementieren

### Development Prioritäten
1. **Foundation** - Projekt-Setup, Dependencies, Architektur
2. **Data Layer** - Room Database, API Clients, Repositories
3. **Domain Layer** - Use Cases, Business Logic
4. **Presentation Layer** - Compose UI, ViewModels, Navigation
5. **Integration** - Background Processing, Testing

Dieses Projekt kombiniert moderne Android-Entwicklung mit AI-Integration für ein innovatives persönliches Produktivitäts-Tool.