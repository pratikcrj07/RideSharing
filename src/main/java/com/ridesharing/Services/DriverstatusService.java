package com.ridesharing.Services;

import com.ridesharing.Entities.Driver;
import com.ridesharing.Entities.User;
import com.ridesharing.Exception.ApiException;
import com.ridesharing.Repository.DriverRepository;
import com.ridesharing.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverstatusService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    // ================= SUSPEND DRIVER =================
    @Transactional
    public String suspendDriver(Long driverId) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ApiException("Driver not found"));

        User user = userRepository.findById(driver.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        // business rule
        if (user.getDriverStatus() != DriverstatusService.APPROVED) {
            throw new ApiException("Only approved drivers can be suspended");
        }

        user.setDriverStatus(DriverstatusService.SUSPENDED);
        driver.setOnline(false); // force offline

        userRepository.save(user);
        driverRepository.save(driver);

        return "Driver suspended successfully";
    }

    // ================= ACTIVATE DRIVER =================
    @Transactional
    public String activateDriver(Long driverId) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ApiException("Driver not found"));

        User user = userRepository.findById(driver.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        if (user.getDriverStatus() != DriverstatusService.SUSPENDED) {
            throw new ApiException("Driver is not suspended");
        }

        user.setDriverStatus(DriverstatusService.APPROVED);

        userRepository.save(user);

        return "Driver activated successfully";
    }
}
