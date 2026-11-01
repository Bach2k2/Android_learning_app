# Japanese Learning App (MVP)

Production-style MVP Japanese learning app with an Android (Kotlin) client and a Spring Boot (Java) REST API backed by PostgreSQL, plus optional local AI assistance (Ollama) for quiz generation and explanations.

## Tech Stack

- Android: Kotlin + MVVM + Repository pattern + Retrofit + Jetpack Compose (Material 3)
- Backend: Java Spring Boot (REST), Spring Security (JWT), Flyway, JPA
- Database: PostgreSQL
- AI (local): Ollama HTTP API (optional in dev)
- DevOps: Docker Compose (local), Railway (backend + Postgres)

## Features (MVP)

- Auth
  - Register / Login
  - JWT access token
  - Refresh token rotation (DB-backed)
- Learning
  - 5 seeded beginner lessons
  - Vocabulary, grammar points, example sentences
  - Basic quiz + answer checking
- AI (optional)
  - Generate quiz questions from lesson data
  - Simple grammar explanation
  - Vocabulary review helper

Note: the seeded lesson content is original, beginner-oriented content (inspired by common curricula). This repo does not include copyrighted textbook text.

## Quick Start (Local)

1) Start API + Postgres:

- `docker compose up --build`

2) (Optional) Start with local AI container:

- `docker compose --profile ai up --build`

Then pull a model inside the Ollama container (first time only):

- `docker exec -it <ollama_container_name> ollama pull llama3.1`

3) Run Android app

- Open `mobile-android/` in Android Studio and run the `app` configuration.
- Default API base URL for emulator is `http://10.0.2.2:8080`.

## Android (Kotlin) Environment

### Architecture

- MVVM + Repository pattern
  - UI: Jetpack Compose (Material 3) screens
  - Navigation: `androidx.navigation:navigation-compose`
  - Presentation: `ViewModel` (`lifecycle-viewmodel-compose`) exposes UI state
  - Async/state: Kotlin Coroutines
  - Data: Retrofit + OkHttp (+ logging interceptor) for REST calls, Gson converter for JSON
  - Local storage: Jetpack DataStore (preferences) for lightweight persistence (for example tokens/settings)

### App functions (MVP)

- Auth: register, login, store JWT/refresh token data client-side
- Lessons: list lessons, view lesson details
- Quiz: answer checking against lesson quiz questions
- Progress: fetch/update learning progress

### Prerequisites

- Android Studio (recent stable)
- JDK 21 
- Android SDK
  - `compileSdk` / `targetSdk`: 35
  - `minSdk`: 26
- Emulator or device
  - Emulator talks to the host machine via `http://10.0.2.2:8080`
  - Physical device should use your host LAN IP (for example `http://192.168.1.10:8080`)

### API base URL configuration

- The Android app reads `API_BASE_URL` from Gradle properties and exposes it as `BuildConfig.API_BASE_URL`.
- Default is `http://10.0.2.2:8080` (Android emulator).
- Override options:
  - Edit `mobile-android/gradle.properties`
  - Or pass a project property: `./gradlew :app:assembleDebug -PAPI_BASE_URL=http://...`
  - Or set it globally in `~/.gradle/gradle.properties` (recommended for local-only overrides)

## API Overview

Swagger UI (after starting the backend):

- `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

- Auth: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`
- Lessons: `GET /api/lessons`, `GET /api/lessons/{id}`
- Quiz: `POST /api/lessons/{lessonId}/quiz/{questionId}/check`
- Progress: `GET /api/progress`, `PUT /api/progress/{lessonId}`
- AI: `POST /api/ai/lessons/{lessonId}/generate-quiz`, `POST /api/ai/grammar/explain`, `POST /api/ai/vocab/review`

## Deployment (Railway)

- Deploy the backend from `backend/Dockerfile`.
- Provision a Railway Postgres instance.
- Configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `JWT_SECRET`.

More details are in `SETUP.md`.
