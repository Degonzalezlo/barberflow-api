# BarberFlow API 💈

**BarberFlow** is a robust, enterprise-grade SaaS (Software as a Service) solution designed to streamline barbershop operations. This project bridges the gap between 14 years of industry expertise and modern software engineering, providing a scalable backend to manage bookings, users, and business logic.

---

## 🚀 Technical Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security & JWT (JSON Web Tokens)
* **Database:** PostgreSQL
* **Architecture:** Modular Monolith with Domain-Driven Design (DDD) principles
* **Containerization:** Docker & Docker Compose

---

## 🏗️ Architecture & Design

The project is structured into functional modules to ensure high maintainability and scalability, following **Clean Architecture** patterns:

* **Domain Layer:** Contains the core business entities and repository interfaces.
* **Infrastructure Layer:** Handles external concerns like database persistence and security configurations.
* **Application/API Layer:** Exposes RESTful endpoints for client consumption.

---

## 🔐 Security Features

* **Stateless Authentication:** Implemented using JWT for secure and scalable user sessions.
* **Role-Based Access Control (RBAC):** Distinct permissions for **ADMIN**, **BARBER**, and **CLIENT** roles.
* **Password Encryption:** Utilizing BCrypt for sensitive data protection.

---

## 🛠️ Getting Started

### Prerequisites
* JDK 21
* Docker & Docker Compose
* Maven

### Local Setup
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/Degonzalezlo/barberflow-api.git](https://github.com/Degonzalezlo/barberflow-api.git)
   cd barberflow-api
