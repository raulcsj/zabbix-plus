package io.zabbixplus.framework.exampleplugin.controller;

import io.zabbixplus.framework.core.service.ExampleTableService;
import io.zabbixplus.framework.database.tables.pojos.ExampleTable;
import io.zabbixplus.framework.database.tables.records.ExampleTableRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamplePluginApiControllerTest {

    @Mock
    private ExampleTableService mockExampleTableService;

    @InjectMocks
    private ExamplePluginApiController controller;

    private ExampleTableRecord record1;
    private ExampleTableRecord record2;

    @BeforeEach
    void setUp() {
        // Initialize some common test data
        record1 = new ExampleTableRecord(1L, "Data 1", LocalDateTime.now().minusDays(1));
        record2 = new ExampleTableRecord(2L, "Data 2", LocalDateTime.now());
    }

    // Test cases for GET /api/plugins/simpleexampleplugin/data
    @Test
    void testGetData_ReturnsEmptyList() {
        when(mockExampleTableService.getRecords()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ExampleTable>> response = controller.getData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetData_ReturnsListOfRecords() {
        // Using ExampleTable (POJO) as that's what the controller method is declared to return in the list
        List<ExampleTable> pojoList = Arrays.asList(
            new ExampleTable(1L, "Data 1", LocalDateTime.now().minusDays(1)),
            new ExampleTable(2L, "Data 2", LocalDateTime.now())
        );
        when(mockExampleTableService.getRecords()).thenReturn(pojoList);

        ResponseEntity<List<ExampleTable>> response = controller.getData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Data 1", response.getBody().get(0).getData());
        assertEquals("Data 2", response.getBody().get(1).getData());
    }

    @Test
    void testGetData_ServiceThrowsException() {
        when(mockExampleTableService.getRecords()).thenThrow(new RuntimeException("Service failure"));

        ResponseEntity<List<ExampleTable>> response = controller.getData();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody()); // Or expect a specific error object if the controller has @ExceptionHandler
    }

    // Test cases for POST /api/plugins/simpleexampleplugin/data
    @Test
    void testCreateData_Successful() {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "New Record");

        ExampleTableRecord createdRecord = new ExampleTableRecord(3L, "New Record", LocalDateTime.now());
        when(mockExampleTableService.createRecord("New Record")).thenReturn(createdRecord);

        // Convert record to POJO for response comparison
        ExampleTable createdPojo = new ExampleTable(createdRecord.getId(), createdRecord.getData(), createdRecord.getCreatedAt());
        when(mockExampleTableService.convertToPojo(createdRecord)).thenReturn(createdPojo);


        ResponseEntity<ExampleTable> response = controller.createData(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Record", response.getBody().getData());
        assertEquals(3L, response.getBody().getId());
    }

    @Test
    void testCreateData_InvalidInput_MissingName() {
        Map<String, String> payload = new HashMap<>(); // Missing "name"
        // No mock setup for service needed as it shouldn't be called

        ResponseEntity<ExampleTable> response = controller.createData(payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody()); // Or an error message if controller returns one
    }

    @Test
    void testCreateData_InvalidInput_NullPayload() {
        ResponseEntity<ExampleTable> response = controller.createData(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }


    @Test
    void testCreateData_ServiceThrowsException() {
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "Faulty Record");

        when(mockExampleTableService.createRecord("Faulty Record")).thenThrow(new RuntimeException("Service failure during create"));

        ResponseEntity<ExampleTable> response = controller.createData(payload);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody()); // Or expect a specific error object
    }

    @Test
    void testCreateData_ServiceReturnsNull() {
        // Scenario where the service method runs but returns null (e.g., internal issue not resulting in exception)
        Map<String, String> payload = new HashMap<>();
        payload.put("name", "Null Record");

        when(mockExampleTableService.createRecord("Null Record")).thenReturn(null);

        ResponseEntity<ExampleTable> response = controller.createData(payload);

        // Depending on how ExamplePluginApiController.createData handles a null record from service:
        // If it considers null as a server-side issue (couldn't create, but no exception thrown by service):
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Controller should indicate an error if service returns null for a new record.");
        assertNull(response.getBody());
    }
}
