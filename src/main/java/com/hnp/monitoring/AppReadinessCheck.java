package com.hnp.monitoring;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class AppReadinessCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        boolean dbReady = false;
        return dbReady ? HealthCheckResponse.up("Ready"):  HealthCheckResponse.down("Database is Down");
    }
}
