package com.ridesharing.Repository;

import com.ridesharing.Entities.DriverApplication;
import com.ridesharing.Entities.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverApplicationRepository
        extends JpaRepository<DriverApplication, Long> {

    Optional<DriverApplication> findByUserId(Long userId);

    //  REQUIRED for admin pending list
    List<DriverApplication> findByStatus(DriverStatus status);
}
