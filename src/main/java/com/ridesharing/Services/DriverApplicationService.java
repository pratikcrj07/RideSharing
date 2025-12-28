package com.ridesharing.Services;

import com.ridesharing.Entities.*;
import com.ridesharing.Exception.ApiException;
import com.ridesharing.Repository.DriverApplicationRepository;
import com.ridesharing.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverApplicationService {

    private final UserRepository userRepository;
    private final DriverApplicationRepository applicationRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // ================= USER APPLIES =================
    @Transactional
    public String apply(Long userId, DriverApplication req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        if (user.getDriverStatus() != DriverstatusService.NOT_APPLIED) {
            throw new ApiException("Already applied or already a driver");
        }

        DriverApplication application = DriverApplication.builder()
                .userId(userId)
                .licenseNumber(req.getLicenseNumber())
                .vehicleNumber(req.getVehicleNumber())
                .vehicleModel(req.getVehicleModel())
                .status(DriverstatusService.PENDING)
                .appliedAt(Instant.now())
                .build();

        user.setDriverStatus(DriverstatusService.PENDING);

        applicationRepo.save(application);
        userRepository.save(user);

        kafkaTemplate.send("auth-events", "DRIVER_APPLIED:" + userId);
        return "Driver application submitted successfully";
    }

    // ================= ADMIN: GET ALL PENDING =================
    @Transactional(readOnly = true)
    public List<DriverApplication> getPendingApplications() {
        return applicationRepo.findByStatus(DriverstatusService.PENDING);
    }

    // ================= ADMIN APPROVES =================
    @Transactional
    public String approve(Long applicationId, Long adminId) {

        DriverApplication app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new ApiException("Application not found"));

        User user = userRepository.findById(app.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        app.setStatus(DriverstatusService.APPROVED);
        app.setReviewedByAdminId(adminId);
        app.setReviewedAt(Instant.now());

        user.setRole(Role.ROLE_DRIVER);
        user.setDriverStatus(DriverstatusService.APPROVED);

        applicationRepo.save(app);
        userRepository.save(user);

        kafkaTemplate.send("auth-events", "DRIVER_APPROVED:" + user.getId());
        return "Driver approved successfully";
    }

    // ================= ADMIN REJECTS =================
    @Transactional
    public String reject(Long applicationId, String reason, Long adminId) {

        DriverApplication app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new ApiException("Application not found"));

        User user = userRepository.findById(app.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        app.setStatus(DriverstatusService.REJECTED);
        app.setRejectionReason(reason);
        app.setReviewedByAdminId(adminId);
        app.setReviewedAt(Instant.now());

        user.setDriverStatus(DriverstatusService.REJECTED);

        applicationRepo.save(app);
        userRepository.save(user);

        kafkaTemplate.send("auth-events", "DRIVER_REJECTED:" + user.getId());
        return "Driver application rejected";
    }
}
