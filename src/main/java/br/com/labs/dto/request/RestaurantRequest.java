package br.com.labs.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestaurantRequest(
    @NotBlank @Size(max = 200) String name,
    @NotBlank String description,
    @Size(max = 300) String location,
    @Min(1) @Max(5) Integer rating
) {}
