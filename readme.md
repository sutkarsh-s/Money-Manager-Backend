# Money Manager - Personal Finance Tracking Portal

A production-grade, enterprise-level Personal Finance Tracking Portal built with a **microservices backend** (Java Spring Boot + Python FastAPI) and a **microfrontend** React application.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                        │
│            (npm Workspaces Monorepo - Vite)              │
│  ┌─────────┬──────────┬─────────┬──────────┬─────────┐  │
│  │  Auth   │ Transact │Planning │Analytics │ Profile  │  │
│  │  MFE    │   MFE    │  MFE    │   MFE    │  MFE     │  │
│  └────┬────┴────┬─────┴────┬────┴────┬─────┴────┬────┘  │
│       └─────────┴──────────┴─────────┴──────────┘        │
│                   Shell App (Router)                      │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP (REST)
┌──────────────────────▼──────────────────────────────────┐
│              API Gateway (Spring Cloud Gateway)          │
│              JWT Validation · Routing · CORS             │
│                      Port 8080                           │
└──┬────┬────┬────┬────┬────┬────┬────┬────┬──────────────┘
   │    │    │    │    │    │    │    │    │
   ▼    ▼    ▼    ▼    ▼    ▼    ▼    ▼    ▼
┌─────┐┌─────┐┌─────┐┌─────┐┌─────┐┌─────┐┌─────┐┌─────┐┌─────┐
│Auth ││Trans││Plan ││Invst││Bank ││Analy││AI   ││Notif││Email│
│8081 ││8082 ││8083 ││8084 ││8085 ││8086 ││8087 ││8088 ││8089 │
└──┬──┘└──┬──┘└──┬──┘└──┬──┘└─────┘└──┬──┘└─────┘└─────┘└──┬──┘
   │      │      │      │             │                     │
   ▼      ▼      ▼      ▼             ▼                     ▼
┌─────────────────────────────────┐  ┌──────────────────────────┐
│         MySQL 8.4               │  │     RabbitMQ 3           │
│         Port 3307               │  │  AMQP 5672 · UI 15672   │
└─────────────────────────────────┘  └──────────────────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite 6, Tailwind CSS v4, npm Workspaces |
| API Gateway | Spring Cloud Gateway (Java 21) |
| Backend Services | Spring Boot 3.5.3 (Java 21) |
| AI Service | Python 3.12, FastAPI, scikit-learn, Prophet |
| Database | MySQL 8.4 |
| Message Broker | RabbitMQ 3 (AMQP) |
| Auth | JWT (JJWT 0.11.5), BCrypt |
| Containerization | Docker, Docker Compose |

## Features

- **Authentication** — Register, Login, Account Activation via Email, JWT-based session
- **Income & Expense Tracking** — Full CRUD with categories, recurring transactions
- **Budget Planning** — Monthly budgets per category with spending tracking
- **Savings Goals** — Set targets, track progress, manage milestones
- **Debt Management** — Track loans, EMIs, interest rates, payoff status
- **Investment Tracking** — Portfolio management with gains/losses
- **Lending** — Lend/borrow tracking with due dates
- **Dashboard & Analytics** — Summary cards, charts (pie, bar, line), financial health score
- **Report Export** — CSV, Excel, and PDF downloads
- **Profile Management** — Edit profile, change password, profile photo (Cloudinary)
- **Dark Mode** — System-aware theme toggle
- **AI Service** (scaffold) — Spending insights, anomaly detection, forecasting

## Project Structure

```
Project/
├── Money-Manager-Backend/
│   ├── common-lib/          # Shared DTOs, exceptions, JWT, security
│   ├── api-gateway/         # Spring Cloud Gateway (port 8080)
│   ├── auth-service/        # Authentication & profiles (port 8081)
│   ├── transaction-service/ # Income, expense, categories (port 8082)
│   ├── planning-service/    # Budgets, savings goals, debts (port 8083)
│   ├── investment-service/  # Investments (port 8084)
│   ├── banking-service/     # Banking integration scaffold (port 8085)
│   ├── analytics-service/   # Dashboard, reports, analytics (port 8086)
│   ├── ai-service/          # Python FastAPI AI/ML service (port 8087)
│   ├── notification-service/# Notifications scaffold (port 8088)
│   ├── email-service/       # Email via RabbitMQ consumer (port 8089)
│   ├── docker-compose.yml
│   └── .env
├── Money-Manager-Frontend/
│   ├── packages/
│   │   ├── shared/           # @mm/shared — common components, hooks, utils
│   │   ├── shell-app/        # Main app shell with routing
│   │   ├── mfe-auth/         # Login, Register, Activate pages
│   │   ├── mfe-transactions/ # Income, Expense, Categories
│   │   ├── mfe-planning/     # Budgets, Savings Goals, Debts
│   │   ├── mfe-investments/  # Investment management
│   │   ├── mfe-banking/      # Banking placeholder
│   │   ├── mfe-analytics/    # Dashboard, Reports
│   │   ├── mfe-profile/      # Profile management
│   │   └── mfe-lending/      # Lend/Borrow tracking
│   └── package.json          # npm Workspaces root
└── docs/                     # Architecture & feature documentation
```

## Quick Start

See [docs/getting-started.md](docs/getting-started.md) for the full step-by-step guide.

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- **Node.js 18+** and **npm 9+**
- **Docker** and **Docker Compose**
- **Python 3.10+** (only if running AI service locally)

### One-Command Docker Start (Recommended)

```bash
# 1. Clone the repo
git clone <repo-url> && cd Project

# 2. Build shared library
cd Money-Manager-Backend/common-lib
mvn clean install -DskipTests

# 3. Build all Java services
for svc in api-gateway auth-service transaction-service planning-service \
           investment-service banking-service analytics-service \
           notification-service email-service; do
  cd ../Money-Manager-Backend/$svc && mvn clean package -DskipTests
done

# 4. Start everything with Docker Compose
cd ../Money-Manager-Backend
docker compose up -d --build

# 5. Start the frontend
cd ../Money-Manager-Frontend
npm install
npm run dev --workspace=packages/shell-app
```

The app will be available at **http://localhost:5173** (frontend) with the API gateway at **http://localhost:8080**.

## Service Ports

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Entry point for all API requests |
| Auth Service | 8081 | Registration, login, profiles |
| Transaction Service | 8082 | Income, expenses, categories, recurring, lending |
| Planning Service | 8083 | Budgets, savings goals, debts |
| Investment Service | 8084 | Investment portfolio |
| Banking Service | 8085 | Banking integration (scaffold) |
| Analytics Service | 8086 | Dashboard, reports, analytics |
| AI Service | 8087 | AI/ML insights (Python FastAPI) |
| Notification Service | 8088 | Notifications (scaffold) |
| Email Service | 8089 | Email delivery via RabbitMQ |
| MySQL | 3307 | Database (mapped from container 3306) |
| RabbitMQ | 5672 | Message broker |
| RabbitMQ UI | 15672 | Management console (guest/guest) |
| Frontend | 5173 | React development server |

## Environment Variables

Copy and configure the `.env` file in `Money-Manager-Backend/`:

```env
# Database
MYSQL_DB_NAME=money_manager
MYSQL_ROOT_PASSWORD=root
MYSQL_DB_USERNAME=appuser
MYSQL_DB_PASSWORD=apppass
MYSQL_HOST=mysql
MYSQL_PORT=3306

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# SMTP (for activation emails)
SMTP_USERNAME=your-smtp-username
SMTP_PASSWORD=your-smtp-password
SMTP_FROM=your-email@example.com

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=36000000
```

Frontend environment (`Money-Manager-Frontend/packages/shell-app/.env`):

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name
```

## API Routes (via Gateway)

All requests go through the API Gateway at `http://localhost:8080/api/v1/`:

| Method | Path | Service | Auth Required |
|--------|------|---------|:---:|
| POST | `/register` | Auth | No |
| POST | `/login` | Auth | No |
| GET | `/activate` | Auth | No |
| GET/PUT | `/profile` | Auth | Yes |
| PUT | `/profile/password` | Auth | Yes |
| GET/POST | `/incomes/**` | Transaction | Yes |
| GET/POST | `/expenses/**` | Transaction | Yes |
| GET/POST | `/categories/**` | Transaction | Yes |
| GET/POST | `/recurring/**` | Transaction | Yes |
| GET/POST | `/lend-borrow/**` | Transaction | Yes |
| GET/POST | `/budgets/**` | Planning | Yes |
| GET/POST | `/savings-goals/**` | Planning | Yes |
| GET/POST | `/debts/**` | Planning | Yes |
| GET/POST | `/investments/**` | Investment | Yes |
| GET | `/dashboard/**` | Analytics | Yes |
| GET | `/analytics/**` | Analytics | Yes |
| GET | `/reports/**` | Analytics | Yes |
| GET | `/ai/**` | AI Service | Yes |

## Documentation

Detailed documentation is available in the `/docs` directory:

- [Getting Started](docs/getting-started.md) — Setup and run instructions
- [Architecture Restructuring](docs/architecture-restructuring.md) — Microservice/MFE architecture details
- [Architecture Improvements](docs/architecture-improvements.md) — SOLID principles and clean architecture
- [Feature Enhancements](docs/feature-enhancements.md) — Core finance features
- [Dashboard Enhancements](docs/dashboard-enhancements.md) — Charts and analytics
- [UI Improvements](docs/ui-improvements.md) — Modern UI/UX
- [Profile Enhancements](docs/profile-enhancements.md) — Profile management
- [Homepage Updates](docs/homepage-updates.md) — Landing page redesign
- [Production Enhancements](docs/production-enhancements.md) — API fixes, versioning, async email, testing

## License

This project is for personal/educational use.
