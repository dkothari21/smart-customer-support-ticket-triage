# Smart Customer Support Ticket Triage System

An intelligent customer support ticket classification system built with Spring Boot that automatically categorizes, prioritizes, and assigns sentiment scores to support tickets using Google Gemini AI.

## 🚀 Features

- **Instant Ticket Submission**: REST API accepts tickets and returns immediately with PENDING status
- **Asynchronous AI Processing**: Tickets are classified in the background using Spring Events
- **Gemini AI Integration**: Powered by Google's Gemini 1.5 Flash for accurate classification
- **Automatic Categorization**: Classifies tickets into BILLING, TECH_SUPPORT, BUG, FEATURE_REQUEST, or GENERAL
- **Smart Prioritization**: Assigns priority levels (LOW, MEDIUM, HIGH, URGENT)
- **Sentiment Analysis**: Rates customer sentiment on a 1-10 scale
- **Real-time Statistics**: Track classification metrics and ticket distribution
- **H2 Database**: Embedded database for easy development and testing

## 🏗️ Architecture

This system implements an **event-driven architecture** using Spring's built-in capabilities:

```
┌─────────────────┐      ┌──────────────┐      ┌─────────────────┐
│  REST API       │─────▶│   Spring     │─────▶│  AI Processor   │
│  (Producer)     │      │   Events     │      │  (Consumer)     │
└─────────────────┘      └──────────────┘      └─────────────────┘
        │                                               │
        ▼                                               ▼
   Returns PENDING                              Classifies with Gemini
   immediately                                  Updates to CLASSIFIED
```

**Producer**: `TicketService` creates tickets and publishes events  
**Consumer**: `AsyncTicketProcessor` listens for events and processes tickets asynchronously  
**Message Broker**: Spring's `ApplicationEventPublisher` (in-memory)

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.3.6**
- **Spring AI Vertex** (Gemini integration)
- **Spring Data JPA**
- **H2 Database**
- **Lombok**
- **Gradle**

## 📋 Prerequisites

- Java 17 or higher
- Google Cloud account with Gemini API access
- Gemini API key

## 🚦 Quick Start

### 1. Clone the repository
```bash
cd smart-customer-support-ticket-triage
```

### 2. Configure Gemini API
The API key is already configured in `src/main/resources/application.yml`. If you need to change it:
```yaml
spring:
  ai:
    vertex:
      ai:
        gemini:
          api-key: YOUR_API_KEY_HERE
```

### 3. Build the project
```bash
./gradlew clean build
```

### 4. Run the application
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## 📚 Documentation

- **[Setup Guide](docs/SETUP.md)** - Detailed installation and configuration
- **[Architecture Overview](docs/ARCHITECTURE.md)** - System design and components
- **[API Testing Guide](docs/API_TESTING.md)** - How to test all endpoints

## 🧪 Testing the System

### Submit a ticket
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "My credit card was charged twice!",
    "description": "I was charged $99.99 twice for the same subscription. Please refund one charge immediately."
  }'
```

### Check ticket status
```bash
curl http://localhost:8080/api/tickets/1
```

### View statistics
```bash
curl http://localhost:8080/api/tickets/stats
```

## 📊 Sample Dataset

The application comes pre-loaded with **110+ realistic support tickets** covering:
- 30 Billing issues
- 30 Technical support requests
- 25 Bug reports
- 15 Feature requests
- 10 General inquiries

## 🗄️ Database Access

Access the H2 console at: `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:file:./data/ticketdb`
- **Username**: `sa`
- **Password**: *(leave empty)*

## 📖 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tickets` | Submit a new ticket |
| GET | `/api/tickets/{id}` | Get ticket by ID |
| GET | `/api/tickets` | List all tickets |
| GET | `/api/tickets?status=PENDING` | Filter by status |
| GET | `/api/tickets?category=BILLING` | Filter by category |
| GET | `/api/tickets?priority=HIGH` | Filter by priority |
| GET | `/api/tickets/stats` | Get classification statistics |

## 🔄 How It Works

1. **User submits a ticket** via REST API
2. **System saves ticket** with status `PENDING` and returns immediately
3. **Event is published** to Spring's event system
4. **Async processor picks up the event** in a separate thread
5. **Gemini AI analyzes** the ticket content
6. **System updates ticket** with category, priority, and sentiment
7. **Status changes to** `CLASSIFIED`

## 🎯 Event-Driven Architecture

This system demonstrates event-driven principles:
- ✅ **Decoupling**: Producer and consumer are independent
- ✅ **Asynchronous**: Non-blocking ticket submission
- ✅ **Scalable**: Thread pool handles concurrent processing
- ✅ **Resilient**: Failed classifications are tracked

## 🔧 Configuration

Key configuration options in `application.yml`:

```yaml
# Thread pool for async processing
spring:
  task:
    execution:
      pool:
        core-size: 10
        max-size: 50

# Gemini model settings
spring:
  ai:
    vertex:
      ai:
        gemini:
          chat:
            options:
              model: gemini-1.5-flash
              temperature: 0.3
```

## 📝 License

This project is created for educational purposes.

## 👨‍💻 Author

Built with ❤️ using Spring Boot and Google Gemini AI
