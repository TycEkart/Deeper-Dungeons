# Deeper Dungeons

## How to Run

### Backend
Run the Spring Boot application via your IDE or terminal:

```bash
./gradlew :backend:bootRun
```

### Frontend

#### Development Server
Start the development server with continuous reloading. This will start a server (usually at `localhost:8080`) and automatically reload when you make changes to the Kotlin code in `frontend/src/jsMain/kotlin`.

```bash
./gradlew :frontend:jsBrowserDevelopmentRun --continuous
```

#### Manual Build / Distribution
To build the production-ready JavaScript distribution (webpack bundle):

```bash
./gradlew :frontend:jsBrowserDistribution
```

### Desktop Executable
Create a native executable via:

```bash
gradle launcher:createExe
```