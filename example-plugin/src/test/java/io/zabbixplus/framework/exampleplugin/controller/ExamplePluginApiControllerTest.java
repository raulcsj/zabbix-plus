package io.zabbixplus.framework.exampleplugin.controller;

import io.zabbixplus.framework.core.entity.ExampleEntity;
import io.zabbixplus.framework.core.service.ExampleTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamplePluginApiControllerTest {

    @Mock
    private ExampleTableService mockExampleTableService;

    @InjectMocks
    private ExamplePluginApiController controller;

    private ExampleEntity entity1;
    private ExampleEntity entity2;

    @BeforeEach
    void setUp() {
        entity1 = new ExampleEntity();
        entity1.setId(1L);
        entity1.setName("Data 1");
        entity1.setCreatedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(1)));

        entity2 = new ExampleEntity();
        entity2.setId(2L);
        entity2.setName("Data 2");
        entity2.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
    }

    // Test cases for GET /api/plugins/simpleexampleplugin/data
    @Test
    void testGetData_ReturnsEmptyList() {
        when(mockExampleTableService.getRecords()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Map<String, Object>>> response = controller.getData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetData_ReturnsListOfRecords() {
        List<ExampleEntity> entityList = Arrays.asList(entity1, entity2);
        when(mockExampleTableService.getRecords()).thenReturn(entityList);

        ResponseEntity<List<Map<String, Object>>> response = controller.getData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        Map<String, Object> recordMap1 = response.getBody().get(0);
        assertEquals(entity1.getId(), recordMap1.get("id"));
        assertEquals(entity1.getName(), recordMap1.get("name"));
        assertNotNull(recordMap1.get("createdAt"));
        assertEquals(entity1.getCreatedAt().toInstant().toString(), recordMap1.get("createdAt"));


        Map<String, Object> recordMap2 = response.getBody().get(1);
        assertEquals(entity2.getId(), recordMap2.get("id"));
        assertEquals(entity2.getName(), recordMap2.get("name"));
        assertEquals(entity2.getCreatedAt().toInstant().toString(), recordMap2.get("createdAt"));
    }

    @Test
    void testGetData_ServiceThrowsException() {
        when(mockExampleTableService.getRecords()).thenThrow(new RuntimeException("Service failure"));

        ResponseEntity<List<Map<String, Object>>> response = controller.getData();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    // Test cases for POST /api/plugins/simpleexampleplugin/data
    @Test
    void testCreateData_Successful() {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "New Record");

        ExampleEntity createdEntity = new ExampleEntity();
        createdEntity.setId(3L);
        createdEntity.setName("New Record");
        createdEntity.setCreatedAt(Timestamp.from(Instant.now()));

        when(mockExampleTableService.createRecord("New Record")).thenReturn(createdEntity);

        ResponseEntity<Map<String, Object>> response = controller.createData(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdEntity.getId(), response.getBody().get("id"));
        assertEquals(createdEntity.getName(), response.getBody().get("name"));
        assertEquals(createdEntity.getCreatedAt().toInstant().toString(), response.getBody().get("createdAt"));
    }

    @Test
    void testCreateData_InvalidInput_MissingName() {
        Map<String, String> payload = new HashMap<>(); // Missing "name"

        ResponseEntity<Map<String, Object>> response = controller.createData(payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Name field is required.", response.getBody().get("error"));
    }

    @Test
    void testCreateData_InvalidInput_NullPayload() {
        ResponseEntity<Map<String, Object>> response = controller.createData(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }


    @Test
    void testCreateData_ServiceThrowsException() {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "Faulty Record");

        when(mockExampleTableService.createRecord("Faulty Record")).thenThrow(new RuntimeException("Service failure during create"));

        ResponseEntity<Map<String, Object>> response = controller.createData(payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreateData_ServiceReturnsNull() {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "Null Record");

        when(mockExampleTableService.createRecord("Null Record")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.createData(payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Controller should indicate an error if service returns null for a new record.");
        assertNull(response.getBody());
    }
}
