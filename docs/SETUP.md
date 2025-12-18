# Setup Guide

This guide will walk you through setting up and running the Smart Customer Support Ticket Triage System.

## Prerequisites

### Required
- **Java 17** or higher
- **Gradle** (wrapper included, no installation needed)
- **Google Cloud Account** with Gemini API access

### Verify Java Installation
```bash
java -version
```

You should see output like:
```
java version "17.0.x"
```

## Installation Steps

### 1. Navigate to Project Directory
```bash
cd smart-customer-support-ticket-triage
```

### 2. Verify Gemini API Configuration

The API key is already configured in `src/main/resources/application.yml`:

```yaml
spring:
  ai:
    vertex:
      ai:
        gemini:
          project-id: your-project-id
          location: us-central1
          api-key: AIzaSyAnjLZcZxEdUqqoAa-jfWdvfxOZxBEhaCM
```

> [!NOTE]
> If you need to use a different API key, update the `api-key` field in `application.yml`

### 3. Build the Project

```bash
./gradlew clean build
```

This will:
- Download all dependencies
- Compile the code
- Run tests
- Create the executable JAR

Expected output:
```
BUILD SUCCESSFUL in 30s
```

### 4. Run the Application

```bash
./gradlew bootRun
```

The application will start and you should see:
```
Started TicketTriageApplication in X.XXX seconds
```

The application is now running on **http://localhost:8080**

## Accessing the H2 Database Console

While the application is running, you can access the database console:

1. Open your browser and go to: **http://localhost:8080/h2-console**

2. Use these connection settings:
   - **JDBC URL**: `jdbc:h2:file:./data/ticketdb`
   - **User Name**: `sa`
   - **Password**: *(leave empty)*

3. Click **Connect**

You can now run SQL queries to inspect the tickets:
```sql
SELECT * FROM tickets;
SELECT * FROM tickets WHERE status = 'CLASSIFIED';
SELECT category, COUNT(*) FROM tickets GROUP BY category;
```

## Verifying the Installation

### 1. Check Application Health

```bash
curl http://localhost:8080/api/tickets/stats
```

You should see statistics about the pre-loaded tickets.

### 2. Submit a Test Ticket

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Test ticket",
    "description": "This is a test to verify the system is working"
  }'
```

You should receive a response with status `PENDING`.

### 3. Check Ticket Classification

Wait a few seconds, then retrieve the ticket:

```bash
curl http://localhost:8080/api/tickets/1
```

The status should now be `CLASSIFIED` with category, priority, and sentiment filled in.

## Configuration Options

### Change Server Port

Edit `src/main/resources/application.yml`:
```yaml
server:
  port: 9090  # Change to your preferred port
```

### Adjust Thread Pool Size

For higher throughput, increase the thread pool:
```yaml
# In AsyncConfig.java
executor.setCorePoolSize(20);  # Default: 10
executor.setMaxPoolSize(100);  # Default: 50
```

### Change Gemini Model

Edit `application.yml`:
```yaml
spring:
  ai:
    vertex:
      ai:
        gemini:
          chat:
            options:
              model: gemini-1.5-pro  # More accurate but slower
              # or gemini-1.5-flash (faster, default)
```

## Troubleshooting

### Build Fails

**Issue**: Gradle build fails with dependency errors

**Solution**: 
```bash
./gradlew clean build --refresh-dependencies
```

### Application Won't Start

**Issue**: Port 8080 already in use

**Solution**: Either stop the other application or change the port in `application.yml`

### Gemini API Errors

**Issue**: Getting 401 Unauthorized errors

**Solution**: 
1. Verify your API key is correct in `application.yml`
2. Check that your Google Cloud project has Gemini API enabled
3. Ensure your API key has the necessary permissions

### Database Errors

**Issue**: Cannot connect to H2 database

**Solution**: 
1. Delete the `data` directory
2. Restart the application
3. The database will be recreated automatically

### Tickets Stuck in PENDING

**Issue**: Tickets are not being classified

**Solution**:
1. Check application logs for errors
2. Verify Gemini API is responding
3. Check thread pool is not exhausted (increase pool size)

## Next Steps

- Read the [Architecture Overview](ARCHITECTURE.md) to understand how the system works
- Follow the [API Testing Guide](API_TESTING.md) to test all endpoints
- Explore the pre-loaded 110+ sample tickets in the database

## Stopping the Application

Press `Ctrl+C` in the terminal where the application is running.

The database will be persisted in the `./data` directory and will be available when you restart.
