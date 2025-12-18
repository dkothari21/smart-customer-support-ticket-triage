# What Happens After a POST Request?

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. USER SUBMITS TICKET via POST /api/tickets                       │
│    {                                                                 │
│      "subject": "App crashes on startup",                           │
│      "description": "The app crashes when I open it"                │
│    }                                                                 │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 2. TICKET CONTROLLER receives request                              │
│    - Validates input (subject & description required)              │
│    - Calls TicketService.createTicket()                            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 3. TICKET SERVICE creates ticket in database                       │
│    - Status: PENDING                                                │
│    - Category: null                                                 │
│    - Priority: null                                                 │
│    - Sentiment: null                                                │
│    - Saves to H2 database                                           │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 4. PUBLISHES EVENT (Spring ApplicationEventPublisher)              │
│    - Creates TicketClassificationEvent with ticket ID              │
│    - Event goes to async thread pool                               │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 5. RETURNS RESPONSE TO USER (< 100ms)                              │
│    {                                                                 │
│      "id": 998,                                                      │
│      "status": "PENDING",    ← Not classified yet!                  │
│      "category": null,                                               │
│      "priority": null,                                               │
│      "sentiment": null                                               │
│    }                                                                 │
└─────────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════
        USER GETS IMMEDIATE RESPONSE - REQUEST COMPLETE
═══════════════════════════════════════════════════════════════════════

Meanwhile, in a SEPARATE THREAD (asynchronous)...

┌─────────────────────────────────────────────────────────────────────┐
│ 6. ASYNC TICKET PROCESSOR picks up event                           │
│    - Runs in background thread pool                                │
│    - Updates ticket status to "PROCESSING"                          │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 7. GEMINI CLASSIFICATION SERVICE is called                         │
│    - Constructs AI prompt with ticket details                      │
│    - Sends HTTP POST to Gemini REST API                            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 8. GEMINI API ANALYZES THE TICKET (2-5 seconds)                    │
│                                                                      │
│    Prompt sent to Gemini:                                           │
│    ┌──────────────────────────────────────────────────────────┐   │
│    │ Analyze this customer support ticket:                    │   │
│    │                                                           │   │
│    │ Subject: App crashes on startup                          │   │
│    │ Description: The app crashes when I open it              │   │
│    │                                                           │   │
│    │ Classify it with:                                        │   │
│    │ - CATEGORY (BILLING, BUG, TECH_SUPPORT, etc.)           │   │
│    │ - PRIORITY (LOW, MEDIUM, HIGH, URGENT)                   │   │
│    │ - SENTIMENT (1-10 scale)                                 │   │
│    │ - REASONING (why you chose these)                        │   │
│    └──────────────────────────────────────────────────────────┘   │
│                                                                      │
│    Gemini AI Response:                                              │
│    ┌──────────────────────────────────────────────────────────┐   │
│    │ CATEGORY: BUG                                            │   │
│    │ PRIORITY: HIGH                                           │   │
│    │ SENTIMENT: 3                                             │   │
│    │ REASONING: User reports app crash on startup which is    │   │
│    │ a critical functional issue preventing app usage.        │   │
│    │ Requires immediate attention. Low sentiment due to       │   │
│    │ frustration with non-working app.                        │   │
│    └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 9. PARSE GEMINI RESPONSE                                           │
│    - Extracts CATEGORY: BUG                                         │
│    - Extracts PRIORITY: HIGH                                        │
│    - Extracts SENTIMENT: 3                                          │
│    - Extracts REASONING: "User reports app crash..."               │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 10. UPDATE TICKET IN DATABASE                                      │
│     - Status: CLASSIFIED                                            │
│     - Category: BUG                                                 │
│     - Priority: HIGH                                                │
│     - Sentiment: 3                                                  │
│     - Updated timestamp                                             │
└─────────────────────────────────────────────────────────────────────┘

═══════════════════════════════════════════════════════════════════════
        CLASSIFICATION COMPLETE (Total: 3-5 seconds)
═══════════════════════════════════════════════════════════════════════
```

## What Gemini API Actually Does

### Input to Gemini
The system sends a structured prompt to Gemini with:
- **Ticket Subject**: "App crashes on startup"
- **Ticket Description**: Full description text
- **Instructions**: Classify into category, priority, sentiment

### Gemini's AI Analysis
Gemini uses natural language processing to:

1. **Understand Context**
   - Identifies keywords like "crashes", "startup", "app"
   - Recognizes this is a technical problem
   - Detects urgency from the description

2. **Categorize the Issue**
   - Determines it's a BUG (not billing, not a question)
   - Recognizes it's a functional failure

3. **Assess Priority**
   - App crashes = critical issue
   - Prevents user from using the app
   - Assigns HIGH priority

4. **Analyze Sentiment**
   - Detects frustration in the language
   - Rates sentiment as 3 (negative)
   - Scale: 1 (very negative) to 10 (very positive)

5. **Provide Reasoning**
   - Explains why it chose these classifications
   - Helps support team understand the AI's decision

### Output from Gemini
Gemini returns structured text like:
```
CATEGORY: BUG
PRIORITY: HIGH
SENTIMENT: 3
REASONING: User reports app crash on startup which is a critical 
functional issue preventing app usage. Requires immediate attention.
```

## Real Example from Your System

Here's an actual Gemini API response from your logs:

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "CATEGORY: BUG\nPRIORITY: HIGH\nSENTIMENT: 3\nREASONING: The user is reporting a functional issue (login button not working) that prevents them from using the service. The mention of trying multiple browsers suggests it's not a user-specific configuration issue but a potential bug. The \"ASAP!\" indicates urgency. The sentiment is low due to frustration with the non-functional feature."
          }
        ]
      }
    }
  ],
  "usageMetadata": {
    "promptTokenCount": 155,
    "candidatesTokenCount": 84,
    "totalTokenCount": 239
  },
  "modelVersion": "gemini-2.5-flash-lite"
}
```

## Why Asynchronous Processing?

### Without Async (❌ Bad)
```
User waits → Ticket saved → Call Gemini (3-5s) → Get response → Return
Total wait: 3-5 seconds for user!
```

### With Async (✅ Good)
```
User waits → Ticket saved → Return immediately (< 100ms)
                          ↓
                    Background: Call Gemini → Update ticket
```

**Benefits**:
- ✅ User gets instant response
- ✅ Better user experience
- ✅ Can handle multiple requests simultaneously
- ✅ System doesn't block on AI processing

## Timeline

| Time | What's Happening |
|------|------------------|
| **0ms** | User submits POST request |
| **50ms** | Ticket saved to database with status=PENDING |
| **80ms** | Event published to async processor |
| **100ms** | **User receives response** ← Request complete! |
| **150ms** | Async processor picks up event |
| **200ms** | Status updated to PROCESSING |
| **250ms** | HTTP request sent to Gemini API |
| **3000ms** | Gemini analyzes and responds |
| **3100ms** | Response parsed |
| **3200ms** | Ticket updated with classification |
| **3200ms** | Status changed to CLASSIFIED ✅ |

## How to See This in Action

1. **Create a ticket** via Swagger UI
2. **Immediately check** the ticket - you'll see `status: "PENDING"`
3. **Wait 5 seconds**
4. **Check again** - now you'll see `status: "CLASSIFIED"` with all fields filled!

## Key Files

- **GeminiClassificationService.java** - Handles Gemini API calls and response parsing
- **AsyncTicketProcessor.java** - Processes tickets asynchronously
- **TicketService.java** - Publishes classification events

## Summary

**Gemini API's Job**: Analyze the ticket text and intelligently classify it into:
- **Category** (what type of issue)
- **Priority** (how urgent)
- **Sentiment** (how the customer feels)
- **Reasoning** (why it made these choices)

This happens **automatically** and **asynchronously** after every ticket is created, making your support team's job much easier! 🎯
