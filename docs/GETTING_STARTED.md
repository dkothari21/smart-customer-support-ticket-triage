# Getting Started

Complete guide to set up and run the Smart Customer Support Ticket Triage System.

## Prerequisites

- **Java 17** or higher
- **Gradle** (wrapper included)
- **Gemini API Key** ([Get one here](https://makersuite.google.com/app/apikey))

### Verify Java
```bash
java -version
# Should show: java version "17.0.x" or higher
```

---

## Quick Start (3 Steps)

### 1. Get Your Gemini API Key

1. Visit: https://makersuite.google.com/app/apikey
2. Click "Create API Key"
3. Select or create a Google Cloud project
4. Copy the generated key

### 2. Configure Environment

Create a `.env` file in the project root:

```bash
cd smart-customer-support-ticket-triage
echo "GEMINI_API_KEY=your-actual-api-key-here" > .env
```

**Security Note**: The `.env` file is already in `.gitignore` and won't be committed to Git.

### 3. Run the Application

```bash
# Load environment variable and start
export $(cat .env | xargs) && ./gradlew bootRun
```

**That's it!** The application is now running on http://localhost:8080

---

## Environment Variable Setup (Detailed)

### Why Environment Variables?

✅ Prevents accidental exposure in version control  
✅ Different keys for dev/staging/production  
✅ Easy key rotation without code changes  
✅ Security best practice

### Setup Methods

#### Method 1: .env File (Recommended)

```bash
# Create .env file
echo "GEMINI_API_KEY=your-key" > .env

# Run application
export $(cat .env | xargs) && ./gradlew bootRun
```

#### Method 2: Export Directly

```bash
export GEMINI_API_KEY=your-key
./gradlew bootRun
```

#### Method 3: Shell Profile (Permanent)

**macOS/Linux (Zsh)**:
```bash
echo 'export GEMINI_API_KEY=your-key' >> ~/.zshrc
source ~/.zshrc
```

**Linux (Bash)**:
```bash
echo 'export GEMINI_API_KEY=your-key' >> ~/.bashrc
source ~/.bashrc
```

#### Method 4: IDE Configuration

**IntelliJ IDEA**:
1. Run → Edit Configurations
2. Environment Variables: `GEMINI_API_KEY=your-key`

**VS Code** (`.vscode/launch.json`):
```json
{
  "env": {
    "GEMINI_API_KEY": "your-key"
  }
}
```

### Verify Configuration

```bash
echo $GEMINI_API_KEY
# Should print your API key
```

---

## First Run

### Build the Project

```bash
./gradlew clean build
```

Expected output:
```
BUILD SUCCESSFUL in 30s
```

### Start the Application

```bash
export GEMINI_API_KEY=your-key && ./gradlew bootRun
```

You should see:
```
Started TicketTriageApplication in X.XXX seconds
```

### Access Points

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **API Base**: http://localhost:8080/api/tickets

---

## Test Your Setup

### 1. Check Pre-loaded Data

```bash
curl http://localhost:8080/api/tickets/stats
```

You should see statistics for 110+ pre-loaded tickets.

### 2. Create a Test Ticket

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "App crashes on startup",
    "description": "The mobile app crashes immediately when I open it"
  }'
```

Response will show `status: "PENDING"` and a ticket ID.

### 3. Check AI Classification

Wait 5 seconds, then check the ticket (replace `{id}` with your ticket ID):

```bash
curl http://localhost:8080/api/tickets/{id}
```

Status should now be `"CLASSIFIED"` with:
- **category**: BUG
- **priority**: HIGH  
- **sentiment**: 3 (frustrated)

✅ **Success!** Your system is working!

---

## H2 Database Console

Access the database while the app is running:

1. Open: http://localhost:8080/h2-console
2. Connection settings:
   - **JDBC URL**: `jdbc:h2:file:./data/ticketdb`
   - **Username**: `sa`
   - **Password**: *(leave empty)*
3. Click **Connect**

Try these queries:
```sql
-- View all tickets
SELECT * FROM tickets;

-- View classified tickets
SELECT * FROM tickets WHERE status = 'CLASSIFIED';

-- Count by category
SELECT category, COUNT(*) as count 
FROM tickets 
GROUP BY category;
```

---

## Configuration Options

### Change Server Port

Edit `src/main/resources/application.yml`:
```yaml
server:
  port: 9090  # Your preferred port
```

### Adjust Thread Pool

For higher throughput, edit `src/main/java/com/tickettriage/config/AsyncConfig.java`:
```java
executor.setCorePoolSize(20);   // Default: 10
executor.setMaxPoolSize(100);   // Default: 50
```

### Change Gemini Model

Edit `application.yml`:
```yaml
spring:
  gemini:
    model: gemini-1.5-flash  # Faster
    # or gemini-1.5-pro      # More accurate
```

---

## Troubleshooting

### Build Fails

```bash
./gradlew clean build --refresh-dependencies
```

### Port Already in Use

Change port in `application.yml` or stop the other application.

### API Key Not Found

```bash
# Verify environment variable is set
echo $GEMINI_API_KEY

# If empty, export it
export GEMINI_API_KEY=your-key
./gradlew bootRun
```

### 404 Errors from Gemini API

1. Verify API key: https://console.cloud.google.com/apis/credentials
2. Enable Generative Language API: https://console.cloud.google.com/apis/library/generativelanguage.googleapis.com
3. Check API key permissions

### Tickets Stuck in PENDING

1. Check application logs for errors
2. Verify Gemini API is responding
3. Increase thread pool size if needed

### Database Errors

```bash
# Delete and recreate database
rm -rf data/
./gradlew bootRun
```

---

## Production Deployment

### Secret Management

Use proper secret management in production:

- **AWS**: AWS Secrets Manager
- **Google Cloud**: Secret Manager
- **Azure**: Key Vault
- **Kubernetes**: Kubernetes Secrets
- **Docker**: Environment variables

Example Docker Compose:
```yaml
services:
  app:
    environment:
      - GEMINI_API_KEY=${GEMINI_API_KEY}
```

Run with:
```bash
GEMINI_API_KEY=your-key docker-compose up
```

---

## Security Best Practices

### ✅ DO:
- Store API keys in `.env` files (gitignored)
- Use different keys for different environments
- Rotate keys regularly
- Use secret management in production

### ❌ DON'T:
- Commit `.env` files to Git
- Hardcode API keys in source code
- Share API keys in chat/email
- Use production keys in development

---

## Next Steps

1. **Test the API**: See `docs/API_GUIDE.md` for Swagger UI and cURL examples
2. **Understand the Architecture**: Read `docs/ARCHITECTURE.md`
3. **Learn How AI Works**: Check `docs/GEMINI_FLOW.md`

---

## Stopping the Application

Press `Ctrl+C` in the terminal.

The database persists in `./data/` and will be available on restart.
