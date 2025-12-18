# Smart Customer Support Ticket Triage System

AI-powered customer support ticket classification using Google Gemini and Spring Boot. Automatically categorizes, prioritizes, and analyzes sentiment of support tickets in real-time.

## ✨ Features

- **Instant Ticket Submission** - REST API returns immediately with PENDING status
- **Asynchronous AI Processing** - Background classification using Spring Events
- **Google Gemini AI** - Powered by `gemini-2.5-flash-lite` for accurate classification
- **Auto-Categorization** - BILLING, TECH_SUPPORT, BUG, FEATURE_REQUEST, GENERAL
- **Smart Prioritization** - LOW, MEDIUM, HIGH, URGENT
- **Sentiment Analysis** - 1-10 scale customer sentiment rating
- **Swagger UI** - Interactive API documentation and testing
- **110+ Test Tickets** - Pre-loaded realistic dataset

## 🚀 Quick Start

```bash
# 1. Get your Gemini API key from https://makersuite.google.com/app/apikey

# 2. Configure environment
echo "GEMINI_API_KEY=your-key-here" > .env

# 3. Run the application
export $(cat .env | xargs) && ./gradlew bootRun

# 4. Open Swagger UI
open http://localhost:8080/swagger-ui.html
```

**That's it!** Start testing with Swagger UI or see the full guide below.

## 📚 Documentation

### Getting Started
**[📖 Getting Started Guide](docs/GETTING_STARTED.md)** - Complete setup, configuration, and first run

### API Usage
**[🔌 API Guide](docs/API_GUIDE.md)** - Swagger UI and cURL testing examples

### Technical Deep Dive
- **[🏗️ Architecture](docs/ARCHITECTURE.md)** - Event-driven design and components
- **[🤖 Gemini Flow](docs/GEMINI_FLOW.md)** - How AI classification works

## 🏗️ Architecture

Event-driven architecture using Spring Events:

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

**Benefits**: Non-blocking, scalable, resilient, easy to test

## 🛠️ Technology Stack

- **Java 17** - Modern Java features
- **Spring Boot 3.3.6** - Framework
- **Google Gemini AI** - Direct REST API integration
- **Spring Data JPA** - Data access
- **H2 Database** - Embedded database
- **Swagger/OpenAPI** - API documentation
- **Lombok** - Boilerplate reduction
- **Gradle** - Build tool

## 🧪 Quick Test

### Using Swagger UI (Recommended)
1. Open http://localhost:8080/swagger-ui.html
2. Try **POST /api/tickets** with:
   ```json
   {
     "subject": "App crashes on startup",
     "description": "The app crashes when I open it"
   }
   ```
3. Wait 5 seconds
4. Check classification with **GET /api/tickets/{id}**

### Using cURL
```bash
# Submit ticket
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Charged twice this month",
    "description": "I see two charges of $49.99"
  }'

# Check status (wait 5 seconds, replace {id})
curl http://localhost:8080/api/tickets/{id}

# View statistics
curl http://localhost:8080/api/tickets/stats
```

## 📊 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tickets` | Submit new ticket |
| GET | `/api/tickets/{id}` | Get ticket details |
| GET | `/api/tickets` | List all tickets |
| GET | `/api/tickets?status=X` | Filter by status |
| GET | `/api/tickets?category=X` | Filter by category |
| GET | `/api/tickets?priority=X` | Filter by priority |
| GET | `/api/tickets/stats` | Get statistics |

## 🗄️ Database Access

H2 Console: http://localhost:8080/h2-console

- **JDBC URL**: `jdbc:h2:file:./data/ticketdb`
- **Username**: `sa`
- **Password**: *(leave empty)*

## 🔄 How It Works

1. User submits ticket → Returns immediately with `PENDING`
2. Event published → Async processor picks it up
3. Gemini AI analyzes → Classifies ticket
4. Database updated → Status changes to `CLASSIFIED`

**Average classification time**: 3-5 seconds

## 🎯 Classification Examples

| Ticket | Category | Priority | Sentiment |
|--------|----------|----------|-----------|
| "Charged twice!" | BILLING | HIGH | 2 (negative) |
| "App crashes" | BUG | HIGH | 3 (frustrated) |
| "Add dark mode" | FEATURE_REQUEST | LOW | 7 (positive) |
| "How to reset password?" | TECH_SUPPORT | MEDIUM | 5 (neutral) |

## 🔧 Configuration

Key settings in `application.yml`:

```yaml
spring:
  gemini:
    api-key: ${GEMINI_API_KEY}  # From environment variable
    model: gemini-2.5-flash-lite
    temperature: 0.3
    max-tokens: 1000
```

Thread pool (in `AsyncConfig.java`):
- Core: 10 threads
- Max: 50 threads
- Queue: 100 tasks

## 🔒 Security

- ✅ API key in environment variable (not committed)
- ✅ `.env` file in `.gitignore`
- ✅ Input validation on all endpoints
- ✅ SQL injection prevention (JPA)

## 📝 Sample Dataset

110+ pre-loaded tickets:
- 30 Billing issues
- 30 Technical support
- 25 Bug reports
- 15 Feature requests
- 10 General inquiries

## 🚦 Next Steps

1. **[Get Started](docs/GETTING_STARTED.md)** - Set up your environment
2. **[Test the API](docs/API_GUIDE.md)** - Try Swagger UI
3. **[Learn the Architecture](docs/ARCHITECTURE.md)** - Understand the design
4. **[Explore Gemini Flow](docs/GEMINI_FLOW.md)** - See how AI works

## 📄 License

MIT License - Created for educational purposes

## 👨‍💻 Author

Built with ❤️ using Spring Boot and Google Gemini AI

---

**Repository**: https://github.com/dkothari21/smart-customer-support-ticket-triage
