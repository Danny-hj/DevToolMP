# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DevToolMP is a tool marketplace platform inspired by skills.sh, featuring tool discovery, popularity rankings, reviews, and GitHub integration. It's built with Spring Boot 3.x (backend) and Vue 3 (frontend).

**Critical**: The project uses Docker for development and deployment. Always use `./scripts/start-dev.sh` or `./scripts/start-local.sh` to start the development environment.

## Development Commands

### Starting the Development Environment

**Docker-based (Recommended)**:
```bash
./scripts/start-dev.sh    # Starts MySQL + Backend + Frontend
docker-compose up -d       # Alternative: Start all services
docker-compose down        # Stop all services
```

**Local Development**:
```bash
./scripts/start-local.sh   # Checks environment and starts services locally
```

### Backend Commands
```bash
cd backend
./mvnw spring-boot:run                    # Start backend (runs on port 8080, context path /api)
./mvnw test                               # Run all tests
./mvnw clean package                      # Build JAR
```

### Frontend Commands
```bash
cd frontend
npm install                               # Install dependencies
npm run dev                              # Start dev server (port 5173)
npm run build                            # Production build
npm run test:unit                        # Run unit tests
npm run lint                             # Run ESLint
```

### Testing
```bash
./scripts/test-all.sh                     # Run all backend + frontend tests
```

### Database
```bash
./scripts/init-db.sh                      # Initialize MySQL database with schema and data
```

## Architecture Overview

### Backend Architecture (Spring Boot 3.x)

**Package Structure**:
- `config/` - Configuration classes (CORS, Cache, RestTemplate)
- `controller/` - REST API endpoints
- `service/` - Business logic layer
- `mapper/` - MyBatis data access layer (interfaces)
- `entity/` - JPA/MyBatis entity classes
- `dto/` - Data Transfer Objects (request/response)
- `exception/` - Global exception handling
- `github/` - GitHub API integration service
- `schedule/` - Scheduled tasks (hot score updates)
- `util/` - Utility classes

**Key Controllers**:
- `ToolController` (/tools) - Tool CRUD, search, favorites, view tracking, install tracking
- `RatingController` (/ratings) - Reviews, ratings, comments, likes
- `RankingController` (/tools/ranking) - Daily/weekly/all-time/trending rankings
- `SearchController` (/search) - Search functionality
- `GitHubController` (/github) - GitHub repository synchronization

**Data Access Pattern**:
- Uses **MyBatis** (NOT JPA) for database operations
- Mapper interfaces in `mapper/` package
- SQL queries defined in XML files in `src/main/resources/mapper/*.xml`
- MyBatis configured to map underscore_to_camelCase automatically

**Important Service Classes**:
- `ToolService` - Core tool operations, GitHub sync
- `RankingService` - Hot score calculation with caching, scheduled updates
- `RatingService` - Review and rating management
- `GitHubService` - GitHub API integration

**Authentication Model** (CRITICAL):
- **No JWT/Authentication system** - Login was removed
- Uses **clientIdentifier** based on IP + User-Agent for user identification
- All user-specific operations (favorites, ratings, views) use `clientIdentifier` string
- TokenService still exists but is legacy code

**Caching**:
- Redis used for distributed caching (not Caffeine)
- Cache configuration in `CacheConfig.java`
- Hot score rankings cached with 10-minute TTL
- Scheduled task updates hot scores every 10 minutes

**Configuration Files**:
- `application.yml` - Base configuration (active profile: dev, Redis config)
- `application-dev.yml` - Local development (localhost MySQL, localhost Redis)
- `application-prod.yml` - Production settings
- **Important**: Backend runs on port 8080 with context path `/api`

### Frontend Architecture (Vue 3)

**Structure**:
- `views/` - Page components (Home, Tools, ToolDetail, Search, Ranking)
- `components/` - Reusable components (layout, rating, tool cards)
- `stores/` - Pinia state management (tools, rating, ranking, user)
- `router/` - Vue Router configuration
- `api/` - Axios configuration and interceptors
- `assets/styles/` - Global SCSS (variables, dark theme, ranking styles)

**Key Pinia Stores**:
- `tools.js` - Tool listing, details, favorites, search
- `rating.js` - Reviews, ratings, statistics
- `ranking.js` - Ranking data with tab switching
- `user.js` - Simple user state (nickname only, no auth)

**Router Configuration**:
- Routes: / (Home), /tools, /tools/:id (ToolDetail), /search, /ranking
- **Important**: Router has beforeEach guard that removes ALL Element Plus overlays/dialogs on navigation (fixes dialog blocking issue)

**API Layer**:
- Axios instance configured in `api/index.js`
- BaseURL: `/api` (proxies to backend port 8080)
- Response interceptor extracts `response.data` automatically
- Error interceptor shows ElMessage for common errors

**Styling System**:
- Dark theme inspired by skills.sh
- Primary color: Neon green (#00ff9d)
- SCSS variables in `assets/styles/variables.scss`
- Element Plus component overrides in `assets/styles/dark-theme.scss`
- Custom ranking styles in `assets/styles/ranking.scss`

## Critical Implementation Details

### Hot Score Calculation Algorithm

Located in `RankingService.java`:
```java
// Different weights for different time periods
dailyRank: view=0.2, favorite=0.3, install=0.5
weeklyRank: view=0.3, favorite=0.3, install=0.4
alltimeRank: view=0.4, favorite=0.3, install=0.3

hotScore = (viewCount × weight) + (favoriteCount × 10 × weight) + (installCount × 5 × weight)
```

### Database Schema

**Core Tables**:
- `tools` - Main tool records with GitHub info, stats, hot scores
- `categories` - Tool categories
- `tool_tags` - Many-to-many relationship (tools ↔ tags)
- `favorites` - User favorites (identified by clientIdentifier)
- `ratings` - User ratings and reviews
- `rating_likes` - Likes on review comments
- `view_records` - View tracking for analytics
- `comment_replies` - Nested comment replies on reviews

**Important**: All user-specific tables use `client_identifier` (VARCHAR), NOT user_id.

### Client Identification System

**Backend** (in `ToolController`, `RatingController`):
```java
String clientIdentifier = request.getHeader("X-Client-Identifier");
if (clientIdentifier == null) {
    clientIdentifier = getClientIdentifierFromRequest(request); // IP + User-Agent
}
```

**Front** (needs to send header):
- Currently NOT implemented in frontend API layer
- Browser fingerprinting would be needed (consider FingerprintJS)
- For now, backend generates from IP + User-Agent

### GitHub Integration

- `GitHubService` fetches repository data (stars, forks, issues, watchers)
- `GitHubController` provides endpoints for manual sync
- Automatic sync scheduled or triggered by user action
- GitHub API base URL configurable in `application.yml`
- Token can be configured for higher rate limits

### Element Plus Dialog Issue Fix

**Problem**: Dialog overlays (`.el-overlay`) persist across route changes, blocking navigation.

**Solution** (implemented in router/index.js):
1. Router `beforeEach` guard removes all `.el-overlay` elements
2. Router `beforeEach` guard removes all `.el-dialog` elements
3. Cleans up `el-popup-parent--hidden` class from body
4. Global click listener in `main.js` as fallback

**When modifying navigation**: Always ensure overlays are cleaned up.

## Docker Configuration

**Services** (`docker-compose.yml`):
- `mysql` - MySQL 8.0 database
- `redis` - Redis server
- `backend` - Spring Boot app
- `frontend` - Vite dev server

**Development vs Production**:
- Development: `docker-compose.yml` - Uses hot-reload, mounts source code
- Production: `docker-compose.prod.yml` - Optimized builds, no hot-reload

**Database Credentials** (from DOCKER.md):
- User: `devtool`
- Password: `devtool123`
- Database: `devtoolmp`
- Port: 3306

## Common Development Tasks

### Adding a New API Endpoint

1. **Backend**:
   - Add method in appropriate `XxxController`
   - Implement business logic in `XxxService`
   - Add MyBatis mapper method if needed
   - Add SQL in `mapper/XxxMapper.xml`

2. **Frontend**:
   - Create store method in appropriate Pinia store
   - Call API using the `request` instance from `api/index.js`
   - Update component to use new data

### Adding a New Page

1. Create component in `views/`
2. Add route in `router/index.js`
3. Add navigation link in `AppHeader.vue` (if needed)
4. Update store if page needs state management

### Database Schema Changes

1. Modify entity class in `entity/`
2. Update mapper XML in `resources/mapper/*.xml`
3. Run SQL migration manually or restart with `ddl-auto: update` (dev only)
4. Update DTOs if fields exposed via API

### Debugging Tips

**Backend**:
- MyBatis SQL logged to stdout (configured in application.yml)
- Global exception handler in `GlobalExceptionHandler.java`
- Set breakpoints in service layer

**Frontend**:
- Router navigation logged to console ([Router] prefix)
- Check Network tab for API calls
- Vue Devtools for component inspection

## Important Gotchas

1. **No Authentication**: Don't look for JWT tokens or login logic. System is anonymous.
2. **MyBatis Not JPA**: All database operations use MyBatis XML mappers, NOT JPA repositories.
3. **Client Identifier**: User operations use IP+UA fingerprint, not user IDs.
4. **Overlay Cleanup**: Router guard removes Element Plus overlays - don't disable it.
5. **Context Path**: Backend API is at `/api` - frontend proxies accordingly.
6. **Redis Required**: Rankings use Redis cache - ensure Redis is running.
7. **Hot Score Updates**: Scheduled task updates every 10 minutes - changes not instant.

## Testing

**Backend Testing**:
- Unit tests with JUnit 5
- Integration tests can use Testcontainers for MySQL/Redis
- Test data in `src/test/resources/`

**Frontend Testing**:
- Unit tests with Vitest + Vue Test Utils
- E2E tests with Playwright
- Component testing in `tests/` directory
