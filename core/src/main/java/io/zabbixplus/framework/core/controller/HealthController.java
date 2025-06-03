package io.zabbixplus.framework.core.controller;

import io.zabbixplus.framework.core.exception.ResourceNotFoundException; // Updated
import io.zabbixplus.framework.core.web.ApiResponse; // Updated
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import io.zabbixplus.framework.core.service.ExampleTableService; // Updated
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private ExampleTableService exampleTableService;

    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("Core Service is UP and Running!");
    }

    @GetMapping("/test/found/{id}")
    public ApiResponse<Map<String, String>> testFound(@PathVariable String id) {
        if ("1".equals(id)) {
             return ApiResponse.success("Found resource with ID " + id, Map.of("id", id, "status", "found"));
        }
        throw new ResourceNotFoundException("Resource with ID " + id + " not found.");
    }

    @PostMapping("/test/create")
    public ApiResponse<Map<String, String>> testCreate(@Valid @RequestBody Map<String, String> payload) {
        return ApiResponse.success("Resource created successfully", payload);
    }

    public static class TestRequestBody {
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
        throw new RuntimeException("This is a test runtime exception for ApiResponse!");
    }

    @PostMapping("/records")
    public ApiResponse<String> addRecord(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null || name.trim().isEmpty()) {
             return new ApiResponse<>(400, "Name is required", null);
        }
        exampleTableService.createRecord(name);
        return ApiResponse.success("Record created successfully");
    }

    @GetMapping("/records")
    public ApiResponse<List<Map<String, Object>>> getAllRecords() {
        return ApiResponse.success(exampleTableService.getRecords());
    }
}
