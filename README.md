# Machine Coding Assessment

Spring Boot starter project for machine coding rounds.

## Stack

- Java 17
- Spring Boot 3.5.0
- Spring Web
- Spring Data JPA
- Spring Validation
- H2 Database
- Lombok

## Run

```bash
./mvnw spring-boot:run
```

## Test

```bash
./mvnw test
```

## H2 Console

When the app is running:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:machinecoding`
- Username: `sa`
- Password: leave empty

## Push To GitHub

Create an empty repo under `Gagan1234567`, then run:

```bash
git remote add origin https://github.com/Gagan1234567/<repo-name>.git
git branch -M main
git push -u origin main
```
