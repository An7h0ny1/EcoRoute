package hackathon.EcoRoute.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> checkHealth() {
        return Map.of(
                "status", "UP",
                "message", "EcoRoute API is running on Houston Server",
                "timestamp", new Date()
        );
    }
}
