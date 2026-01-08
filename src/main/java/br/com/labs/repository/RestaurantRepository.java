package br.com.labs.repository;

import br.com.labs.model.Restaurant;
import br.com.labs.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Page<Restaurant> findByCreatedBy(User user, Pageable pageable);

    Page<Restaurant> findByCreatedById(Long userId, Pageable pageable);
}
