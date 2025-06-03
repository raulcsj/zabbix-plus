package com.csj.framework.core.controller;

import com.csj.framework.core.exception.ResourceNotFoundException;
import com.csj.framework.core.web.ApiResponse; // Import ApiResponse
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import com.csj.framework.core.service.ExampleTableService; // Import ExampleTableService
import org.springframework.beans.factory.annotation.Autowired; // For Autowired
import java.util.List; // For List in response
import java.util.Map;

@RestController
@RequestMapping("/api") // Common base path for APIs
public class HealthController {

    @Autowired
    private ExampleTableService exampleTableService;

    // Using ApiResponse directly as the return type
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("Core Service is UP and Running!");
    }

    @GetMapping("/test/found/{id}")
    public ApiResponse<Map<String, String>> testFound(@PathVariable String id) {
        if ("1".equals(id)) {
             return ApiResponse.success("Found resource with ID " + id, Map.of("id", id, "status", "found"));
        }
        // This case will be handled by GlobalExceptionHandler
        throw new ResourceNotFoundException("Resource with ID " + id + " not found.");
    }

    // Example of an endpoint that creates a resource and returns data
    @PostMapping("/test/create")
    public ApiResponse<Map<String, String>> testCreate(@Valid @RequestBody Map<String, String> payload) {
        // Process payload...
        // For simplicity, just returning the payload.
        // Ensure the map has a 'name' key if you want to use it with TestRequestBody validation logic,
        // or adjust validation. For this generic Map, @Valid won't do much unless specific constraints
        // are on Map entries, which is more complex. Let's assume basic Map echo for now.
        return ApiResponse.success("Resource created successfully", payload);
    }

    // For testing validation (already added in previous step, ensure it uses ApiResponse if not throwing validation error directly)
    // The MethodArgumentNotValidException will be caught by GlobalExceptionHandler, which is fine.
    public static class TestRequestBody { // Inner class for request body
        @NotBlank(message = "Name cannot be blank")
        private String name;

        @jakarta.validation.constraints.Email(message = "Email should be valid")
        private String email;

        @jakarta.validation.constraints.NotNull(message = "Age cannot be null")
        @jakarta.validation.constraints.Min(value = 18, message = "Age must be at least 18")
        private Integer age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    @PostMapping("/test/validation")
    public ApiResponse<TestRequestBody> testValidation(@Valid @RequestBody TestRequestBody data) {
        return ApiResponse.success("Data is valid", data);
    }

    @GetMapping("/test/error")
    public ApiResponse<Void> testError() {
        // This will be caught by GlobalExceptionHandler and wrapped in ErrorResponse, not ApiResponse
        throw new RuntimeException("This is a test runtime exception for ApiResponse!");
    }

    // Endpoints for ExampleTableService
    @PostMapping("/records")
    public ApiResponse<String> addRecord(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null || name.trim().isEmpty()) {
            // Consider using @Valid on a DTO for this instead of manual check
            // For now, simple check and direct error response (though not using ErrorResponse DTO here)
            // Better to throw an exception that GlobalExceptionHandler can format.
            // throw new IllegalArgumentException("Name is required for creating a record.");
             return new ApiResponse<>(400, "Name is required", null); // Quick non-standard error
        }
        exampleTableService.createRecord(name);
        return ApiResponse.success("Record created successfully");
    }

    @GetMapping("/records")
    public ApiResponse<List<Map<String, Object>>> getAllRecords() {
        return ApiResponse.success(exampleTableService.getRecords());
    }
}
