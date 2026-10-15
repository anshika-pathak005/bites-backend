
# Bites - Restaurant Ordering & Management Backend

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1.svg)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36.svg)](https://maven.apache.org/)

**Live Demo:** [Bites](https://bites-culinary.vercel.app)

**Repositories:**
- **Frontend:** [bites-frontend](https://github.com/anshika-pathak005/bites-frontend)
- **Backend:** [bites-backend](https://github.com/anshika-pathak005/bites-backend)

## 1. Introduction

Bites is a restaurant ordering and management application built with a Spring Boot backend and a Next.js frontend. The backend exposes REST APIs for customer operations, menu browsing, cart management, checkout, order tracking, delivery pricing, and restaurant administration.

It also supports guest shopping, JWT-based authentication, online payments through Razorpay, Cash on Delivery (COD), and cloud-based image storage.

The backend follows a layered architecture that separates API handling, business logic, data access, and persistence.

## 2. Features

### Customer & Guest Management
- Customer registration, login, and profile management.
- JWT-based authentication with refresh token support.
- Guest shopping without requiring immediate registration.
- Guest cart and order handling.
- Address management with geographic coordinates.

### Menu & Cart
- Browse menu items and categories.
- View individual menu item details.
- Filter menu items.
- Add, update, and remove cart items.
- Personalized menu recommendations based on available application logic.

### Checkout & Order Management
- Checkout and order creation.
- Order status tracking.
- Order cancellation support.
- Server-side delivery charge calculation.
- Delivery coverage validation using geographic coordinates and distance calculations.

### Payments
- Online payment integration using Razorpay.
- Payment order creation and signature verification.
- Payment webhook handling.
- Cash on Delivery (COD) support.
- Payment status management.

### Admin Management
- Separate admin authentication and profile management.
- Dashboard summaries.
- Category and menu item management.
- Customer management.
- Order management and status updates.
- Delivery area and delivery charge slab configuration.
- Restaurant settings management.
- Image uploads through Cloudinary.

### Security & Reliability
- Spring Security configuration.
- JWT authentication and authorization.
- Refresh token and guest identity handling.
- Login and order-related rate limiting.
- Request/response logging.
- Centralized exception handling and structured API responses.
- Database versioning through Flyway migrations.

## 3. Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot 3.3.4 | Application framework |
| Spring Web | REST API development |
| Spring Data JPA | Database access and ORM |
| Spring Security | Authentication and authorization |
| MySQL | Relational database |
| Flyway | Database schema migrations |
| JWT (JJWT) | Access token and authentication handling |
| Razorpay Java SDK | Online payment integration |
| Cloudinary | Image storage and management |
| MapStruct | Object mapping |
| Lombok | Boilerplate code reduction |
| Bucket4j | Rate limiting |
| Springdoc OpenAPI | API documentation |
| Maven | Dependency management and build |
| JUnit & Spring Security Test | Automated testing |

## 4. Architecture

The application follows a layered architecture:

```text
Client (Next.js Frontend)
          |
          v
     REST Controllers
          |
          v
       Services
   (Business Logic)
          |
          v
      Repositories
          |
          v
        MySQL
```

Supporting components include:

- **Security Layer:** Handles JWT authentication, authorization, refresh tokens, and guest identity.
- **DTO Layer:** Defines request and response objects for API communication.
- **Mapper Layer:** Converts entities into API response objects and maps related data.
- **Exception Layer:** Handles application exceptions and provides consistent error responses.
- **Configuration Layer:** Configures security, CORS, Cloudinary, Razorpay, and API documentation.
- **Migration Layer:** Maintains database schema changes through versioned SQL scripts.

### Request Flow

Client request -> Controller -> Service -> Repository -> Database

The service layer handles business rules and coordinates operations. Controllers expose endpoints, while repositories manage database interactions through Spring Data JPA.

External integrations, such as Razorpay and Cloudinary, are handled through their respective service and configuration components.

## 5. Project Structure

```text
src/
├── main/
│   ├── java/com/bites/
│   │   ├── config/          # Application and integration configuration
│   │   ├── controller/      # Customer, admin, and payment APIs
│   │   ├── dto/             # API request and response objects
│   │   ├── entity/          # JPA entities and enums
│   │   ├── exception/       # Custom exceptions and global handler
│   │   ├── logging/         # Request and response logging
│   │   ├── mapper/          # Entity and DTO mapping
│   │   ├── repository/      # Data access and query specifications
│   │   ├── security/        # JWT, guest identity, and rate limiting
│   │   ├── service/         # Business logic and service implementations
│   │   └── util/            # Shared utilities and API response classes
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       ├── logback-spring.xml
│       └── db/migration/    # Versioned Flyway SQL migrations
└── test/
    └── java/com/bites/      # Automated tests
```

## 6. Prerequisites

Make sure the following are installed:

- Java Development Kit (JDK) 21
- Maven
- MySQL Server or a compatible MySQL database
- Git

You will also need credentials for the external services you intend to use, such as Razorpay and Cloudinary.

## 7. Installation & Setup

### Clone the Repository

```bash
git clone https://github.com/anshika-pathak005/bites-backend.git
cd bites-backend
```

### Configure Environment Variables

Create a `.env` file in the project root and configure the required values using `.env.example` as a reference.

```bash
cp .env.example .env
```

On Windows PowerShell, use:

```powershell
Copy-Item .env.example .env
```

Update the database connection, authentication secrets, and external service credentials before running the application.

**Important:** Never commit `.env` or expose database passwords, JWT secrets, or payment gateway credentials in the repository.

### Configure the Database

1. Create a MySQL database for the application.
2. Configure the database connection details in your environment.
3. Ensure the database user has the necessary permissions to apply migrations.
4. Start the application. Flyway applies the versioned migrations during startup, according to the application configuration.

Avoid manually applying the same migrations when Flyway manages the schema.

## 8. Environment Variables

The exact variable names and requirements are defined in `.env.example` and the application configuration files.

Common configuration categories include:

| Category | Purpose |
|---|---|
| Database | MySQL URL, username, and password |
| JWT & Authentication | Token signing secrets and token configuration |
| Refresh Cookies | Refresh token cookie settings |
| Cloudinary | Cloud image storage credentials |
| Razorpay | API key ID, API key secret, and webhook secret |
| Application | Active Spring profile and server settings |

Refer to `.env.example` for the exact variable names and expected values. Configure production secrets through your deployment platform's environment settings.

## 9. Running the Application

### Run in Development Mode

```bash
mvn spring-boot:run
```

This starts the Spring Boot application using the active configuration.

### Build the Application

```bash
mvn clean package
```

This compiles the application, runs the configured test lifecycle, and packages the application as a JAR if the build succeeds.

### Run the Packaged JAR

After a successful build:

```bash
java -jar target/bites-backend-0.1.0.jar
```

Ensure the required environment variables and database configuration are available to the application.

### Run Tests

```bash
mvn test
```

This runs the automated tests configured in the Maven project.

## 10. API Documentation

The project includes Springdoc OpenAPI for API documentation.

When the application is running, try:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

These are the default local URLs; the actual host and port depend on the active application configuration.

## 11. Deployment

The repository includes a `Dockerfile` for containerized deployment.

The backend can be deployed to a compatible Docker hosting platform with the following configured:

- Database connectivity and credentials.
- Active Spring profile.
- JWT and refresh token settings.
- Razorpay credentials and webhook configuration.
- Cloudinary credentials.
- Correct CORS origins for the deployed frontend.

For production, use managed environment variables for secrets and a persistent MySQL database. Configure Razorpay webhooks with the publicly accessible backend endpoint intended to receive payment events.

## 12. Author

**Anshika Pathak**

- GitHub: [anshika-pathak005](https://github.com/anshika-pathak005)
- Bites Frontend: [Repository](https://github.com/anshika-pathak005/bites-frontend)
- Bites Backend: [Repository](https://github.com/anshika-pathak005/bites-backend)
