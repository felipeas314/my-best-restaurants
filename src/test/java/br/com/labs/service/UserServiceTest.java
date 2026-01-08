package br.com.labs.service;

import br.com.labs.dto.request.RegisterRequest;
import br.com.labs.model.Role;
import br.com.labs.model.User;
import br.com.labs.repository.RoleRepository;
import br.com.labs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest request;
    private Role userRole;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest("Felipe", "felipe@email.com", "password123");
        userRole = new Role("ROLE_USER");
        userRole.setId(1L);
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("should register user successfully")
        void shouldRegisterUserSuccessfully() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            User result = userService.register(request);

            assertThat(result.getName()).isEqualTo("Felipe");
            assertThat(result.getEmail()).isEqualTo("felipe@email.com");
            assertThat(result.getPassword()).isEqualTo("encodedPassword");
            assertThat(result.getRoles()).contains(userRole);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            when(userRepository.existsByEmail("felipe@email.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already registered");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should create ROLE_USER if not exists")
        void shouldCreateRoleIfNotExists() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
            when(roleRepository.save(any(Role.class))).thenReturn(userRole);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            userService.register(request);

            verify(roleRepository).save(any(Role.class));
        }
    }
}
