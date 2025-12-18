# Testing the API with Swagger UI

## Access Swagger UI

1. **Open your browser** and navigate to:
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. You should see the **Smart Customer Support Ticket Triage API** documentation

---

## Test 1: Create a New Ticket

### Step 1: Expand the POST endpoint
- Find **"POST /api/tickets"** under "Ticket Management"
- Click on it to expand the endpoint details

### Step 2: Try it out
- Click the blue **"Try it out"** button on the right

### Step 3: Enter request body
- In the **Request body** text area, paste this JSON:

```json
{
  "subject": "App crashes on startup",
  "description": "The mobile app crashes immediately after I open it. Tried reinstalling but same issue. Using iPhone 15 Pro with iOS 17.5"
}
```

### Step 4: Execute the request
- Click the **"Execute"** button
- Wait for the response

### Step 5: Check the response
You should see:
- **Response Code**: `201 Created`
- **Response Body** showing:
  ```json
  {
    "id": 998,
    "subject": "App crashes on startup",
    "description": "The mobile app crashes...",
    "status": "PENDING",
    "category": null,
    "priority": null,
    "sentiment": null,
    "createdAt": "2025-12-18T...",
    "updatedAt": "2025-12-18T...",
    "errorMessage": null
  }
  ```

**Important**: Note the `id` value (e.g., `998`) - you'll need this for the next test!

---

## Test 2: Check Classification Status

### Step 6: Wait for processing
- **Wait 5-10 seconds** for the AI to classify the ticket asynchronously

### Step 7: Expand the GET endpoint
- Scroll up and find **"GET /api/tickets/{id}"**
- Click on it to expand

### Step 8: Try it out
- Click the blue **"Try it out"** button

### Step 9: Enter the ticket ID
- In the **id** parameter field, enter the ticket ID you noted earlier (e.g., `998`)

### Step 10: Execute the request
- Click the **"Execute"** button

### Step 11: Verify classification
You should now see the ticket is **CLASSIFIED**:
```json
{
  "id": 998,
  "subject": "App crashes on startup",
  "description": "The mobile app crashes...",
  "status": "CLASSIFIED",
  "category": "BUG",
  "priority": "HIGH",
  "sentiment": 2,
  "createdAt": "2025-12-18T...",
  "updatedAt": "2025-12-18T...",
  "errorMessage": null
}
```

✅ **Success!** The ticket was automatically classified by Gemini AI:
- **Category**: BUG (detected it's a technical issue)
- **Priority**: HIGH (app crashes are critical)
- **Sentiment**: 2 (user is frustrated)

---

## Test 3: View All Tickets

### Step 12: Test the list endpoint
- Find **"GET /api/tickets"** (without the {id})
- Click **"Try it out"**
- Optionally filter by:
  - **status**: `CLASSIFIED`
  - **category**: `BUG`
  - **priority**: `HIGH`
- Click **"Execute"**

You'll see a list of all tickets matching your filters!

---

## Test 4: View Statistics

### Step 13: Check ticket stats
- Find **"GET /api/tickets/stats"**
- Click **"Try it out"**
- Click **"Execute"**

You'll see aggregated statistics:
```json
{
  "totalTickets": 998,
  "byStatus": {
    "PENDING": 10,
    "PROCESSING": 2,
    "CLASSIFIED": 985,
    "FAILED": 1
  },
  "byCategory": {
    "BUG": 245,
    "BILLING": 189,
    "TECH_SUPPORT": 312,
    "FEATURE_REQUEST": 156,
    "GENERAL": 83
  },
  "byPriority": {
    "LOW": 234,
    "MEDIUM": 456,
    "HIGH": 234,
    "URGENT": 61
  }
}
```

---

## What to Expect

### Immediate Response (< 1 second)
- Ticket created with `status: "PENDING"`
- Returns immediately (non-blocking)

### Async Processing (3-5 seconds)
- Ticket status changes to `"PROCESSING"`
- Gemini AI analyzes the ticket
- Classification results are saved

### Final Result
- Ticket status changes to `"CLASSIFIED"`
- `category`, `priority`, and `sentiment` are populated
- AI reasoning is stored

---

## Troubleshooting

### If ticket stays PENDING
- Check the application logs in your terminal
- Verify `GEMINI_API_KEY` environment variable is set
- Make sure the application is running

### If you get a 404 error
- Ensure the application is running on port 8080
- Check that you're using the correct URL

### If classification fails
- Check the `errorMessage` field in the response
- Look at application logs for detailed error messages

---

## Next Steps

Try creating tickets with different subjects to see how Gemini classifies them:

**Billing Issue**:
```json
{
  "subject": "Charged twice this month",
  "description": "I see two charges of $49.99 on my credit card"
}
```
Expected: `BILLING`, `HIGH` priority

**Feature Request**:
```json
{
  "subject": "Add dark mode please",
  "description": "Would love to have a dark mode option for night use"
}
```
Expected: `FEATURE_REQUEST`, `LOW` or `MEDIUM` priority

**General Question**:
```json
{
  "subject": "How do I change my password?",
  "description": "I forgot my password and need help resetting it"
}
```
Expected: `TECH_SUPPORT` or `GENERAL`, `MEDIUM` priority
