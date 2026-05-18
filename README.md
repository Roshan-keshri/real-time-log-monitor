# 📡 Real-Time Multi-Tenant Log Monitoring System

An enterprise-grade, real-time log monitoring and alerting backend. Built with Spring Boot, this system allows multiple companies to securely send, store, and monitor their application logs in real-time using **Apache Kafka**, WebSockets, and API Keys.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)

## ✨ Core Features

* **⚡ Event-Driven & Asynchronous:** High-throughput log ingestion using **Apache Kafka**. The API responds instantly (`202 Accepted`), while logs are processed asynchronously in the background.
* **🏢 Multi-Tenant Architecture:** Data is strictly isolated. Companies can only access their own logs via secure API Keys.
* **🌊 Real-Time Streaming:** Built-in WebSocket (STOMP) server streams incoming logs instantly to connected frontend dashboards.
* **🚨 Automated Alert Engine:** Actively monitors log streams and triggers real-time alerts if specific thresholds (e.g., 5 errors in 1 minute) are breached.
* **🔐 Advanced Security:** Dual-layer security using **JWT** for user dashboard authentication and **API Keys** for machine-to-machine log ingestion.

## 🏗️ System Architecture

```mermaid
flowchart TD
    %% Styling
    classDef client fill:#f8f9fa,stroke:#ced4da,stroke-width:2px,color:#000
    classDef backend fill:#e3f2fd,stroke:#2196f3,stroke-width:2px,color:#000
    classDef db fill:#fff3e0,stroke:#ff9800,stroke-width:2px,color:#000
    classDef alert fill:#ffebee,stroke:#f44336,stroke-width:2px,color:#000
    classDef kafka fill:#f3e5f5,stroke:#9c27b0,stroke-width:2px,color:#000

    %% External Clients
    subgraph External Systems
        Producers["Microservices / Apps"]:::client
        Dashboard["Frontend Web Dashboard"]:::client
    end

    %% Backend System
    subgraph Spring Boot Backend
        Security["Security Filter (JWT/API Key)"]:::backend
        API["Log Controller (Producer)"]:::backend
        KafkaQueue[("Apache Kafka Topic")]:::kafka
        Worker["Kafka Consumer (Background)"]:::backend
        AlertEngine["Real-Time Alert Engine"]:::alert
        WebSocket["WebSocket Broker (STOMP)"]:::backend
    end

    %% Database
    Database[("PostgreSQL DB")]:::db

    %% Relationships
    Producers -- "POST /api/logs (x-api-key)" --> Security
    Dashboard -- "Login / Fetch Logs (JWT)" --> Security
    
    Security -- "Validated Request" --> API
    
    API -- "1. Instantly Queues Log (202 Accepted)" --> KafkaQueue
    KafkaQueue -- "2. Async Processing" --> Worker
    
    Worker -- "3. Save Data" --> Database
    Worker -- "4. Analyze Log" --> AlertEngine
    Worker -- "5. Stream Log" --> WebSocket
    
    AlertEngine -- "Threshold Exceeded?" --> WebSocket
    
    WebSocket -- "Live Updates" --> Dashboard
```
1. **Microservices/Clients** send logs securely using their unique `x-api-key`.
2. **Spring Boot App** intercepts requests, validates the API key, and instantly pushes the payload to an **Apache Kafka Topic**.
3. **Kafka Background Consumer** processes the queue, storing logs in PostgreSQL without blocking the main HTTP threads.
4. **Alert Engine** evaluates the log against company-specific thresholds.
5. **WebSocket Broker** broadcasts the log (and any alerts) to the authenticated React/Angular frontend in real-time.

## 🛠️ Tech Stack

* **Backend:** Java 17, Spring Boot 3, Spring Web
* **Message Broker:** Apache Kafka (Dockerized)
* **Security:** Spring Security, JWT, API Key Authentication
* **Real-Time:** Spring WebSockets, STOMP protocol
* **Database:** PostgreSQL, Spring Data JPA / Hibernate

## 🚀 Getting Started

### Prerequisites
* Java 17+
* PostgreSQL running on port `5432`
* **Docker Desktop** (Required for Kafka)

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/real-time-log-monitor.git
   cd real-time-log-monitor
   ```

2. **Configure Environment Variables:**
   This project uses environment variables for security. You must configure these before running the application:
   * `DB_USERNAME`: Your PostgreSQL username
   * `DB_PASSWORD`: Your PostgreSQL password
   * `JWT_SECRET`: A secure, 256-bit secret string for token generation

3. **Start Apache Kafka via Docker:**
   ```bash
   docker-compose up -d
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## 📡 API Reference

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user & company | None |
| `POST` | `/auth/login` | Get JWT token | None |
| `POST` | `/api/logs` | Ingest a log asynchronously | `x-api-key` header |
| `GET` | `/api/logs` | Fetch company logs | `Bearer {JWT}` |

---
## 📸 Screenshots

-> /auth/login
<img width="1772" height="906" alt="image" src="https://github.com/user-attachments/assets/8d138275-eb08-4978-8a8b-26382be3c63a" />

-> /api/logs
<img width="1827" height="965" alt="image" src="https://github.com/user-attachments/assets/687b734b-5b22-4777-96fa-642b35674da8" />

-> /api/register  
<img width="1782" height="947" alt="image" src="https://github.com/user-attachments/assets/9bad38ba-f189-45b4-8f25-b14cb0efbf4f" />

---

*Built by [Roshan Keshri](https://www.linkedin.com/in/roshan-keshri/) - Feel free to reach out!*
