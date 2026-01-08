package br.com.labs.dto.response;

import br.com.labs.model.Restaurant;

import java.time.LocalDateTime;

public record RestaurantResponse(
    Long id,
    String name,
    String description,
    String location,
    Integer rating,
    String createdByName,
    LocalDateTime createdAt
) {
    public static RestaurantResponse fromEntity(Restaurant restaurant) {
        return new RestaurantResponse(
            restaurant.getId(),
            restaurant.getName(),
            restaurant.getDescription(),
            restaurant.getLocation(),
            restaurant.getRating(),
            restaurant.getCreatedBy().getName(),
            restaurant.getCreatedAt()
        );
    }
}
