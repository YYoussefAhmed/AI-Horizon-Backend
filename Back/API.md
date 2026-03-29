# API Documentation

This document describes the REST API endpoints for the Blinders English Course Application.

## Base URL
```
http://localhost:8080/api
```

## Authentication

Most API endpoints require authentication via JWT. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```
Endpoints under `/api/auth/**` are public. Endpoints under `/api/admin/**` require the `ADMIN` role.

---

## 1. Authentication (AuthController)

### Register User
*   **POST** `/api/auth/register`
*   **Body:** `RegisterRequest` (name, email, password)
*   **Response:** `LoginResponse` (token, User info)
*   **Description:** Creates a new user and returns a JWT token.

### Login User
*   **POST** `/api/auth/login`
*   **Body:** `LoginRequest` (email, password)
*   **Response:** `LoginResponse` (token, User info)
*   **Description:** Authenticates a user and returns a JWT token.

---

## 2. Admin Management (AdminController)
*Requires `ADMIN` role.*

### Levels
*   **POST** `/api/admin/levels` - Create a level (name, order, description, passingScore)
*   **GET** `/api/admin/levels` - Get all levels

### Skills
*   **POST** `/api/admin/skills` - Create a skill under a level (name, levelId)
*   **GET** `/api/admin/skills` - Get all skills

### Lessons
*   **POST** `/api/admin/lessons` - Create a lesson under a skill (title, content, order, skillId)
*   **PUT** `/api/admin/lessons/{id}` - Update a lesson
*   **DELETE** `/api/admin/lessons/{id}` - Delete a lesson
*   **GET** `/api/admin/lessons` - Get all lessons

### Questions
*   **POST** `/api/admin/questions` - Create a question (text, options, correctAnswerIndex, questionType, skillId/levelId)
*   **PUT** `/api/admin/questions/{id}` - Update a question
*   **DELETE** `/api/admin/questions/{id}` - Delete a question
*   **GET** `/api/admin/questions` - Get all questions

### Users
*   **GET** `/api/admin/users` - Get all registered users
*   **PUT** `/api/admin/users/{id}/role` - Update a user's role (ADMIN/USER)

---

## 3. Levels & Skills (LevelController)

### Get All Levels
*   **GET** `/api/levels`
*   **Response:** List of `Level` objects ordered by order index.

### Get Level by ID
*   **GET** `/api/levels/{id}`
*   **Response:** A single `Level` object.

### Get Skills by Level
*   **GET** `/api/levels/{id}/skills`
*   **Response:** List of `Skill` objects that belong to the specified Level.

---

## 4. Lessons (LessonController)

### Get Lessons
*   **GET** `/api/lessons`
*   **Query Params:** `?skillId={id}` (optional)
*   **Response:** List of `Lesson` objects. If `skillId` is provided, returns lessons for that specific skill.

### Get Lesson by ID
*   **GET** `/api/lessons/{id}`
*   **Response:** A single `Lesson` object.

### Complete Lesson
*   **POST** `/api/lessons/{id}/complete`
*   **Response:** `UserProgress` object. Marks a lesson as completed for the authenticated user and logs the completion time.

---

## 5. Quiz & Exams (QuizController)

### Get Placement Test
*   **GET** `/api/quiz/placement-test`
*   **Response:** List of `QuestionDto` for the initial placement test.

### Submit Placement Test
*   **POST** `/api/quiz/placement-test/submit`
*   **Body:** `QuizSubmission` (Map of questionId -> selectedIndex)
*   **Response:** Calculates the score and automatically assigns a `LevelType` to the user based on their score. Marks `placementTestCompleted` as true.

### Get Level Exam
*   **GET** `/api/quiz/level-exam/{levelId}`
*   **Response:** List of `QuestionDto` representing the final exam for a specific level.

### Submit Level Exam
*   **POST** `/api/quiz/level-exam/{levelId}/submit`
*   **Body:** `QuizSubmission`
*   **Response:** Calculates score. If passed, automatically upgrades the user to the next LevelType.

---

## 6. User Progress (ProgressController)

### Get Overall Progress
*   **GET** `/api/progress`
*   **Response:** Map containing `currentLevel`, `placementTestCompleted`, `completedLessonsCount`, and history of `examAttempts`.

### Get Level Progress
*   **GET** `/api/progress/level/{levelId}`
*   **Response:** Information regarding the user's progress and exam attempts within a specific level.

---

## 7. Feedback (FeedbackController)

### Submit Level Feedback
*   **POST** `/api/feedback/level/{levelId}`
*   **Body:** Map containing `rating` (Integer).
*   **Response:** Saves a rating for a specific level.

### Get Level Feedback
*   **GET** `/api/feedback/level/{levelId}`
*   **Response:** List of all feedback records for a specific level.
