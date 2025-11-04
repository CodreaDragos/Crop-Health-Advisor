# Crop Health Advisor

Crop health monitoring system using satellite data and AI analysis.

## Table of Contents

- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)


## Architecture

```
┌─────────────────┐
│  Frontend (Vue) │
│  Port: 5173     │
└────────┬────────┘
         │ HTTP/REST
         ▼
┌─────────────────┐
│  Backend API    │
│  Spring Boot    │
│  Port: 8081     │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌──────────────┐
│  MySQL │ │ Sentinel Hub │
│  DB    │ │  API         │
└────────┘ └──────────────┘

┌─────────────────┐
│ Desktop Client  │
│  (WPF/.NET)     │
└────────┬────────┘
         │ HTTP/REST
         ▼
┌─────────────────┐
│  Backend API    │
│  (Same as above)│
└─────────────────┘
```

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: MySQL 8.0+
- **ORM**: Hibernate/JPA
- **Security**: Spring Security + JWT
- **Reactive**: Spring WebFlux (for satellite API calls)
- **AI**: Google Gemini AI (mock implementation available)

### Frontend
- **Framework**: Vue.js 3
- **Build Tool**: Vite
- **Maps**: Leaflet.js
- **HTTP Client**: Axios
- **Styling**: CSS3 with modern design

### Desktop Client
- **Framework**: WPF (.NET 8.0)
- **Language**: C#
- **HTTP Client**: HttpClient
- **JSON**: Newtonsoft.Json

## Project Structure

```
SCD-proiect/
├── crop-health-advisor/          # Backend (Spring Boot)
│   ├── src/main/java/
│   │   └── com/proiect/SCD/CropHealthAdvisor/
│   │       ├── controllers/      # REST Controllers
│   │       ├── services/         # Business Logic
│   │       ├── models/           # Entity Models
│   │       ├── repositories/     # JPA Repositories
│   │       ├── jwt/              # JWT Security
│   │       └── dto/              # Data Transfer Objects
│   └── src/main/resources/
│       └── application.properties
│
├── crop-health-frontend/         # Frontend (Vue.js)
│   ├── src/
│   │   ├── components/          # Vue Components
│   │   ├── services/             # API Services
│   │   └── main.js
│   └── package.json
│
├── CropHealth-Desktop/           # Desktop Client (WPF)
│   └── CropHealth-Desktop/
│       ├── Interface/            # WPF Windows
│       ├── Models/               # Data Models
│       └── Services/             # API Service
│
└── README.md
```

## Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- MySQL 8.0+
- .NET 8.0 SDK (for desktop client)
- Visual Studio 2022 or VS Code (for desktop client)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd "SCD proiect"
   ```

2. **Configure MySQL database**
   - Create a MySQL database:
     ```sql
     CREATE DATABASE crop_health_db;
     ```

3. **Configure `application.properties`**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/crop_health_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   # JWT Configuration
   jwt.secret=your-secret-key-here
   jwt.expiration=86400000
   
   # Sentinel Hub API (optional)
   sentinelhub.api.key=your-api-key
   
   # OpenWeatherMap API (optional)
   openweathermap.api.key=your-api-key
   
   # Gemini AI (optional)
   gemini.api.key=your-api-key
   ```

4. **Build and run**
   ```bash
   cd crop-health-advisor
   mvn clean install
   mvn spring-boot:run
   ```

   Backend will start on `http://localhost:8081`

### Frontend Setup

1. **Install dependencies**
   ```bash
   cd crop-health-frontend
   npm install
   ```

2. **Configure API URL** (if different from default)
   - Edit `src/services/authService.js` and `locationService.js`
   - Default: `http://localhost:8081/api`

3. **Run development server**
   ```bash
   npm run dev
   ```

   Frontend will start on `http://localhost:5173`

### Desktop Client Setup

1. **Open solution**
   - Open `CropHealth-Desktop/CropHealth-Desktop.sln` in Visual Studio

2. **Restore NuGet packages**
   - Right-click solution → Restore NuGet Packages

3. **Configure API URL** (if different)
   - Edit `Services/BackendApiService.cs`
   - Default: `http://localhost:8081/api/`

4. **Build and run**
   - Press F5 or click Run

## Configuration

### API Keys Setup

#### Sentinel Hub API
1. Sign up at [Sentinel Hub](https://www.sentinel-hub.com/)
2. Get your API key
3. Add to `application.properties`:
   ```properties
   sentinelhub.api.key=your-api-key
   ```

#### OpenWeatherMap API (Optional)
1. Sign up at [OpenWeatherMap](https://openweathermap.org/api)
2. Get your free API key
3. Add to `application.properties`:
   ```properties
   openweathermap.api.key=your-api-key
   ```

#### Gemini AI (Optional)
1. Get API key from Google Cloud
2. Add to `application.properties`:
   ```properties
   gemini.api.key=your-api-key
   ```

**Note**: The application works with mock data if API keys are not provided.

## Running the Application

### Start Backend
```bash
cd crop-health-advisor
mvn spring-boot:run
```

### Start Frontend
```bash
cd crop-health-frontend
npm run dev
```

### Start Desktop Client
- Open Visual Studio
- Press F5 or Run

### Default Credentials
- **Admin**: `admin@gmail.com` / `admin1234`
- **User**: Register via frontend or desktop client

## API Documentation

### Authentication
- `POST /api/auth/login` - Login with email and password
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```

### Users
- `GET /api/users` - Get all users (Admin only)
- `POST /api/users/register` - Register new user (Public)
- `POST /api/users` - Create user (Admin only)
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/by-email?email={email}` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Locations
- `GET /api/locations` - Get all locations
- `POST /api/locations` - Create location
- `GET /api/locations/user/{userId}` - Get locations by user
- `GET /api/locations/{id}` - Get location by ID
- `PUT /api/locations` - Update location
- `DELETE /api/locations/{id}` - Delete location

### Reports
- `GET /api/reports?locationId={id}` - Generate new report
- `GET /api/reports/all` - Get all reports
- `GET /api/reports/{id}` - Get report by ID
- `GET /api/reports/location/{locationId}` - Get reports by location
- `POST /api/reports` - Create report
- `PUT /api/reports` - Update report
- `DELETE /api/reports/{id}` - Delete report

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER'
);
```

### Locations Table
```sql
CREATE TABLE locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Reports Table
```sql
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ndvi_value DOUBLE,
    temperature_value DOUBLE,
    precipitation_value DOUBLE,
    ai_interpretation TEXT,
    report_date DATETIME,
    location_id BIGINT,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
);
```


