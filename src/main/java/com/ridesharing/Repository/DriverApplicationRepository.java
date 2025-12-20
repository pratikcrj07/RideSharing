package com.ridesharing.Repository;

import com.ridesharing.Entities.DriverApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverApplicationRepository
        extends JpaRepository<DriverApplication, Long> {

    Optional<DriverApplication> findByUserId(Long userId);
}
