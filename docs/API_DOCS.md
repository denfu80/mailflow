# mach.einfach API Dokumentation

Die mach.einfach API ermöglicht es, Todo-Listen programmatisch zu erstellen, zu verwalten und zu aktualisieren.

## Basis-URL

```
https://doeasy-topaz.vercel.app/api
```

(Lokal: `http://localhost:3000/api`)

## Authentifizierung

Die API benötigt aktuell **keine Authentifizierung**. Alle Requests werden als `api` User behandelt.

⚠️ **Hinweis**: In einer Produktionsumgebung sollte eine API-Key-Authentifizierung implementiert werden.

## Endpunkte

### 1. Liste erstellen

Erstellt eine neue Todo-Liste mit einer automatisch generierten, lesbaren ID.

**Endpoint:** `POST /api/lists`

**Request Body:**
```json
{
  "name": "Meine Einkaufsliste",
  "description": "Für den Wocheneinkauf",
  "creatorName": "Max",
  "flavour": "bring"
}
```

**Parameter:**
- `name` (optional, string): Name der Liste
- `description` (optional, string): Beschreibung der Liste
- `creatorName` (optional, string): Name des Erstellers (wird zufällig generiert, falls nicht angegeben)
- `flavour` (optional, string): Liste-Typ (`mach`, `bring`, `schenk`, `organisier`, `pack`). Standard: `mach`

**Response (201 Created):**
```json
{
  "success": true,
  "listId": "calm-snails-dream",
  "url": "/list/calm-snails-dream",
  "creatorName": "Max",
  "flavour": "bring"
}
```

**Beispiel (curl):**
```bash
curl -X POST https://doeasy-topaz.vercel.app/api/lists \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Einkaufsliste",
    "creatorName": "Max",
    "flavour": "bring"
  }'
```

### 2. Todo hinzufügen

Fügt ein neues Todo zu einer existierenden Liste hinzu.

**Endpoint:** `POST /api/lists/{listId}/todos`

**URL-Parameter:**
- `listId` (required): ID der Liste (z.B. `calm-snails-dream`)

**Request Body:**
```json
{
  "text": "Äpfel kaufen",
  "creatorName": "Anna",
  "completed": false
}
```

**Parameter:**
- `text` (required, string): Text des Todos
- `creatorName` (optional, string): Name des Erstellers (wird zufällig generiert, falls nicht angegeben)
- `completed` (optional, boolean): Ob das Todo bereits abgehakt ist. Standard: `false`

**Response (201 Created):**
```json
{
  "success": true,
  "todoId": "-O8xYz123abc",
  "todo": {
    "id": "-O8xYz123abc",
    "text": "Äpfel kaufen",
    "completed": false,
    "createdAt": 1234567890000,
    "createdBy": "api",
    "creatorName": "Anna"
  }
}
```

### 3. Todo abhaken / Status ändern

Ändert den completed-Status eines Todos.

**Endpoint:** `PATCH /api/lists/{listId}/todos/{todoId}`

**URL-Parameter:**
- `listId` (required): ID der Liste
- `todoId` (required): ID des Todos (z.B. `-O8xYz123abc`)

**Request Body:**
```json
{
  "completed": true,
  "completedByName": "Max"
}
```

**Parameter:**
- `completed` (required, boolean): Neuer Status (`true` = abgehakt, `false` = nicht abgehakt)
- `completedByName` (optional, string): Name der Person, die das Todo abgehakt hat (wird zufällig generiert, falls nicht angegeben)

### 4. Todo löschen (Bonus)

Löscht ein Todo (soft-delete, d.h. setzt `deletedAt` Timestamp).

**Endpoint:** `DELETE /api/lists/{listId}/todos/{todoId}`

**URL-Parameter:**
- `listId` (required): ID der Liste
- `todoId` (required): ID des Todos

## Fehler-Codes

| Status Code | Bedeutung |
|-------------|-----------|
| 200 | Erfolgreiche Anfrage (Update) |
| 201 | Erfolgreich erstellt (Create) |
| 400 | Ungültige Anfrage (z.B. fehlende Parameter) |
| 404 | Ressource nicht gefunden (Liste oder Todo existiert nicht) |
| 500 | Server-Fehler |
| 503 | Service nicht verfügbar (Firebase nicht konfiguriert) |
