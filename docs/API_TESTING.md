# API Testing Guide

This guide provides step-by-step instructions for testing all API endpoints of the Smart Customer Support Ticket Triage System.

## Prerequisites

1. Application is running on `http://localhost:8080`
2. You have `curl` installed (or use Postman/Insomnia)

## Testing Workflow

### 1. Submit a New Ticket

**Endpoint**: `POST /api/tickets`

**Example 1: Billing Issue**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Double charge on my credit card",
    "description": "I was charged twice for my subscription this month. Transaction IDs: TXN123 and TXN124. Please refund one of them immediately!"
  }'
```

**Expected Response** (immediate):
```json
{
  "id": 111,
  "subject": "Double charge on my credit card",
  "description": "I was charged twice for my subscription...",
  "status": "PENDING",
  "category": null,
  "priority": null,
  "sentiment": null,
  "createdAt": "2025-12-18T09:50:00",
  "updatedAt": "2025-12-18T09:50:00",
  "errorMessage": null
}
```

**Example 2: Technical Support**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Cannot login to my account",
    "description": "I keep getting Invalid credentials error even though I am using the correct password. This is very frustrating!"
  }'
```

**Example 3: Bug Report**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "App crashes on startup",
    "description": "The mobile app crashes immediately after I open it. I am using iPhone 14 with iOS 17. This is a critical bug!"
  }'
```

**Example 4: Feature Request**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Add dark mode support",
    "description": "Please add a dark mode option. The bright white interface hurts my eyes at night. This would be a great feature!"
  }'
```

### 2. Get Ticket by ID

**Endpoint**: `GET /api/tickets/{id}`

Wait 2-3 seconds after creating a ticket, then check its status:

```bash
curl http://localhost:8080/api/tickets/111
```

**Expected Response** (after classification):
```json
{
  "id": 111,
  "subject": "Double charge on my credit card",
  "description": "I was charged twice for my subscription...",
  "status": "CLASSIFIED",
  "category": "BILLING",
  "priority": "HIGH",
  "sentiment": 2,
  "createdAt": "2025-12-18T09:50:00",
  "updatedAt": "2025-12-18T09:50:03",
  "errorMessage": null
}
```

**Key Observations**:
- ✅ Status changed from `PENDING` to `CLASSIFIED`
- ✅ Category identified as `BILLING`
- ✅ Priority set to `HIGH` (urgent billing issue)
- ✅ Sentiment is `2` (very negative due to "immediately!")
- ✅ `updatedAt` timestamp changed

### 3. List All Tickets

**Endpoint**: `GET /api/tickets`

```bash
curl http://localhost:8080/api/tickets
```

This returns all tickets (including the 110 pre-loaded ones).

**Response**: Array of ticket objects
```json
[
  {
    "id": 1,
    "subject": "Double charge on my credit card",
    "status": "PENDING",
    ...
  },
  {
    "id": 2,
    "subject": "Cannot update payment method",
    "status": "PENDING",
    ...
  }
  // ... more tickets
]
```

### 4. Filter Tickets by Status

**Endpoint**: `GET /api/tickets?status={STATUS}`

**Get all pending tickets**:
```bash
curl http://localhost:8080/api/tickets?status=PENDING
```

**Get all classified tickets**:
```bash
curl http://localhost:8080/api/tickets?status=CLASSIFIED
```

**Get all failed tickets**:
```bash
curl http://localhost:8080/api/tickets?status=FAILED
```

**Available statuses**: `PENDING`, `PROCESSING`, `CLASSIFIED`, `FAILED`

### 5. Filter Tickets by Category

**Endpoint**: `GET /api/tickets?category={CATEGORY}`

**Get all billing tickets**:
```bash
curl http://localhost:8080/api/tickets?category=BILLING
```

**Get all technical support tickets**:
```bash
curl http://localhost:8080/api/tickets?category=TECH_SUPPORT
```

**Get all bug reports**:
```bash
curl http://localhost:8080/api/tickets?category=BUG
```

**Available categories**: `BILLING`, `TECH_SUPPORT`, `BUG`, `FEATURE_REQUEST`, `GENERAL`

### 6. Filter Tickets by Priority

**Endpoint**: `GET /api/tickets?priority={PRIORITY}`

**Get all urgent tickets**:
```bash
curl http://localhost:8080/api/tickets?priority=URGENT
```

**Get all high priority tickets**:
```bash
curl http://localhost:8080/api/tickets?priority=HIGH
```

**Available priorities**: `LOW`, `MEDIUM`, `HIGH`, `URGENT`

### 7. Get Classification Statistics

**Endpoint**: `GET /api/tickets/stats`

```bash
curl http://localhost:8080/api/tickets/stats
```

**Expected Response**:
```json
{
  "totalTickets": 110,
  "byStatus": {
    "PENDING": 110,
    "PROCESSING": 0,
    "CLASSIFIED": 0,
    "FAILED": 0
  },
  "byCategory": {
    "BILLING": 0,
    "TECH_SUPPORT": 0,
    "BUG": 0,
    "FEATURE_REQUEST": 0,
    "GENERAL": 0
  },
  "byPriority": {
    "LOW": 0,
    "MEDIUM": 0,
    "HIGH": 0,
    "URGENT": 0
  }
}
```

> [!NOTE]
> Initially all tickets are `PENDING`. As the async processor classifies them, the statistics will update.

## Testing Async Processing

### Observe Real-Time Classification

Run this script to watch tickets being classified in real-time:

```bash
#!/bin/bash
# Watch ticket classification progress

echo "Watching ticket classification..."
while true; do
  clear
  echo "=== Ticket Classification Statistics ==="
  curl -s http://localhost:8080/api/tickets/stats | jq '.'
  echo ""
  echo "Press Ctrl+C to stop"
  sleep 2
done
```

Save as `watch-stats.sh`, make executable, and run:
```bash
chmod +x watch-stats.sh
./watch-stats.sh
```

### Submit Multiple Tickets Rapidly

Test concurrent processing:

```bash
#!/bin/bash
# Submit 10 tickets rapidly

for i in {1..10}; do
  curl -X POST http://localhost:8080/api/tickets \
    -H "Content-Type: application/json" \
    -d "{
      \"subject\": \"Test ticket $i\",
      \"description\": \"This is test ticket number $i for load testing\"
    }" &
done

wait
echo "All tickets submitted!"
```

## Validation Test Cases

### Test Case 1: Billing Classification

**Input**:
```json
{
  "subject": "Refund request",
  "description": "I want a refund for my subscription. I was charged $99 but did not use the service."
}
```

**Expected Classification**:
- Category: `BILLING`
- Priority: `MEDIUM` or `HIGH`
- Sentiment: `3-5` (neutral to slightly negative)

### Test Case 2: Urgent Technical Issue

**Input**:
```json
{
  "subject": "URGENT: Production system down",
  "description": "Our production system is completely down! All users are affected. This is critical!"
}
```

**Expected Classification**:
- Category: `TECH_SUPPORT` or `BUG`
- Priority: `URGENT`
- Sentiment: `1-2` (very negative)

### Test Case 3: Positive Feature Request

**Input**:
```json
{
  "subject": "Love your product! Feature suggestion",
  "description": "I absolutely love using your product! It would be even better if you could add export to PDF feature. Thanks!"
}
```

**Expected Classification**:
- Category: `FEATURE_REQUEST`
- Priority: `LOW` or `MEDIUM`
- Sentiment: `8-10` (very positive)

### Test Case 4: General Inquiry

**Input**:
```json
{
  "subject": "Question about pricing",
  "description": "What is the difference between the Pro and Premium plans? I am trying to decide which one to purchase."
}
```

**Expected Classification**:
- Category: `GENERAL`
- Priority: `LOW` or `MEDIUM`
- Sentiment: `5-7` (neutral to positive)

## Error Handling Tests

### Test Invalid Input

**Missing subject**:
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "description": "This has no subject"
  }'
```

**Expected**: HTTP 400 Bad Request

**Subject too long** (> 500 characters):
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "'$(python3 -c 'print("A" * 501)')'",
    "description": "Test"
  }'
```

**Expected**: HTTP 400 Bad Request

### Test Non-Existent Ticket

```bash
curl http://localhost:8080/api/tickets/99999
```

**Expected**: HTTP 500 with error message (or 404 if you add exception handling)

## Performance Testing

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
    DURATION=$((END - START))
    echo "Classification completed in $DURATION seconds"
    break
  fi
  sleep 0.5
done
```

## Using Postman

If you prefer a GUI, import these requests into Postman:

### Collection Structure
```
Smart Ticket Triage
├── Submit Ticket (POST)
├── Get Ticket by ID (GET)
├── List All Tickets (GET)
├── Filter by Status (GET)
├── Filter by Category (GET)
├── Filter by Priority (GET)
└── Get Statistics (GET)
```

### Environment Variables
```
base_url: http://localhost:8080
ticket_id: 1
```

## Troubleshooting

### Tickets Stuck in PENDING

**Check application logs**:
```bash
tail -f logs/application.log
```

Look for errors in the async processor.

### Classification Taking Too Long

**Possible causes**:
1. Gemini API rate limits
2. Network latency
3. Thread pool exhausted

**Solution**: Check logs and increase thread pool size if needed.

### Unexpected Classifications

**Example**: Billing ticket classified as GENERAL

**Debugging**:
1. Check the Gemini response in logs (DEBUG level)
2. Verify prompt is clear
3. Try adjusting temperature in `application.yml`

## Next Steps

- Explore the [Architecture Documentation](ARCHITECTURE.md) to understand how it works
- Check the H2 console to see database changes in real-time
- Modify the Gemini prompt to customize classification logic
- Add custom categories or priorities for your use case

## Quick Reference

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/tickets` | POST | Submit new ticket |
| `/api/tickets/{id}` | GET | Get ticket details |
| `/api/tickets` | GET | List all tickets |
| `/api/tickets?status=X` | GET | Filter by status |
| `/api/tickets?category=X` | GET | Filter by category |
| `/api/tickets?priority=X` | GET | Filter by priority |
| `/api/tickets/stats` | GET | Get statistics |
