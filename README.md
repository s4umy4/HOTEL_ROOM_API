# Room Management API

A REST API built with Java and Spring Boot for managing rooms. It supports creating, reading, updating, deleting, listing, filtering, searching, sorting, and paginating room records.

## Features

- Create a room
- Retrieve a room by ID
- Replace a room with `PUT`
- Partially update a room with `PATCH`
- Delete a room
- List rooms with pagination
- Filter by room type, availability, and price range
- Search room names and locations
- Sort by name, price per night, or capacity
- Validate request data and return JSON error responses
- Enforce unique room numbers

## Requirements

Install the following before running the application:

- **Java 17 or later** (use the version configured in your project)
- **Maven** (or use the Maven Wrapper if the project contains `mvnw` / `mvnw.cmd`)
- An API client such as Postman, curl, or the VS Code REST Client extension

Check your Java and Maven installations:

```bash
java -version
mvn -version
```

If your project includes a Maven Wrapper, you can use it instead of installing Maven:

- macOS/Linux: `./mvnw`
- Windows: `mvnw.cmd`

## Project setup

1. Open the project folder in VS Code or your preferred IDE.
2. Confirm that `pom.xml` is in the project root.
3. Confirm the source files are under `src/main/java/com/example/demo/`, including the controller, service, model, validator, and exception classes.
4. Open a terminal at the folder containing `pom.xml`.

Example project layout:

```text
your-project/
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── demo/
│                       ├── RoomController.java
│                       ├── RoomService.java
│                       ├── Room.java
│                       ├── RoomValidator.java
│                       ├── ApiException.java
│                       └── ApiExceptionHandler.java
└── README.md
```

Your project may contain additional Spring Boot application or test files; keep them in place.

## Build and run

### macOS / Linux

From the project root:

```bash
mvn clean package
mvn spring-boot:run
```

If you have the Maven Wrapper:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

### Windows PowerShell

From the project root:

```powershell
mvn clean package
mvn spring-boot:run
```

If you have the Maven Wrapper:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

When the application starts successfully, the API should be available at:

```text
http://localhost:8080
```

Keep the terminal running while you use the API. To stop the server, press `Ctrl+C`.

### Run the packaged JAR (optional)

After building, run the JAR produced in `target/`:

```bash
java -jar target/<generated-file-name>.jar
```

Replace `<generated-file-name>.jar` with the actual JAR filename inside `target/`.

## API endpoints

Base URL: `http://localhost:8080/api/rooms`

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/rooms` | Create a room |
| `GET` | `/api/rooms/{id}` | Get one room by ID |
| `GET` | `/api/rooms` | List, search, filter, sort, and paginate rooms |
| `PUT` | `/api/rooms/{id}` | Replace a room's fields |
| `PATCH` | `/api/rooms/{id}` | Update selected fields |
| `DELETE` | `/api/rooms/{id}` | Delete a room |

## Room fields

A room request uses these fields:

| Field | Type | Validation |
|---|---|---|
| `name` | string | 3–60 characters |
| `location` | string | 2–40 characters |
| `type` | string | `Single`, `Double`, `Suite`, `Deluxe`, or `Family` |
| `pricePerNight` | number | Greater than 0 and at most 100000 |
| `capacity` | integer | Whole number from 1 to 10 |
| `available` | boolean | `true` or `false` |
| `roomNumber` | string | 4–20 characters; must be unique |

The API generates the room `id`; do not supply it when creating a room. For updates, the URL ID identifies the record.

## Example requests

### Create a room

```bash
curl -i -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ocean View Suite",
    "location": "Chennai",
    "type": "Suite",
    "pricePerNight": 4500,
    "capacity": 3,
    "available": true,
    "roomNumber": "CHN-101"
  }'
```

A successful create returns HTTP `201 Created` and the created room, including its generated ID.

### Get a room by ID

Replace `1` with an ID returned by the create request:

```bash
curl -i http://localhost:8080/api/rooms/1
```

### List rooms

```bash
curl -i "http://localhost:8080/api/rooms"
```

The list response includes `data`, `page`, `limit`, `total`, and `totalPages`.

### Pagination

```bash
curl -i "http://localhost:8080/api/rooms?page=1&limit=5"
```

The default page is `1`, the default page size is `10`, and the maximum page size is `50`.

### Filter rooms

```bash
curl -i "http://localhost:8080/api/rooms?type=Suite"
curl -i "http://localhost:8080/api/rooms?available=true"
curl -i "http://localhost:8080/api/rooms?minPricePerNight=1000&maxPricePerNight=5000"
```

Combine filters when needed:

```bash
curl -i "http://localhost:8080/api/rooms?type=Suite&available=true&page=1&limit=5"
```

### Search rooms

Search is case-insensitive and checks room names and locations:

```bash
curl -i "http://localhost:8080/api/rooms?search=ocean"
```

### Sort rooms

Supported sort fields are `name`, `pricePerNight`, and `capacity`. The order can be `asc` or `desc`.

```bash
curl -i "http://localhost:8080/api/rooms?sort=pricePerNight&order=desc"
curl -i "http://localhost:8080/api/rooms?sort=name&order=asc"
```

### Replace a room (PUT)

`PUT` requires all room fields except the generated ID:

```bash
curl -i -X PUT http://localhost:8080/api/rooms/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Suite",
    "location": "Chennai",
    "type": "Deluxe",
    "pricePerNight": 5200,
    "capacity": 4,
    "available": true,
    "roomNumber": "CHN-101"
  }'
```

Use a room number that is not already assigned to another room.

### Partially update a room (PATCH)

`PATCH` only needs the fields you want to change:

```bash
curl -i -X PATCH http://localhost:8080/api/rooms/1 \
  -H "Content-Type: application/json" \
  -d '{
    "pricePerNight": 4800,
    "available": false
  }'
```

### Delete a room

```bash
curl -i -X DELETE http://localhost:8080/api/rooms/1
```

A successful delete returns HTTP `204 No Content`.

## Error responses

Errors are returned as JSON, usually in this format:

```json
{
  "error": "Description of the problem"
}
```

Common status codes:

- `400 Bad Request` — invalid JSON, missing fields, invalid values, or invalid query parameters
- `404 Not Found` — room ID or route not found
- `409 Conflict` — duplicate room number
- `500 Internal Server Error` — unexpected server-side error

## Running tests

Run the project's automated tests, if present:

```bash
mvn test
```

To build and run the application in one command:

```bash
mvn spring-boot:run
```

If you are using a separate API evaluation script, start the server first and then run that script in its own terminal. Ensure it targets `http://localhost:8080`.

## Troubleshooting

### Port 8080 is already in use

Stop the other process using port `8080`, or configure a different server port in `src/main/resources/application.properties`:

```properties
server.port=8081
```

If you change the port, update the API base URL in your client or evaluation script as well.

### `mvn` command not found

Install Maven or use the Maven Wrapper if available in the project.

### Java version mismatch

Check `java -version` and the Java version required by `pom.xml`. Configure your IDE and terminal to use a compatible JDK.

### Application starts but requests fail

- Confirm the server startup completed without errors.
- Check that the URL begins with `http://localhost:8080/api/rooms`.
- For POST, PUT, and PATCH, include the `Content-Type: application/json` header.
- Make sure request bodies contain valid JSON and the required fields.
- Check the terminal logs for exception details.

## Notes

- The current service implementation stores rooms in memory. Records are lost when the application restarts unless persistence is added.
- This README documents the API based on the current source structure and validation rules. Adjust the commands if your `pom.xml`, Java version, or run configuration differs.
