# Architecture Documentation

This document describes the updated architecture of the Blinders English Course Application.

## System Overview

Blinders is a Spring Boot application designed to help visually impaired users learn English. The core logic revolves around structured levels, skill-based lessons, and adaptive testing.

## Application Layers

The project follows a standard layered architecture:

1.  **Controller Layer**: Handles HTTP requests and response mapping. (e.g., `AdminController`, `AuthController`).
2.  **Service Layer**: (Partial) Business logic for authentication and security.
3.  **Data Access Layer (JPA Repositories)**: Interfaces for MySQL database interaction using Spring Data JPA.
4.  **Entity Layer**: JPA entities representing the database schema.

## Core Entities and Data Model

*   **User**: Core entity storing user identity, credentials, role (ADMIN/USER), `currentLevel`, and `placementTestCompleted` status.
*   **Level**: Defines difficulty levels (e.g., EASY, MEDIUM, ADVANCED). Contains a description and a `passingScore`.
*   **Skill**: Categorizes content within a level (e.g., Listening, Writing, Vocabulary).
*   **Lesson**: The primary educational unit containing content and ordering.
*   **UserProgress**: Tracks if a user has completed a specific lesson.
*   **Question**: Stores assessment questions. Questions can be tagged as `PLACEMENT` or `LEVEL_EXAM`.
*   **ExamAttempt**: Logs the results of every exam taken by a user, including score and pass/fail status.
*   **LevelFeedback**: Stores user-submitted ratings for specific levels.

## Key Workflows

### 1. New User Onboarding
Users register via `/api/auth/register`, which triggers the generation of a JWT token. They then take a **Placement Test** via `/api/quiz/placement-test`. Based on the score, the system automatically sets their `currentLevel`.

### 2. Learning and Progress
Users browse lessons within their assigned level. When a lesson is finished, a post to `/api/lessons/{id}/complete` records their progress.

### 3. Level Advancement
After completing lessons, users take a **Level Exam**. Passing the exam (meeting the Level's `passingScore`) automatically promotes the user to the next global `LevelType`.

## Security

*   **JWT Authentication**: All non-auth endpoints require a valid JWT token in the header.
*   **Role-Based Access**: The `/api/admin/**` path is restricted to users with the `ADMIN` role using `@PreAuthorize`.
*   **Password Hashing**: BCrypt is used for storing user passwords securely.
