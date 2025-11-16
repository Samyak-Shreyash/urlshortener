# URL Shortener API

A RESTful URL shortener service built with Java that provides API endpoints for converting long URLs into short 6-character codes and vice versa.

## Features

* **REST API** : HTTP endpoints for URL shortening and expansion
* **URL Shortening** : Convert long URLs into 6-character short codes
* **URL Redirection** : Redirect short codes to their original URLs
* **JSON Responses** : Clean API responses in JSON format
* **In-Memory Storage** : Uses HashMap for fast lookups
* **Gradle Build** : Easy dependency management and build automation

## Project Structure

**text**

```
url-shortener-api/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── urlshortener/
│                   ├── controller/
│                   │   └── URLController.java
│                   ├── service/
│                   │   ├── URLShortenerService.java
│                   ├── model/
│                   │   └── UrlMapping.java
model/
│                   │   └── UrlMapping.java
│                   └── Application.java
├── .env
├── build.gradle
├── settings.gradle
└── README.md
```

## Installation

### Prerequisites

* Java 11 or higher
* Gradle 7.0 or higher

### Steps

1. Clone or download the project files
2. Navigate to the project directory
3. Build the project:
   **bash**

   ```
   ./gradlew build
   ```
4. Run the application:
   **bash**

   ```
   ./gradlew bootRun
   ```

The API will be available at `http://localhost:8080`

## Gradle Configuration

### build.gradle

**groovy**

```
plugins {
    id 'org.springframework.boot' version '2.7.0'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
}

group = 'com.urlshortener'
version = '1.0.0'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
  
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
}

test {
    useJUnitPlatform()
}
```

### settings.gradle

**groovy**

```
rootProject.name = 'url-shortener-api'
```

## API Endpoints

### Shorten URL

* **URL** : `POST /api/shorten`
* **Description** : Converts a long URL to a short code
* **Request Body** :
  **json**

```
{
  "url": "https://www.example.com/very/long/url/path"
}
```

* **Response** :
  **json**

```
{
  "shortCode": "abc123",
  "shortUrl": "http://localhost:8080/api/abc123",
  "originalUrl": "https://www.example.com/very/long/url/path"
}
```

### Expand URL

* **URL** : `GET /api/expand/{shortCode}`
* **Description** : Retrieves original URL from a short code
* **Response** :
  **json**

```
{
  "shortCode": "abc123",
  "originalUrl": "https://www.example.com/very/long/url/path",
  "shortUrl": "http://localhost:8080/api/abc123"
}
```

### Redirect to Original URL

* **URL** : `GET /api/{shortCode}`
* **Description** : Redirects to the original URL (HTTP 302)
* **Response** : HTTP 302 Redirect to original URL

### Get Statistics

* **URL** : `GET /api/stats`
* **Description** : Returns service statistics
* **Response** :
  **json**

```
{
  "totalUrls": 15,
  "service": "URL Shortener API",
  "version": "1.0.0"
}
```

## Code Implementation

### Application.java

**java**

```
package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### model/ShortenRequest.java

**java**

```
package com.urlshortener.model;

import javax.validation.constraints.NotBlank;

public class ShortenRequest {
  
    @NotBlank(message = "URL is required")
    private String url;

    // Default constructor
    public ShortenRequest() {}

    public ShortenRequest(String url) {
        this.url = url;
    }

    // Getters and setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
```

### service/URLShortenerService.java

**java**

```
package com.urlshortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class URLShortenerService {
  
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private final Random random = new Random();
  
    @Autowired
    private URLStorageService storageService;

    public String shortenURL(String originalURL) {
        // Check if URL already has a short code
        String existingCode = storageService.getShortCode(originalURL);
        if (existingCode != null) {
            return existingCode;
        }

        String shortCode = generateShortCode();
        storageService.storeURL(shortCode, originalURL);
        return shortCode;
    }

    public String expandURL(String shortCode) {
        return storageService.getOriginalURL(shortCode);
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        String code = sb.toString();
  
        // Ensure uniqueness
        if (storageService.containsShortCode(code)) {
            return generateShortCode();
        }
        return code;
    }

    public int getTotalURLs() {
        return storageService.getSize();
    }
}
```

### controller/URLController.java

**java**

```
package com.urlshortener.controller;

import com.urlshortener.model.ShortenRequest;
import com.urlshortener.service.URLShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class URLController {

    @Autowired
    private URLShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shortenURL(@Valid @RequestBody ShortenRequest request) {
        String originalURL = request.getUrl();
        String shortCode = urlShortenerService.shortenURL(originalURL);
  
        Map<String, String> response = new HashMap<>();
        response.put("shortCode", shortCode);
        response.put("shortUrl", "http://localhost:8080/api/" + shortCode);
        response.put("originalUrl", originalURL);
  
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expand/{shortCode}")
    public ResponseEntity<?> expandURL(@PathVariable String shortCode) {
        String originalUrl = urlShortenerService.expandURL(shortCode);
  
        if (originalUrl != null) {
            Map<String, String> response = new HashMap<>();
            response.put("shortCode", shortCode);
            response.put("originalUrl", originalUrl);
            response.put("shortUrl", "http://localhost:8080/api/" + shortCode);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Short code not found");
            error.put("code", shortCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/{shortCode}")
    public void redirectToOriginalURL(
            @PathVariable String shortCode, 
            HttpServletResponse response) throws IOException {
  
        String originalUrl = urlShortenerService.expandURL(shortCode);
  
        if (originalUrl != null) {
            response.sendRedirect(originalUrl);
        } else {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Short code not found");
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("service", "URL Shortener API");
        stats.put("version", "1.0.0");
        stats.put("totalUrls", urlShortenerService.getTotalURLs());
  
        return ResponseEntity.ok(stats);
    }
}
```

## API Usage Examples

### Using curl

**Shorten a URL:**

**bash**

```
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.example.com/very/long/url/path"}'
```

**Expand a short code:**

**bash**

```
curl http://localhost:8080/api/expand/abc123
```

**Get statistics:**

**bash**

```
curl http://localhost:8080/api/stats
```

### Using JavaScript Fetch

**javascript**

```
// Shorten URL
const shortenResponse = await fetch('http://localhost:8080/api/shorten', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        url: 'https://www.example.com/very/long/url/path'
    })
});
const shortData = await shortenResponse.json();
console.log(shortData.shortUrl);

// Expand URL
const expandResponse = await fetch('http://localhost:8080/api/expand/abc123');
const expandData = await expandResponse.json();
console.log(expandData.originalUrl);
```

## Error Responses

The API returns standard HTTP status codes:

* `200 OK` - Successful request
* `400 Bad Request` - Invalid input (e.g., missing URL)
* `404 Not Found` - Short code not found
* `500 Internal Server Error` - Server error

Example error response:

**json**

```
{
  "error": "Short code not found",
  "code": "invalid123"
}
```

## Testing the API

Run the application and test with:

1. **Shorten a URL:**
   **bash**

   ```
   curl -X POST http://localhost:8080/api/shorten -H "Content-Type: application/json" -d '{"url":"https://spring.io/projects/spring-boot"}'
   ```
2. **Expand the short code** (use the code from step 1):
   **bash**

   ```
   curl http://localhost:8080/api/expand/abc123
   ```
3. **Redirect to original URL:**
   Open in browser: `http://localhost:8080/api/abc123`
4. **Check statistics:**
   **bash**

   ```
   curl http://localhost:8080/api/stats
   ```

## Configuration

### Customizing Server Port

Add to `src/main/resources/application.properties`:

**properties**

```
server.port=8081
server.servlet.context-path=/url-shortener
```

### Adding CORS Support

Add to `URLController.java`:

**java**

```
@CrossOrigin(origins = "*")
public class URLController {
    // ...
}
```

## Future Enhancements

* Database persistence (H2, PostgreSQL)
* URL validation and sanitization
* Custom short codes
* URL expiration
* Click analytics
* Rate limiting
* API authentication (JWT)
* Swagger/OpenAPI documentation
* Docker containerization
* Cache implementation (Redis)
* Bulk URL shortening

## License

This project is open source and available under the [MIT License](https://license/).
