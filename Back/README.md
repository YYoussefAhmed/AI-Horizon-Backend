# Blinders - English Course Application

A Spring Boot application designed to help visually impaired users learn English through a structured curriculum of levels, skills, and lessons.

## Features

- **Structured Learning**: Curriculum divided into levels (Easy, Medium, Advanced, Native).
- **Skill-Based Lessons**: Focused content on Vocabulary, Grammar, etc.
- **Adaptive Assessments**: Automatic level placement and progression based on exam results.
- **User Progress Tracking**: Real-time tracking of completed lessons and exam history.

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data JPA**
- **MySQL Database**
- **JWT Authentication**
- **Maven**

## Getting Started

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE blinders_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Initialize schema (Optional, Hibernate will auto-create)
mysql -u root -p blinders_db < database_setup.sql
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database credentials.

### 3. Run Application

```bash
mvnw.cmd spring-boot:run
```

## API Documentation

Interactive API documentation is available via Swagger UI when the server is running:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## License

MIT License
