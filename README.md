# -Twitter-like-Application
Design and implement a simplified Twitter-like application that allows authenticated users to create short posts (maximum 140 characters) in a single public stream/feed. The project must start as a Spring Boot monolith, evolve into serverless microservices on AWS, and be fully secured using Auth0.

## Monolith Integration Test Evidence

The Spring Boot monolith backend is covered by an integration suite focused on security, validation, and functional behavior using real HTTP layer testing.

### Main coverage

- **Suite size:** 15 integration tests
- **Tools:** JUnit 5, MockMvc, spring-security-test
- **Public endpoints:** `GET /api/posts`, `GET /api/stream`
- **Protected endpoints:** `POST /api/posts` (scope `write:posts`), `GET /api/me` (scope `read:profile`)
- **Validation:** empty/blank content, max length > 140, exact length 140
- **Auth0 scopes:** access control verified with JWTs with and without required scopes
- **Persistence:** post creation verified against real persisted data in tests

### Execution result

```text
Tests run: 15
Failures: 0
Errors: 0
Build: SUCCESS
```
