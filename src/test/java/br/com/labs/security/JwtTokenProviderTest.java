package br.com.labs.security;

import br.com.labs.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private User user;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "secret", "mySuperSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong!");
        ReflectionTestUtils.setField(tokenProvider, "expiration", 86400000L);

        user = new User("Felipe", "felipe@email.com", "password");
        user.setId(1L);
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateTokenTests {

        @Test
        @DisplayName("should generate valid JWT token")
        void shouldGenerateValidToken() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            String token = tokenProvider.generateToken(authentication);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }
    }

    @Nested
    @DisplayName("validateToken()")
    class ValidateTokenTests {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrueForValidToken() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            String token = tokenProvider.generateToken(authentication);

            boolean isValid = tokenProvider.validateToken(token);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("should return false for invalid token")
        void shouldReturnFalseForInvalidToken() {
            boolean isValid = tokenProvider.validateToken("invalid.token.here");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false for null token")
        void shouldReturnFalseForNullToken() {
            boolean isValid = tokenProvider.validateToken(null);

            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("getUserIdFromToken()")
    class GetUserIdFromTokenTests {

        @Test
        @DisplayName("should extract user id from token")
        void shouldExtractUserIdFromToken() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            String token = tokenProvider.generateToken(authentication);

            Long userId = tokenProvider.getUserIdFromToken(token);

            assertThat(userId).isEqualTo(1L);
        }
    }
}
