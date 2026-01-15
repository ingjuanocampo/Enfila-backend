# EnFila Backend

A Kotlin backend using Ktor for the EnFila Android application. This backend provides REST APIs for managing users, clients, shifts, company sites, and messaging functionality.

## Features

- **User Management**: CRUD operations for users
- **Client Management**: CRUD operations for clients
- **Shift Management**: Create, update, and manage shifts with assignation flow
- **Company Site Management**: Manage company sites and locations
- **Messaging**: Twilio integration for WhatsApp messaging
- **Migration**: Tools to migrate from Firebase to PostgreSQL
- **Database**: PostgreSQL with Exposed ORM
- **Architecture**: Clean architecture with repository pattern

## Tech Stack

- **Kotlin**: Primary language
- **Ktor**: Web framework
- **PostgreSQL**: Database
- **Exposed**: ORM for database operations
- **Koin**: Dependency injection
- **Kotlinx Serialization**: JSON serialization
- **Twilio**: SMS/WhatsApp messaging
- **Docker**: Containerization

## API Endpoints

### Users
- `POST /api/v1/users` - Create user
- `GET /api/v1/users` - Get all users  
- `GET /api/v1/users/{id}` - Get user by ID
- `GET /api/v1/users/by-phone/{phone}` - Get user by phone
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user

### Clients
- `POST /api/v1/clients` - Create client
- `GET /api/v1/clients` - Get all clients
- `GET /api/v1/clients/{id}` - Get client by ID
- `PUT /api/v1/clients/{id}` - Update client
- `DELETE /api/v1/clients/{id}` - Delete client

### Shifts
- `POST /api/v1/shifts` - Create shift
- `POST /api/v1/shifts/assign` - Assign shift (simplified endpoint)
- `GET /api/v1/shifts` - Get all shifts (with optional filters)
- `GET /api/v1/shifts/{id}` - Get shift by ID
- `GET /api/v1/shifts/{id}/details` - Get shift with client details
- `PUT /api/v1/shifts/{id}` - Update shift
- `DELETE /api/v1/shifts/{id}` - Delete shift

### Company Sites
- `POST /api/v1/company-sites` - Create company site
- `GET /api/v1/company-sites` - Get all company sites
- `GET /api/v1/company-sites/{id}` - Get company site by ID
- `PUT /api/v1/company-sites/{id}` - Update company site
- `DELETE /api/v1/company-sites/{id}` - Delete company site

### Messaging
- `POST /api/v1/messages/send` - Send WhatsApp message via Twilio

### Migration
- `POST /api/v1/migration/from-firebase` - Migrate data from Firebase
- `GET /api/v1/migration/status` - Check migration status

## Setup

### Prerequisites

- Docker and Docker Compose
- Kotlin/JVM 17+
- PostgreSQL 15+

### Environment Variables

Create a `.env` file:

```bash
TWILIO_SID=your_twilio_sid
TWILIO_TOKEN=your_twilio_token
DATABASE_URL=jdbc:postgresql://localhost:5432/enfila_db
DATABASE_USER=enfila_user
DATABASE_PASSWORD=enfila_password
```

### Firebase Setup (Optional - for migration)

1. Download your Firebase service account JSON file
2. Place it as `firebase-service-account.json` in the root directory
3. Update `application.yaml` with your Firebase project ID

### Running with Docker

```bash
# Start the services
docker-compose up --build

# Run in background
docker-compose up -d --build

# View logs
docker-compose logs -f backend

# Stop services
docker-compose down
```

### Running Locally

```bash
# Start PostgreSQL
docker-compose up postgres -d

# Run the application
./gradlew run

# Or build and run
./gradlew build
java -jar build/libs/enfila-backend-1.0.0-all.jar
```

### Database Migration

The application will automatically create database tables on startup. To migrate data from Firebase:

```bash
curl -X POST http://localhost:8080/api/v1/migration/from-firebase
```

## Configuration

The application uses `application.yaml` for configuration. Key settings:

- **Database**: Connection details and pool settings
- **Firebase**: Credentials and project ID for migration
- **Twilio**: Base URL for API calls (credentials from environment)
- **CORS**: Allowed hosts and headers

## Data Models

### User
```json
{
  "id": "string",
  "phone": "string",
  "name": "string?",
  "companyIds": ["string"]?
}
```

### Client
```json
{
  "id": "string",
  "name": "string?",
  "shifts": ["string"]?
}
```

### Shift
```json
{
  "id": "string",
  "date": "long",
  "parentCompanySite": "string",
  "number": "int",
  "contactId": "string",
  "notes": "string?",
  "state": "WAITING|CALLING|CANCELLED|FINISHED",
  "attentionStartDate": "long?",
  "endDate": "long?"
}
```

## Development

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew test
```

### Code Style
This project follows Kotlin coding conventions.

## Deployment

### Production Considerations

1. **Security**: 
   - Use environment variables for sensitive data
   - Configure proper CORS settings
   - Add authentication/authorization as needed

2. **Database**:
   - Use managed PostgreSQL service
   - Configure connection pooling
   - Set up backups

3. **Monitoring**:
   - Add logging and metrics
   - Set up health checks
   - Configure alerting

4. **Scaling**:
   - Use container orchestration (Kubernetes)
   - Configure load balancing
   - Add caching if needed

## Architecture

The backend follows clean architecture principles:

```
├── config/           # Configuration (Database, DI, Security)
├── data/
│   ├── database/    # Database tables and schema
│   ├── models/      # Data transfer objects
│   └── repositories/# Data access layer
├── services/        # Business logic layer
└── routes/          # HTTP routes and controllers
```

## Migration from Firebase

This backend is designed to replace Firebase usage in the Android app. The migration process:

1. **Data Migration**: Use the `/migration/from-firebase` endpoint
2. **Android App Update**: Replace Firebase SDK with Ktor HTTP client
3. **Repository Pattern**: Update repositories to call REST APIs instead of Firebase
4. **Authentication**: Migrate from Firebase Auth to custom solution if needed

## Support

For questions or issues, please check the project documentation or create an issue in the repository.
