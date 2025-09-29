# Health Check Batch Application with Web Console

A Spring Boot batch application with web-based management console that performs health checks on multiple servers via REST API calls, stores results in H2 database, and sends Telegram notifications when response times exceed specified thresholds.

## Features

- **Web Management Console** - Browser-based interface for managing target servers and viewing logs
- **H2 Database Storage** - Target server configurations and execution logs stored in embedded database
- **Environment-specific configuration** using Spring profiles (dev, test, prod)
- **REST API health checks** supporting both GET and POST methods
- **JSON response handling** with proper parsing and logging
- **Configurable timeout thresholds** for response time monitoring
- **Telegram notifications** for slow responses and failures
- **Comprehensive logging** with separate log files for errors and batch results
- **Manual trigger endpoints** for testing and monitoring
- **Scheduled batch execution** using cron expressions
- **Real-time statistics** and monitoring dashboard

## Technology Stack

- Java 1.8
- Spring Boot 2.7.18
- Spring Data JPA
- H2 Database (embedded)
- Thymeleaf 3.1.2
- Gradle 6.9.1
- WebFlux (reactive HTTP client)
- Jackson (JSON processing)
- Telegram Bot API
- Bootstrap 5 (UI framework)
- Logback (logging)

## Project Structure

```
src/main/java/com/company/batch/
├── HealthCheckBatchApplication.java     # Main application class
├── config/
│   ├── BatchConfig.java                 # Bean configurations
│   └── BatchProperties.java             # Configuration properties binding
├── controller/
│   ├── BatchController.java             # REST endpoints for manual operations
│   └── WebConsoleController.java        # Web console controllers
├── dto/
│   └── HealthCheckResult.java           # Data transfer object for results
├── entity/
│   ├── TargetServer.java                # JPA entity for target servers
│   └── ExecutionLog.java                # JPA entity for execution logs
├── job/
│   └── HealthCheckBatch.java            # Main batch job with scheduling
├── repository/
│   ├── TargetServerRepository.java      # JPA repository for servers
│   └── ExecutionLogRepository.java      # JPA repository for logs
└── service/
    ├── HealthCheckService.java          # Health check logic with DB integration
    ├── TelegramService.java             # Telegram notification service
    └── TargetServerService.java         # Target server management service

src/main/resources/
├── templates/console/                   # Thymeleaf templates for web console
│   ├── layout.html                      # Base layout template
│   ├── dashboard.html                   # Dashboard page
│   ├── servers.html                     # Server management page
│   ├── server-form.html                 # Server add/edit form
│   ├── logs.html                        # Execution logs page
│   ├── log-detail.html                  # Log detail page
│   └── server-logs.html                 # Server-specific logs page
├── application.yml                      # Main configuration
├── data.sql                             # Initial database data
└── logback-spring.xml                   # Logging configuration
```

## Web Console Features

### Dashboard (`/console/`)
- Real-time statistics overview
- Quick action buttons
- Recent execution logs summary
- Auto-refresh functionality

### Server Management (`/console/servers`)
- Add, edit, delete target servers
- Enable/disable servers
- Search and filter servers
- Test server connections
- Environment-specific server management

### Execution Logs (`/console/logs`)
- View all execution logs with pagination
- Filter by success/failure status
- Filter by server name
- Detailed log information with response bodies
- Performance metrics and statistics

### Database Console (`/h2-console`)
- Direct access to H2 database
- Browse tables and execute SQL queries
- Database: `jdbc:h2:mem:healthcheck`
- Username: `sa`, Password: (empty)

## Configuration

### Application Properties

The application uses environment-specific YAML configuration:

```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:healthcheck;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

batch:
  health-check:
    schedule:
      cron: "0 */5 * * * *"  # Every 5 minutes
    timeout:
      threshold: 10000  # 10 seconds
    telegram:
      bot-token: "YOUR_BOT_TOKEN"
      chat-id: "YOUR_CHAT_ID"
      enabled: true
```

### Database Schema

The application automatically creates these tables:

- **target_servers** - Server configuration and metadata
- **execution_logs** - Health check execution results and performance data

## Setup Instructions

### 1. Prerequisites

- Java 1.8 or higher
- Gradle 6.9.1
- JBoss Application Server (if deploying as WAR)

### 2. Clone and Build

```bash
# Clone the project
git clone <repository-url>
cd health-check-batch

# Build the project
./gradlew build
```

### 3. Configure Telegram Bot (Optional)

1. Create a Telegram bot via [@BotFather](https://t.me/botfather)
2. Get the bot token
3. Get your chat ID (you can message [@userinfobot](https://t.me/userinfobot))
4. Update the configuration in `application.yml`

### 4. Running the Application

#### Development Mode

```bash
# Run with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Or with environment variable
export SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```

#### Production Mode

```bash
# Build JAR
./gradlew build

# Run JAR
java -jar build/libs/health-check-batch-1.0.0.jar --spring.profiles.active=prod
```

#### JBoss Deployment

```bash
# Build WAR (modify build.gradle to include war plugin)
./gradlew war

# Deploy to JBoss
cp build/libs/health-check-batch-1.0.0.war $JBOSS_HOME/standalone/deployments/
```

### 5. Access the Application

Once running, access these URLs:

- **Web Console**: http://localhost:8080/console/
- **H2 Database Console**: http://localhost:8080/h2-console
- **REST API Health**: http://localhost:8080/api/batch/health
- **Application Health**: http://localhost:8080/actuator/health (if actuator enabled)

## Web Console Usage

### Managing Target Servers

1. Navigate to `/console/servers`
2. Click "Add New Server" to create a server configuration
3. Fill in the form:
  - **Name**: Descriptive name for the server
  - **URL**: Full HTTP/HTTPS endpoint URL
  - **Method**: HTTP method (GET, POST, PUT, HEAD)
  - **Timeout**: Request timeout in milliseconds
  - **Environment**: dev, test, or prod
  - **Request Body**: JSON body for POST/PUT requests
  - **Description**: Optional description

4. Enable/disable servers using the toggle button
5. Test connections directly from the server form

### Viewing Execution Logs

1. Navigate to `/console/logs`
2. Use filters to find specific logs:
  - Filter by success/failure status
  - Search by server name
  - Use pagination for large datasets

3. Click on a log entry to view detailed information:
  - Full request/response details
  - Error messages for failed requests
  - Performance metrics
  - Related logs for the same server

### Dashboard Monitoring

The dashboard provides:
- **Server Statistics**: Total and active server counts
- **24-hour Performance**: Success rates and average response times
- **Quick Actions**: Manual triggers and shortcuts
- **Recent Activity**: Latest execution results

## API Endpoints

### Health Check Management
```bash
# Application health
GET /api/batch/health

# Manual health check trigger
POST /api/batch/trigger

# Immediate health check (returns results)
GET /api/batch/check-now

# View configuration
GET /api/batch/config

# Get statistics
GET /api/batch/stats
```

### Telegram Integration
```bash
# Test Telegram notifications
POST /api/batch/telegram/test
```

### Web Console
```bash
# Dashboard
GET /console/

# Server management
GET /console/servers
GET /console/servers/new
GET /console/servers/{id}/edit
POST /console/servers
POST /console/servers/{id}/update
POST /console/servers/{id}/delete
POST /console/servers/{id}/toggle

# Log viewing
GET /console/logs
GET /console/logs/{id}
GET /console/servers/{id}/logs
```

## Database Management

### Accessing the Database

1. Navigate to `/h2-console`
2. Use these connection settings:
  - **JDBC URL**: `jdbc:h2:mem:healthcheck`
  - **User Name**: `sa`
  - **Password**: (leave empty)

### Useful SQL Queries

```sql
-- View all active servers
SELECT * FROM target_servers WHERE enabled = true;

-- View recent execution logs
SELECT * FROM execution_logs 
ORDER BY execution_time DESC 
LIMIT 50;

-- Get failure statistics
SELECT server_name, COUNT(*) as failure_count
FROM execution_logs 
WHERE success = false 
GROUP BY server_name;

-- Get average response times by server
SELECT server_name, AVG(elapsed_time_ms) as avg_response_time
FROM execution_logs 
WHERE success = true 
GROUP BY server_name;

-- Clean up old logs (older than 30 days)
DELETE FROM execution_logs 
WHERE execution_time < DATEADD('DAY', -30, CURRENT_TIMESTAMP);
```

## Monitoring and Logging

### Log Files

- `logs/health-check-batch.log` - Main application log
- `logs/health-check-batch-error.log` - Error-only log
- `logs/batch-results.log` - Batch execution results

### Log Rotation

- Maximum file size: 10MB (50MB for results)
- Retention: 30 days (60 days for results)
- Total size cap: 300MB (1GB for results)

### Performance Monitoring

The web console provides real-time monitoring of:
- Response times and success rates
- Server availability statistics
- Execution history and trends
- Error patterns and analysis

## Customization

### Adding New Server Types

1. Use the web console to add servers dynamically
2. Configure different HTTP methods and request bodies
3. Set environment-specific timeouts and thresholds
4. Enable/disable servers as needed

### Custom Notification Logic

Modify `TelegramService.java` to customize:
- Message formatting and content
- Notification triggers and thresholds
- Integration with other messaging platforms

### Scheduling Changes

Update the cron expression in your configuration:

```yaml
batch:
  health-check:
    schedule:
      cron: "0 */2 * * * *"  # Every 2 minutes
      # cron: "0 0 */1 * * *"  # Every hour
      # cron: "0 30 8 * * *"   # Daily at 8:30 AM
```

### Database Customization

To use a different database (MySQL, PostgreSQL, etc.):

1. Add the database driver dependency to `build.gradle`
2. Update the datasource configuration in `application.yml`
3. Modify the JPA dialect if necessary

## Troubleshooting

### Common Issues

1. **Web console not loading**
  - Check if the application started successfully
  - Verify the port (default: 8080) is not in use
  - Check logs for Thymeleaf template errors

2. **Database connection errors**
  - Ensure H2 console is enabled in configuration
  - Verify the JDBC URL format
  - Check if the database is being created properly

3. **Health checks not running**
  - Verify at least one server is enabled
  - Check the cron expression syntax
  - Review application logs for scheduling errors

4. **Telegram notifications not working**
  - Verify bot token and chat ID configuration
  - Check if the bot was added to the chat
  - Review Telegram service logs

### Debug Mode

Enable debug logging for detailed troubleshooting:

```yaml
logging:
  level:
    com.kica.ess.batch: DEBUG
    org.springframework.web: DEBUG
    org.springframework.data.jpa: DEBUG
```

### Data Recovery

If you need to backup or restore data:

```bash
# Export data from H2 console
SCRIPT TO 'backup.sql';

# Import data (run SQL script through H2 console or application startup)
RUNSCRIPT FROM 'backup.sql';
```

## Contributing

1. Follow the existing code structure and patterns
2. Add appropriate logging for new features
3. Update web console templates for UI changes
4. Test with different environments and configurations
5. Ensure backward compatibility with existing data

## License

This project is licensed under the MIT License.