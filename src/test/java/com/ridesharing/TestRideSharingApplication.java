package com.ridesharing;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestRideSharingApplication {

    public static void main(String[] args) {
        SpringApplication.from(RideSharingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
