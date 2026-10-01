# TrustDesk AI Support Operations - Changelog

## [1.0.0] - 2026-10-01

### Added
- AI Ticket Triage - Automatic classification by category, priority, and sentiment
- Cited Draft Generation - AI-powered response generation with policy citations
- Approval-Gated Tool Actions - Safe workflow: Request → Approve → Execute
- Security Guardrails - Real-time detection of prompt injection, PII leakage, and unsafe actions
- Evaluation Runner - Metrics tracking for AI performance (category/priority/escalation accuracy)
- Complete REST API documentation
- Sample data seeding with JSON files

### Features
- **100% Category Accuracy** - AI classifies tickets correctly
- **100% Priority Accuracy** - AI assigns correct priority levels
- **50% Escalation Accuracy** - Escalation logic needs refinement
- **Idempotency Support** - Prevents duplicate tool actions on retries
- **SQLite Database** - Embedded database for rapid development

### API Endpoints
| Endpoint | Description |
|----------|-------------|
| `/api/triage/{ticketId}` | AI ticket classification |
| `/api/draft/{ticketId}` | Generate draft response |
| `/api/tool-actions` | Request tool action |
| `/api/tool-actions/{id}/approve` | Approve/reject action |
| `/api/tool-actions/{id}/execute` | Execute approved action |
| `/api/evals/run` | Run evaluation cases |
| `/api/guardrail/check` | Check security guardrails |

### Tech Stack
- **Backend**: Spring Boot 3.2.3, Java 21
- **Database**: SQLite 3.45.1.0 (embedded)
- **AI**: Groq API (openai/gpt-oss-120b model)
- **Build**: Maven

## Planned Features
- RAG implementation for semantic search
- Real tool integration (CRM, ERP)
- Advanced analytics dashboard
- Real-time monitoring and alerts