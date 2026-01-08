package br.com.labs.integration;

import br.com.labs.dto.request.LoginRequest;
import br.com.labs.dto.request.RegisterRequest;
import br.com.labs.dto.response.TokenResponse;
import br.com.labs.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("should register user successfully")
        void shouldRegisterUserSuccessfully() {
            RegisterRequest request = new RegisterRequest("Felipe", "felipe@test.com", "password123");

            ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                    "/api/auth/register",
                    request,
                    UserResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().name()).isEqualTo("Felipe");
            assertThat(response.getBody().email()).isEqualTo("felipe@test.com");
            assertThat(response.getBody().id()).isNotNull();
        }

        @Test
        @DisplayName("should return bad request for duplicate email")
        void shouldReturnBadRequestForDuplicateEmail() {
            RegisterRequest request = new RegisterRequest("User1", "duplicate@test.com", "password123");
            restTemplate.postForEntity("/api/auth/register", request, UserResponse.class);

            RegisterRequest duplicateRequest = new RegisterRequest("User2", "duplicate@test.com", "password456");
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/auth/register",
                    duplicateRequest,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should return bad request for invalid email")
        void shouldReturnBadRequestForInvalidEmail() {
            RegisterRequest request = new RegisterRequest("Felipe", "invalid-email", "password123");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/auth/register",
                    request,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("should login successfully and return token")
        void shouldLoginSuccessfully() {
            // Register user first
            RegisterRequest registerRequest = new RegisterRequest("LoginUser", "login@test.com", "password123");
            restTemplate.postForEntity("/api/auth/register", registerRequest, UserResponse.class);

            // Login
            LoginRequest loginRequest = new LoginRequest("login@test.com", "password123");
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    "/api/auth/login",
                    loginRequest,
                    TokenResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().token()).isNotNull();
            assertThat(response.getBody().token()).isNotEmpty();
            assertThat(response.getBody().type()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("should return unauthorized for invalid credentials")
        void shouldReturnUnauthorizedForInvalidCredentials() {
            LoginRequest loginRequest = new LoginRequest("nonexistent@test.com", "wrongpassword");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/auth/login",
                    loginRequest,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
