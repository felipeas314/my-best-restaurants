package br.com.labs.service;

import br.com.labs.dto.request.RestaurantRequest;
import br.com.labs.dto.response.RestaurantResponse;
import br.com.labs.exception.ResourceNotFoundException;
import br.com.labs.model.Restaurant;
import br.com.labs.model.User;
import br.com.labs.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private User user;
    private Restaurant restaurant;
    private RestaurantRequest request;

    @BeforeEach
    void setUp() {
        user = new User("Felipe", "felipe@email.com", "password");
        user.setId(1L);

        restaurant = new Restaurant("Outback", "Melhor costela!", "Shopping", 5, user);
        restaurant.setId(1L);

        request = new RestaurantRequest("Outback", "Melhor costela!", "Shopping", 5);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create restaurant successfully")
        void shouldCreateRestaurant() {
            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

            RestaurantResponse response = restaurantService.create(request, user);

            assertThat(response.name()).isEqualTo("Outback");
            assertThat(response.description()).isEqualTo("Melhor costela!");
            assertThat(response.createdByName()).isEqualTo("Felipe");
            verify(restaurantRepository).save(any(Restaurant.class));
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("should return paginated restaurants")
        void shouldReturnPaginatedRestaurants() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Restaurant> page = new PageImpl<>(List.of(restaurant));
            when(restaurantRepository.findAll(pageable)).thenReturn(page);

            Page<RestaurantResponse> result = restaurantService.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("Outback");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("should return restaurant when found")
        void shouldReturnRestaurantWhenFound() {
            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            RestaurantResponse response = restaurantService.findById(1L);

            assertThat(response.name()).isEqualTo("Outback");
            assertThat(response.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> restaurantService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Restaurant not found");
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("should update restaurant when user is owner")
        void shouldUpdateWhenOwner() {
            RestaurantRequest updateRequest = new RestaurantRequest("Outback Steakhouse", "A melhor costela!", "Mall", 5);
            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

            RestaurantResponse response = restaurantService.update(1L, updateRequest, user);

            assertThat(response).isNotNull();
            verify(restaurantRepository).save(any(Restaurant.class));
        }

        @Test
        @DisplayName("should throw exception when user is not owner")
        void shouldThrowExceptionWhenNotOwner() {
            User otherUser = new User("Outro", "outro@email.com", "password");
            otherUser.setId(2L);

            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            assertThatThrownBy(() -> restaurantService.update(1L, request, otherUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("only update your own");
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("should delete restaurant when user is owner")
        void shouldDeleteWhenOwner() {
            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            doNothing().when(restaurantRepository).delete(restaurant);

            restaurantService.delete(1L, user);

            verify(restaurantRepository).delete(restaurant);
        }

        @Test
        @DisplayName("should throw exception when user is not owner")
        void shouldThrowExceptionWhenNotOwner() {
            User otherUser = new User("Outro", "outro@email.com", "password");
            otherUser.setId(2L);

            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

            assertThatThrownBy(() -> restaurantService.delete(1L, otherUser))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }
}
