# Testing Guide

This guide demonstrates how to test the Ktor Sample API endpoints.

## Prerequisites

Make sure the server is running:
```bash
./gradlew run
```

The server will start on `http://localhost:8080`

## API Endpoints

### 1. Register a New User

**Endpoint:** `POST /auth/register`

**Request:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

**Response (201 Created):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. Login

**Endpoint:** `POST /auth/login`

**Request:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Access Protected Endpoint

**Endpoint:** `GET /kotr/ping`

**Request:**
```bash
# Replace YOUR_ACCESS_TOKEN with the actual token
curl -X GET http://localhost:8080/kotr/ping \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Response (200 OK):**
```json
{
  "message": "pong",
  "username": "testuser"
}
```

### 4. Refresh Access Token

**Endpoint:** `POST /auth/refresh`

**Request:**
```bash
# Replace YOUR_REFRESH_TOKEN with the actual token
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Note:** The old refresh token will be revoked and can no longer be used.

## Complete Test Flow

Here's a complete test flow you can run:

```bash
# 1. Register a new user
RESPONSE=$(curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"myuser","password":"mypass"}' \
  -s)

# Extract tokens
ACCESS_TOKEN=$(echo "$RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo "$RESPONSE" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

# 2. Test protected endpoint
curl -X GET http://localhost:8080/kotr/ping \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# 3. Refresh tokens
NEW_RESPONSE=$(curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" \
  -s)

NEW_ACCESS_TOKEN=$(echo "$NEW_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

# 4. Test with new access token
curl -X GET http://localhost:8080/kotr/ping \
  -H "Authorization: Bearer $NEW_ACCESS_TOKEN"

# 5. Try old refresh token (should fail)
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

## Error Responses

### Duplicate Username (400 Bad Request)
```json
{
  "error": "User already exists"
}
```

### Invalid Credentials (401 Unauthorized)
```json
{
  "error": "Invalid credentials"
}
```

### Invalid/Revoked Refresh Token (401 Unauthorized)
```json
{
  "error": "Refresh token not found or revoked"
}
```

### Unauthorized Access (401 Unauthorized)
When accessing `/kotr/ping` without a valid token, you'll receive a 401 status with no body.

## Token Details

- **Access Token:** Valid for 15 minutes (900 seconds)
- **Refresh Token:** Valid for 30 days (2,592,000 seconds)
- Refresh tokens are rotated on each refresh - old token is revoked
- All refresh tokens are revoked when a user logs in or registers

## Configuration

Token TTLs and other settings can be configured in `src/main/resources/application.conf`:

```hocon
security {
    jwt {
        secret = ${?JWT_SECRET}  # Set via environment variable
        accessTokenTTL = 900     # 15 minutes
        refreshTokenTTL = 2592000  # 30 days
    }
}
```
