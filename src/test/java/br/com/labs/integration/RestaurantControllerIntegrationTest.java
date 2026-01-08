package br.com.labs.integration;

import br.com.labs.dto.request.LoginRequest;
import br.com.labs.dto.request.RegisterRequest;
import br.com.labs.dto.request.RestaurantRequest;
import br.com.labs.dto.response.RestaurantResponse;
import br.com.labs.dto.response.TokenResponse;
import br.com.labs.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;
    private String userEmail;

    @BeforeEach
    void setUp() {
        // Create unique user for each test
        userEmail = "user" + System.currentTimeMillis() + "@test.com";
        RegisterRequest registerRequest = new RegisterRequest("Test User", userEmail, "password123");
        restTemplate.postForEntity("/api/auth/register", registerRequest, UserResponse.class);

        // Login to get token
        LoginRequest loginRequest = new LoginRequest(userEmail, "password123");
        ResponseEntity<TokenResponse> loginResponse = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                TokenResponse.class
        );
        authToken = loginResponse.getBody().token();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Nested
    @DisplayName("GET /api/restaurants")
    class GetAllRestaurantsTests {

        @Test
        @DisplayName("should return empty page when no restaurants")
        void shouldReturnEmptyPage() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/restaurants",
                    Map.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("should return restaurants without authentication")
        void shouldReturnRestaurantsWithoutAuth() {
            // Create a restaurant first
            RestaurantRequest request = new RestaurantRequest("Outback", "Melhor costela!", "Shopping", 5);
            HttpEntity<RestaurantRequest> entity = new HttpEntity<>(request, createAuthHeaders());
            restTemplate.postForEntity("/api/restaurants", entity, RestaurantResponse.class);

            // Get all without auth
            ResponseEntity<Map> response = restTemplate.getForEntity("/api/restaurants", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("POST /api/restaurants")
    class CreateRestaurantTests {

        @Test
        @DisplayName("should create restaurant successfully")
        void shouldCreateRestaurantSuccessfully() {
            RestaurantRequest request = new RestaurantRequest(
                    "Outback Steakhouse",
                    "A melhor costela da cidade!",
                    "Shopping Center Norte",
                    5
            );

            HttpEntity<RestaurantRequest> entity = new HttpEntity<>(request, createAuthHeaders());
            ResponseEntity<RestaurantResponse> response = restTemplate.postForEntity(
                    "/api/restaurants",
                    entity,
                    RestaurantResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().name()).isEqualTo("Outback Steakhouse");
            assertThat(response.getBody().description()).isEqualTo("A melhor costela da cidade!");
            assertThat(response.getBody().rating()).isEqualTo(5);
            assertThat(response.getBody().createdByName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("should return unauthorized without token")
        void shouldReturnUnauthorizedWithoutToken() {
            RestaurantRequest request = new RestaurantRequest("Test", "Description", "Location", 3);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/restaurants",
                    request,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("should return bad request for invalid data")
        void shouldReturnBadRequestForInvalidData() {
            RestaurantRequest request = new RestaurantRequest("", "", null, 10); // Invalid rating

            HttpEntity<RestaurantRequest> entity = new HttpEntity<>(request, createAuthHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/restaurants",
                    entity,
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/restaurants/{id}")
    class GetRestaurantByIdTests {

        @Test
        @DisplayName("should return restaurant by id")
        void shouldReturnRestaurantById() {
            // Create restaurant
            RestaurantRequest request = new RestaurantRequest("Pizzaria", "Melhor pizza!", "Centro", 4);
            HttpEntity<RestaurantRequest> entity = new HttpEntity<>(request, createAuthHeaders());
            ResponseEntity<RestaurantResponse> createResponse = restTemplate.postForEntity(
                    "/api/restaurants",
                    entity,
                    RestaurantResponse.class
            );
            Long restaurantId = createResponse.getBody().id();

            // Get by id
            ResponseEntity<RestaurantResponse> response = restTemplate.getForEntity(
                    "/api/restaurants/" + restaurantId,
                    RestaurantResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().name()).isEqualTo("Pizzaria");
        }

        @Test
        @DisplayName("should return not found for non-existent id")
        void shouldReturnNotFoundForNonExistentId() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    "/api/restaurants/99999",
                    String.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PUT /api/restaurants/{id}")
    class UpdateRestaurantTests {

        @Test
        @DisplayName("should update restaurant successfully")
        void shouldUpdateRestaurantSuccessfully() {
            // Create restaurant
            RestaurantRequest createRequest = new RestaurantRequest("Old Name", "Old description", "Old Location", 3);
            HttpEntity<RestaurantRequest> createEntity = new HttpEntity<>(createRequest, createAuthHeaders());
            ResponseEntity<RestaurantResponse> createResponse = restTemplate.postForEntity(
                    "/api/restaurants",
                    createEntity,
                    RestaurantResponse.class
            );
            Long restaurantId = createResponse.getBody().id();

            // Update
            RestaurantRequest updateRequest = new RestaurantRequest("New Name", "New description", "New Location", 5);
            HttpEntity<RestaurantRequest> updateEntity = new HttpEntity<>(updateRequest, createAuthHeaders());
            ResponseEntity<RestaurantResponse> response = restTemplate.exchange(
                    "/api/restaurants/" + restaurantId,
                    HttpMethod.PUT,
                    updateEntity,
                    RestaurantResponse.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().name()).isEqualTo("New Name");
            assertThat(response.getBody().rating()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("DELETE /api/restaurants/{id}")
    class DeleteRestaurantTests {

        @Test
        @DisplayName("should delete restaurant successfully")
        void shouldDeleteRestaurantSuccessfully() {
            // Create restaurant
            RestaurantRequest request = new RestaurantRequest("To Delete", "Will be deleted", "Somewhere", 1);
            HttpEntity<RestaurantRequest> createEntity = new HttpEntity<>(request, createAuthHeaders());
            ResponseEntity<RestaurantResponse> createResponse = restTemplate.postForEntity(
                    "/api/restaurants",
                    createEntity,
                    RestaurantResponse.class
            );
            Long restaurantId = createResponse.getBody().id();

            // Delete
            HttpEntity<Void> deleteEntity = new HttpEntity<>(createAuthHeaders());
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/restaurants/" + restaurantId,
                    HttpMethod.DELETE,
                    deleteEntity,
                    Void.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify deleted
            ResponseEntity<String> getResponse = restTemplate.getForEntity(
                    "/api/restaurants/" + restaurantId,
                    String.class
            );
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /api/restaurants/my")
    class GetMyRestaurantsTests {

        @Test
        @DisplayName("should return only user's restaurants")
        void shouldReturnOnlyUsersRestaurants() {
            // Create restaurants
            RestaurantRequest request1 = new RestaurantRequest("My Restaurant 1", "Desc 1", "Loc 1", 5);
            RestaurantRequest request2 = new RestaurantRequest("My Restaurant 2", "Desc 2", "Loc 2", 4);

            HttpEntity<RestaurantRequest> entity1 = new HttpEntity<>(request1, createAuthHeaders());
            HttpEntity<RestaurantRequest> entity2 = new HttpEntity<>(request2, createAuthHeaders());

            restTemplate.postForEntity("/api/restaurants", entity1, RestaurantResponse.class);
            restTemplate.postForEntity("/api/restaurants", entity2, RestaurantResponse.class);

            // Get my restaurants
            HttpEntity<Void> getEntity = new HttpEntity<>(createAuthHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(
                    "/api/restaurants/my",
                    HttpMethod.GET,
                    getEntity,
                    Map.class
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }
}
