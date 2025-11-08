# MailFlow - AI-Powered Todo Extraction from Gmail

## 📋 Projekt-Übersicht

**MailFlow** ist eine native Android App, die den Gmail-Posteingang eines Benutzers automatisch überwacht, um aus neuen E-Mails Aufgaben (Todos) zu extrahieren. Ein vordefinierter KI-Agent analysiert die E-Mails und sendet die erkannten Aufgaben an eine vom Benutzer konfigurierte externe To-Do-Listen-Anwendung.

## 🎯 Kernfunktionalitäten

### Mail-Processing Pipeline
- **Automatische Gmail-Synchronisation:** Überprüft den Posteingang im Hintergrund regelmäßig auf neue E-Mails.
- **KI-gestützte Aufgaben-Extraktion:** Nutzt Google Gemini, um den Inhalt neuer E-Mails zu analysieren und potenzielle Aufgaben zu identifizieren.
- **Integration externer Todo-Apps:** Sendet extrahierte Aufgaben über eine Web-API an eine vom Benutzer festgelegte To-Do-Anwendung.
- **Benachrichtigungen:** Informiert den Benutzer, wenn neue Aufgaben erfolgreich erstellt wurden.

### Konfiguration
- **Einfache Einrichtung:** Der Benutzer muss nur sein Gmail-Konto verbinden.
- **Konfigurierbarer Listenname:** Der Name der Zielliste in der Todo-App kann in den Einstellungen festgelegt werden.
- **(Zukunft):** Konfiguration des API-Endpunkts und der Authentifizierung.

## 🏗️ Technische Architektur

Die App folgt weiterhin einem Clean Architecture-Muster mit einem Multi-Modul-Setup.

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
├── Room Database (für Verarbeitungs-Logs)
├── Gmail API Client
├── Gemini API Client
├── Todo API Client (NEU)
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

Das UI/UX-Konzept wird stark vereinfacht.

### Screen-Flow
1.  **Activity Log (Hauptbildschirm):** Zeigt eine Liste der letzten Aktivitäten an (z.B. "Letzte Synchronisierung um 10:30", "1 neues Todo erstellt").
2.  **Settings:** App-Konfiguration, Gmail-Authentifizierung und Eingabe des Namens für die Todo-Liste.

### Design System
- Material Design 3 mit Jetpack Compose
- Dark/Light Theme Support

## 🔧 Tech Stack & Dependencies

Der Kern des Tech-Stacks bleibt erhalten, aber einige spezifische Abhängigkeiten werden entfernt.

- **Entfernt:** YAML Parser.
- **Hinzugefügt:** Eine Retrofit-Implementierung für die neue Todo-API.

### API Integration
- **Gmail API** - Mail-Zugriff
- **Google Sign-In** - OAuth2 Authentication
- **Gemini API** - KI-Verarbeitung
- **Retrofit** - HTTP Client für die Todo-API (siehe [API-Dokumentation](docs/API_DOCS.md))

## 📊 Datenmodell

Das lokale Datenmodell wird drastisch vereinfacht.

### Entities
- **EmailMessage:** Speichert eine Referenz auf bereits verarbeitete E-Mails, um Duplikate zu vermeiden.
- **ProcessingLog:** Ein einfacher Log-Eintrag für UI-Zwecke (z.B. "Sync um 10:30, 2 Todos gefunden").

### Data Flow
```
Gmail API → Background Sync → KI-Analyse (Gemini) → Todo-Extraktion → Todo API Client → Externe Web App
```

## 🚀 Entwicklungsplan (Neuausrichtung)

1.  **Bereinigung:** Entfernen der alten UI (Agenten-Verwaltung, Chat) und der zugehörigen Logik (Use Cases, Repositories).
2.  **API-Abstraktion:** Erstellen einer Schnittstelle für den `TodoApiClient`.
3.  **Logik anpassen:** Anpassen des `ProcessEmailUseCase` zur reinen Todo-Extraktion und zum Aufruf des `TodoApiClient`.
4.  **UI neu erstellen:** Implementieren des einfachen Activity Logs und Anpassen des Einstellungsbildschirms.
5.  **Notifications:** Anpassen der Benachrichtigungen für erstellte Todos.
6.  **Integration:** Implementieren des echten `TodoApiClient` mit Retrofit, sobald die API-Spezifikation vorliegt.

Dieses Projekt fokussiert sich nun darauf, eine klare, nützliche Automatisierungsaufgabe zu lösen, anstatt eine komplexe, konfigurierbare Plattform zu sein.