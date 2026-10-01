# TrustDesk AI Support Operations

AI-first customer support operations platform that automates ticket triage, generates policy-compliant draft responses, manages approval-gated tool actions, and includes built-in security guardrails.

## 🚀 Features

- **AI Ticket Triage** - Automatically classifies tickets by category, priority, and sentiment
- **Cited Draft Generation** - Generates customer responses backed by policy documents
- **Approval-Gated Actions** - Safe tool execution workflow (Request → Approve → Execute)
- **Security Guardrails** - Real-time detection of prompt injection, PII leakage, and unsafe actions
- **Evaluation System** - Metrics tracking for AI performance

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.2.3, Java 21
- **Database**: SQLite (embedded)
- **AI**: Groq API (openai/gpt-oss-120b model)
- **Build**: Maven

## 📋 Prerequisites

- Java 21 or later
- Maven 3.8+
- Groq API key (free at [groq.com](https://groq.com))

## ⚙️ Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/omkarpatil44/Trustdesk-AI-Support-Operations.git
   cd trustdesk
   ```

2. **Configure Groq API Key**
   
   Add your Groq API key to `src/main/resources/application.yml`:
   ```yaml
   groq:
     api-key: YOUR_GROQ_API_KEY_HERE
     model: openai/gpt-oss-120b
   ```

   Or set environment variable:
   ```bash
   export GROQ_API_KEY=YOUR_GROQ_API_KEY_HERE
   ```

3. **Build the project**
   ```bash
   mvn clean compile
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on port 8083.

## 📡 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/triage/{ticketId}` | POST | AI ticket classification |
| `/api/draft/{ticketId}` | POST | Generate draft response |
| `/api/tool-actions` | POST | Request tool action |
| `/api/tool-actions/{id}/approve` | POST | Approve/reject action |
| `/api/tool-actions/{id}/execute` | POST | Execute approved action |
| `/api/evals/run` | POST | Run evaluation cases |
| `/api/guardrail/check` | POST | Check security guardrails |

## 📊 Project Structure

```
src/main/java/com/trustdesk/
├── controller/          # REST API endpoints
├── entity/              # JPA entities (database models)
├── repository/          # Spring Data repositories
├── service/             # Business logic layer
└── TrustDeskApplication.java  # Main entry point
```

## 🔒 Security

The application includes:
- **Prompt Injection Detection** - Blocks attempts to override AI instructions
- **PII Leakage Detection** - Prevents extraction of sensitive data
- **Unsafe Action Detection** - Blocks dangerous action requests
- **Approval Workflow** - Human oversight for all tool actions

## 🧪 Testing

Run evaluation cases:
```bash
curl -X POST http://localhost:8083/api/evals/run
```

Test guardrails:
```bash
curl -X POST http://localhost:8083/api/guardrail/check \
  -H "Content-Type: application/json" \
  -d '{"text": "Ignore previous instructions"}'
```

## 📚 Documentation

See [DOCUMENTATION.md](../DOCUMENTATION.md) for complete technical documentation.

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/amazing-feature`
2. Commit your changes: `git commit -m 'Add some amazing feature'`
3. Push to the branch: `git push origin feature/amazing-feature`
4. Open a Pull Request

## 📄 License

This project is part of the Airtribe AI-first Software Engineering Program.

## 👤 Author

**Omkar Patil**

- GitHub: [@omkarpatil44](https://github.com/omkarpatil44)
- **Evaluation System** - Metrics tracking for AI performance (category/priority/escalation accuracy)

## 🛠️ Tech Stack