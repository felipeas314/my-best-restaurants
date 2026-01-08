package br.com.labs.service;

import br.com.labs.dto.request.RestaurantRequest;
import br.com.labs.dto.response.RestaurantResponse;
import br.com.labs.exception.ResourceNotFoundException;
import br.com.labs.model.Restaurant;
import br.com.labs.model.User;
import br.com.labs.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public RestaurantResponse create(RestaurantRequest request, User currentUser) {
        Restaurant restaurant = new Restaurant(
            request.name(),
            request.description(),
            request.location(),
            request.rating(),
            currentUser
        );

        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromEntity(saved);
    }

    public Page<RestaurantResponse> findAll(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
            .map(RestaurantResponse::fromEntity);
    }

    public Page<RestaurantResponse> findByUser(Long userId, Pageable pageable) {
        return restaurantRepository.findByCreatedById(userId, pageable)
            .map(RestaurantResponse::fromEntity);
    }

    public RestaurantResponse findById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));
        return RestaurantResponse.fromEntity(restaurant);
    }

    @Transactional
    public RestaurantResponse update(Long id, RestaurantRequest request, User currentUser) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));

        if (!restaurant.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only update your own restaurants");
        }

        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setLocation(request.location());
        restaurant.setRating(request.rating());

        Restaurant updated = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromEntity(updated);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));

        boolean isOwner = restaurant.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only delete your own restaurants");
        }

        restaurantRepository.delete(restaurant);
    }
}
