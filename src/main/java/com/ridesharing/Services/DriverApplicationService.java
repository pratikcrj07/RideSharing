package com.ridesharing.Services;

import com.ridesharing.Entities.*;
import com.ridesharing.Exception.ApiException;
import com.ridesharing.Repository.DriverApplicationRepository;
import com.ridesharing.Repository.DriverRepository;
import com.ridesharing.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
@Service
@RequiredArgsConstructor
public class DriverApplicationService {

    private final UserRepository userRepository;
    private final DriverApplicationRepository applicationRepo;
    private final DriverRepository driverRepository;

    // USER APPLIES
    @Transactional
    public String apply(Long userId, DriverApplication req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        if (user.getDriverStatus() != DriverStatus.NOT_APPLIED) {
            throw new ApiException("Already applied or already a driver");
        }

        DriverApplication application = DriverApplication.builder()
                .userId(userId)
                .licenseNumber(req.getLicenseNumber())
                .vehicleNumber(req.getVehicleNumber())
                .vehicleModel(req.getVehicleModel())
                .status(DriverStatus.PENDING)
                .appliedAt(Instant.now())
                .build();

        user.setDriverStatus(DriverStatus.PENDING);

        applicationRepo.save(application);
        userRepository.save(user);

        System.out.println("Event: DRIVER_APPLIED - " + userId);
        return "Driver application submitted successfully";
    }

    // ADMIN: GET PENDING
    @Transactional(readOnly = true)
    public List<DriverApplication> getPendingApplications() {
        return applicationRepo.findByStatus(DriverStatus.PENDING);
    }

    // ADMIN APPROVE
    @Transactional
    public String approve(Long applicationId, Long adminId) {
        DriverApplication app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new ApiException("Application not found"));

        User user = userRepository.findById(app.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        app.setStatus(DriverStatus.APPROVED);
        app.setReviewedByAdminId(adminId);
        app.setReviewedAt(Instant.now());

        user.setRole(Role.ROLE_DRIVER);
        user.setDriverStatus(DriverStatus.ACTIVE);

        driverRepository.findByUserId(user.getId()).orElseGet(() -> {
            Driver driver = Driver.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .vehicleNumber(app.getVehicleNumber())
                    .vehicleModel(app.getVehicleModel())
                    .approved(true)
                    .online(false)
                    .build();
            return driverRepository.save(driver);
        });

        applicationRepo.save(app);
        userRepository.save(user);

        System.out.println("Event: DRIVER_APPROVED - " + user.getId());
        return "Driver approved successfully";
    }

    // ADMIN REJECT
    @Transactional
    public String reject(Long applicationId, String reason, Long adminId) {
        DriverApplication app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new ApiException("Application not found"));

        User user = userRepository.findById(app.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        app.setStatus(DriverStatus.REJECTED);
        app.setRejectionReason(reason);
        app.setReviewedByAdminId(adminId);
        app.setReviewedAt(Instant.now());

        user.setDriverStatus(DriverStatus.REJECTED);

        applicationRepo.save(app);
        userRepository.save(user);

        System.out.println("Event: DRIVER_REJECTED - " + user.getId());
        return "Driver application rejected";
    }
}
