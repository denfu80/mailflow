# Refactoring-Plan: MailFlow zu Todo-Extractor

Dieses Dokument beschreibt den Plan zur Neuausrichtung der MailFlow-App.

## 1. Zielsetzung

Die App wird von einem komplexen Multi-Agenten-System zu einem schlanken Tool, das eine einzige, vordefinierte Aufgabe hat: E-Mails analysieren und daraus Aufgaben (Todos) extrahieren. Diese Todos werden an eine externe, vom Benutzer bereitgestellte To-Do-Listen-Webanwendung gesendet.

## 2. Geplante Schritte

### Schritt 1: Todo-API-Abstraktion

- **Aktion:** Eine Schnittstelle (`TodoApiClient`) im `data`-Modul erstellen.
- **Details:** Diese Schnittstelle wird Methoden wie `addTodo(listName: String, todoTitle: String, todoBody: String)` definieren.
- **Implementierung:** Eine echte Retrofit-Implementierung wird direkt erstellt, basierend auf der [vorhandenen API-Dokumentation](docs/API_DOCS.md).

### Schritt 2: Anpassung der Verarbeitungslogik (Domain Layer)

- **Aktion:** Den `ProcessEmailUseCase` und den zugehörigen KI-Prompt anpassen.
- **Details:** Der Use Case wird so geändert, dass er nicht mehr versucht, generischen Kontext zu speichern, sondern gezielt nach Aufgaben sucht.
- **KI-Prompt:** Der Gemini-Prompt wird fest codiert, um die Extraktion von Aufgaben aus einem E-Mail-Text zu optimieren. Das Ergebnis sollte ein oder mehrere Todos enthalten.
- **Integration:** Der Use Case ruft nach erfolgreicher Extraktion den `TodoApiClient` auf, um die Aufgabe zu übermitteln.

### Schritt 3: Vereinfachung der UI (Presentation Layer)

- **Aktion:** Nicht mehr benötigte UI-Komponenten entfernen.
- **Entfernt werden:**
    - `ChatScreen` und zugehörige ViewModels/Navigation.
    - `AgentListScreen` / `DashboardScreen` in seiner jetzigen Form.
    - `CreateAgentScreen` und der YAML-Editor.
- **Aktion:** Neue, vereinfachte UI erstellen.
- **Neuer Hauptbildschirm:** Ein einfacher "Activity Log" Screen, der den Status der letzten Synchronisierungen und die Anzahl der erstellten Todos anzeigt.
- **Angepasster Einstellungsbildschirm:**
    - Entfernen aller Agenten-spezifischen Einstellungen.
    - Hinzufügen eines Textfeldes zur Konfiguration des `listName` für die Todo-API.
    - (Zukunft): Felder für API-Endpunkt und Authentifizierung.

### Schritt 4: Anpassung der Hintergrundverarbeitung (Data Layer)

- **Aktion:** Den `EmailProcessingWorker` anpassen.
- **Details:** Der Worker wird den modifizierten `ProcessEmailUseCase` aufrufen. Die Logik zur Verarbeitung von mehreren Agenten entfällt.

### Schritt 5: Notifications anpassen

- **Aktion:** Das Benachrichtigungssystem aktualisieren.
- **Details:** Statt allgemeiner Verarbeitungs-Benachrichtigungen wird eine spezifische Benachrichtigung gesendet, wenn ein neues Todo erfolgreich erstellt wurde (z.B. "Neues Todo 'Rechnung bezahlen' zur Liste 'Inbox' hinzugefügt.").

### Schritt 6: Bereinigung der Datenmodelle und Use Cases

- **Aktion:** Nicht mehr benötigte Modelle und Use Cases entfernen.
- **Entfernt werden:**
    - `MailAgentEntity`, `AgentContextEntity` aus der Room-Datenbank.
    - `CreateAgentUseCase`, `ChatWithAgentUseCase` aus dem Domain-Layer.
    - Zugehörige Repositories und DAOs.

## 3. Aktualisierung der Dokumentation

- **CONTEXT.md:** Projektübersicht, Architektur und Datenmodell an die neue, vereinfachte Zielsetzung anpassen.
- **ROADMAP.yaml:** Die Roadmap komplett überarbeiten, um die neuen Phasen (z.B. "Implementierung der Todo-API", "UI-Vereinfachung") widerzuspiegeln. Alte Phasen werden entfernt.
- **QUICKSTART.md:** Test-Szenarien an die neue Funktionalität anpassen (z.B. "Wie teste ich, ob ein Todo erstellt wird?").
