# API Guide

Complete guide for testing the Smart Customer Support Ticket Triage API using Swagger UI and cURL.

## Quick Access

- **Swagger UI**: http://localhost:8080/swagger-ui.html (Interactive testing)
- **API Base URL**: http://localhost:8080/api/tickets
- **H2 Console**: http://localhost:8080/h2-console

---

## Option 1: Swagger UI (Recommended for Beginners)

### Access Swagger

1. Open: http://localhost:8080/swagger-ui.html
2. You'll see the **Smart Customer Support Ticket Triage API** documentation

### Test 1: Create a Ticket

1. Find **POST /api/tickets** → Click to expand
2. Click **"Try it out"**
3. Paste this JSON:
   ```json
   {
     "subject": "App crashes on startup",
     "description": "The mobile app crashes immediately when I open it. Using iPhone 15 Pro."
   }
   ```
4. Click **"Execute"**
5. **Note the ticket ID** from the response (e.g., `998`)

**Response**:
```json
{
  "id": 998,
  "status": "PENDING",
  "category": null,
  "priority": null,
  "sentiment": null
}
```

### Test 2: Check Classification

1. **Wait 5 seconds** for AI processing
2. Find **GET /api/tickets/{id}** → Click to expand
3. Click **"Try it out"**
4. Enter your ticket ID (e.g., `998`)
5. Click **"Execute"**

**Response**:
```json
{
  "id": 998,
  "status": "CLASSIFIED",
  "category": "BUG",
  "priority": "HIGH",
  "sentiment": 2
}
```

✅ **Success!** The ticket was automatically classified!

### Test 3: View All Tickets

1. Find **GET /api/tickets** → Click to expand
2. Click **"Try it out"**
3. Optionally filter by:
   - **status**: `CLASSIFIED`
   - **category**: `BUG`
   - **priority**: `HIGH`
4. Click **"Execute"**

### Test 4: View Statistics

1. Find **GET /api/tickets/stats** → Click to expand
2. Click **"Try it out"**
3. Click **"Execute"**

**Response**:
```json
{
  "totalTickets": 998,
  "byStatus": { "CLASSIFIED": 985, "PENDING": 10 },
  "byCategory": { "BUG": 245, "BILLING": 189 },
  "byPriority": { "HIGH": 234, "URGENT": 61 }
}
```

---

## Option 2: cURL (Command Line)

### 1. Create a Ticket

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Double charge on my credit card",
    "description": "I was charged twice for my subscription. Please refund!"
  }'
```

### 2. Get Ticket by ID

```bash
# Wait 5 seconds, then check (replace 111 with your ticket ID)
curl http://localhost:8080/api/tickets/111
```

### 3. List All Tickets

```bash
curl http://localhost:8080/api/tickets
```

### 4. Filter by Status

```bash
# Get pending tickets
curl "http://localhost:8080/api/tickets?status=PENDING"

# Get classified tickets
curl "http://localhost:8080/api/tickets?status=CLASSIFIED"
```

### 5. Filter by Category

```bash
# Get billing tickets
curl "http://localhost:8080/api/tickets?category=BILLING"

# Get bug reports
curl "http://localhost:8080/api/tickets?category=BUG"
```

### 6. Filter by Priority

```bash
# Get urgent tickets
curl "http://localhost:8080/api/tickets?priority=URGENT"

# Get high priority tickets
curl "http://localhost:8080/api/tickets?priority=HIGH"
```

### 7. Get Statistics

```bash
curl http://localhost:8080/api/tickets/stats
```

---

## Test Cases by Category

### Billing Issue
```json
{
  "subject": "Charged twice this month",
  "description": "I see two charges of $49.99 on my credit card"
}
```
**Expected**: Category=`BILLING`, Priority=`HIGH`, Sentiment=`2-3`

### Technical Support
```json
{
  "subject": "Cannot login to my account",
  "description": "I keep getting 'Invalid credentials' error even with correct password"
}
```
**Expected**: Category=`TECH_SUPPORT`, Priority=`MEDIUM`, Sentiment=`3-4`

### Bug Report
```json
{
  "subject": "URGENT: Production system down",
  "description": "Our production system is completely down! All users affected!"
}
```
**Expected**: Category=`BUG`, Priority=`URGENT`, Sentiment=`1-2`

### Feature Request
```json
{
  "subject": "Add dark mode please",
  "description": "Would love to have a dark mode option for night use. Great product!"
}
```
**Expected**: Category=`FEATURE_REQUEST`, Priority=`LOW`, Sentiment=`7-8`

### General Inquiry
```json
{
  "subject": "Question about pricing",
  "description": "What is the difference between Pro and Premium plans?"
}
```
**Expected**: Category=`GENERAL`, Priority=`LOW`, Sentiment=`5-6`

---

## Classification Reference

### Categories
- `BILLING` - Payment, refunds, charges
- `TECH_SUPPORT` - Login, access, configuration
- `BUG` - Crashes, errors, broken features
- `FEATURE_REQUEST` - New features, improvements
- `GENERAL` - Questions, general inquiries

### Priorities
- `URGENT` - System down, critical issues
- `HIGH` - Important issues affecting users
- `MEDIUM` - Standard issues
- `LOW` - Minor issues, questions

### Sentiment Scale
- `1-3` - Negative (angry, frustrated)
- `4-6` - Neutral
- `7-10` - Positive (happy, satisfied)

---

## Advanced Testing

### Watch Classification in Real-Time

```bash
#!/bin/bash
# Save as watch-stats.sh

while true; do
  clear
  echo "=== Ticket Statistics ==="
  curl -s http://localhost:8080/api/tickets/stats | jq '.'
  echo ""
  echo "Press Ctrl+C to stop"
  sleep 2
done
```

Run:
```bash
chmod +x watch-stats.sh
./watch-stats.sh
```

### Submit Multiple Tickets

```bash
#!/bin/bash
# Submit 10 test tickets

for i in {1..10}; do
  curl -X POST http://localhost:8080/api/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"subject\": \"Test ticket $i\",
      \"description\": \"This is test ticket number $i\"
    }" &
done

wait
echo "All tickets submitted!"
```

### Measure Classification Time

```bash
#!/bin/bash
# Measure time from submission to classification

echo "Submitting ticket..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Performance test",
    "description": "Testing classification speed"
  }')

TICKET_ID=$(echo $RESPONSE | jq -r '.id')
echo "Ticket ID: $TICKET_ID"

START=$(date +%s)
while true; do
  STATUS=$(curl -s http://localhost:8080/api/tickets/$TICKET_ID | jq -r '.status')
  if [ "$STATUS" = "CLASSIFIED" ]; then
    END=$(date +%s)
    echo "Classification completed in $((END - START)) seconds"
    break
  fi
  sleep 0.5
done
```

---

## Validation Tests

### Test Invalid Input

**Missing subject**:
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"description": "No subject"}'
```
**Expected**: HTTP 400 Bad Request

**Empty description**:
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"subject": "Test", "description": ""}'
```
**Expected**: HTTP 400 Bad Request

### Test Non-Existent Ticket

```bash
curl http://localhost:8080/api/tickets/99999
```
**Expected**: Error response

---

## Troubleshooting

### Ticket Stays PENDING
- Check application logs
- Verify `GEMINI_API_KEY` is set
- Ensure application is running

### Classification Fails
- Check `errorMessage` field in response
- Look at application logs
- Verify Gemini API is accessible

### Unexpected Classifications
- Check Gemini response in logs (DEBUG level)
- Adjust temperature in `application.yml`
- Modify prompt in `GeminiClassificationService.java`

---

## API Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/tickets` | POST | Submit new ticket |
| `/api/tickets/{id}` | GET | Get ticket details |
| `/api/tickets` | GET | List all tickets |
| `/api/tickets?status=X` | GET | Filter by status |
| `/api/tickets?category=X` | GET | Filter by category |
| `/api/tickets?priority=X` | GET | Filter by priority |
| `/api/tickets/stats` | GET | Get statistics |

---

## Next Steps

- **Understand the Flow**: Read `docs/GEMINI_FLOW.md`
- **Learn the Architecture**: See `docs/ARCHITECTURE.md`
- **Customize**: Modify prompts, add categories, adjust priorities
