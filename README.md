# FraudShield 🛡️

Real-time fraud detection API that analyzes transactions in under 50ms before money moves.


Tech Stack
- Java 21 + Spring Boot 4.0.6
- MongoDB Atlas (database)
- Redis (caching)
- Apache Kafka (async events)
- JWT + BCrypt (security)
 Features
- 6-layer fraud engine (Behavioral, Mule, Device, Merchant, IP)
- Cross-bank shared blacklist
- Under 50ms transaction analysis
- 13 REST API endpoints
- Complete audit trail

 Architecture
Bank → FraudShield API → 6-Layer Engine → APPROVED/FLAGGED/BLOCKED
↓
Redis (cache) + Kafka (async) + MongoDB (storage)


API Endpoints
| Method | URL | Purpose |
|--------|-----|---------|
| POST | /api/v1/auth/register | Register bank account |
| POST | /api/v1/auth/login | Login + get JWT token |
| POST | /api/v1/transaction/analyze | Analyze transaction for fraud |
| GET | /api/v1/transaction/history/{id} | Transaction history |
| POST | /api/v1/fraud/blacklist | Add to blacklist |
| GET | /api/v1/audit/logs | View audit trail |
