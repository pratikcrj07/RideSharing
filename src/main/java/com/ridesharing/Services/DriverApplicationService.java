package com.ridesharing.Services;

import com.ridesharing.Entities.*;
import com.ridesharing.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverApplicationService {

    private final UserRepository userRepository;
    private final DriverApplicationRepo applicationRepository;

    public String apply(Long userId, DriverApplication req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDriverStatus() != DriverStatus.NOT_APPLIED) {
            throw new RuntimeException("You have already applied or are already a driver");
        }

        req.setUserId(userId);
        req.setStatus(DriverStatus.PENDING);
        applicationRepository.save(req);

        user.setDriverStatus(DriverStatus.PENDING);
        userRepository.save(user);

        return "Driver application submitted successfully";
    }

    public String approve(Long applicationId) {

        DriverApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User user = userRepository.findById(app.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        app.setStatus(DriverStatus.APPROVED);
        applicationRepository.save(app);

        user.setDriverStatus(DriverStatus.APPROVED);
        userRepository.save(user);

        return "Driver Approved Successfully";
    }

    public String reject(Long applicationId) {

        DriverApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User user = userRepository.findById(app.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        app.setStatus(DriverStatus.REJECTED);
        applicationRepository.save(app);

        user.setDriverStatus(DriverStatus.REJECTED);
        userRepository.save(user);

        return "Driver Application Rejected";
    }
}
