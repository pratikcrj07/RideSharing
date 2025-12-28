package com.ridesharing.Repository;

import com.ridesharing.Entities.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
    Optional<Driver> findByUserId(Long userId);
    boolean existsByEmail(String email);
}
