package io.zabbixplus.framework.exampleplugin.controller;

import io.zabbixplus.framework.core.entity.ExampleEntity;
import io.zabbixplus.framework.core.service.ExampleTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plugins/simpleexampleplugin") // Standardized base path
public class ExamplePluginApiController {

    private static final Logger logger = LoggerFactory.getLogger(ExamplePluginApiController.class);

    private final ExampleTableService exampleTableService;

    @Autowired
    public ExamplePluginApiController(ExampleTableService exampleTableService) {
        this.exampleTableService = exampleTableService;
    }

    // Helper method to map ExampleEntity to a Map
    private Map<String, Object> mapEntityToMap(ExampleEntity entity) {
        Map<String, Object> map = new HashMap<>();
        if (entity == null) {
            return null; // Or an empty map, depending on desired behavior
        }
        map.put("id", entity.getId());
        map.put("name", entity.getName());
        map.put("createdAt", entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant().toString() : null);
        return map;
    }

    @GetMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> getData() {
        try {
            List<ExampleEntity> records = exampleTableService.getRecords();
            List<Map<String, Object>> result = records.stream()
                .map(this::mapEntityToMap)
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching records in ExamplePluginApiController", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> createData(@RequestBody Map<String, String> payload) {
        if (payload == null || payload.get("name") == null || payload.get("name").trim().isEmpty()) {
            logger.warn("Create data request with missing or empty name.");
            return ResponseEntity.badRequest().body(Map.of("error", "Name field is required."));
        }
        try {
            String name = payload.get("name");
            ExampleEntity newRecord = exampleTableService.createRecord(name);
            if (newRecord == null) {
                logger.error("Failed to create record, service returned null.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(mapEntityToMap(newRecord));
        } catch (Exception e) {
            logger.error("Error creating record in ExamplePluginApiController", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
