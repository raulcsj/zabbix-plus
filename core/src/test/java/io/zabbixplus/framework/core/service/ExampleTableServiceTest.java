package io.zabbixplus.framework.core.service;

import io.ebean.DB;
import io.zabbixplus.framework.core.CoreApplication;
import io.zabbixplus.framework.core.entity.ExampleEntity; // Ebean entity
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp; // For comparing createdAt
import java.time.Instant; // For creating Timestamp
import java.time.temporal.ChronoUnit; // For comparing timestamps
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CoreApplication.class)
@ActiveProfiles("test")
@Transactional // Rollback transactions after each test
class ExampleTableServiceTest {

    @Autowired
    private ExampleTableService exampleTableService;

    // DSLContext removed

    @BeforeEach
    void setUp() {
        // Clean up the table before each test using Ebean
        // DB.deleteAll(ExampleEntity.class) is also an option
        DB.find(ExampleEntity.class).delete();
    }

    @Test
    void testCreateRecord() {
        String testData = "Test Data Create";
        ExampleEntity createdEntity = exampleTableService.createRecord(testData);

        assertNotNull(createdEntity);
        assertNotNull(createdEntity.getId(), "ID should be generated after save.");
        assertEquals(testData, createdEntity.getName());
        assertNotNull(createdEntity.getCreatedAt(), "CreatedAt should be set by @WhenCreated.");

        // Verify in DB using Ebean
        ExampleEntity dbEntity = DB.find(ExampleEntity.class, createdEntity.getId());
        assertNotNull(dbEntity, "Entity should be found in DB after creation.");
        assertEquals(testData, dbEntity.getName());
        assertNotNull(dbEntity.getCreatedAt());
        assertEquals(createdEntity.getCreatedAt().getTime(), dbEntity.getCreatedAt().getTime()); // Compare timestamp values
    }

    @Test
    void testGetRecordById() {
        String testData = "Test Data Get By Id";
        ExampleEntity newEntity = exampleTableService.createRecord(testData);
        assertNotNull(newEntity.getId(), "Created entity must have an ID for this test.");

        ExampleEntity foundEntity = exampleTableService.getRecordById(newEntity.getId());

        assertNotNull(foundEntity, "Entity should be found by ID.");
        assertEquals(newEntity.getId(), foundEntity.getId());
        assertEquals(testData, foundEntity.getName());
    }

    @Test
    void testGetRecordById_NotFound() {
        ExampleEntity foundEntity = exampleTableService.getRecordById(-1L); // Non-existent ID
        assertNull(foundEntity, "Should return null for a non-existent ID.");
    }

    @Test
    void testGetRecords() { // Renamed from testGetAllRecords to match service method
        exampleTableService.createRecord("Record 1");
        exampleTableService.createRecord("Record 2");

        List<ExampleEntity> entities = exampleTableService.getRecords();

        assertNotNull(entities);
        assertEquals(2, entities.size(), "Should retrieve two records.");
    }

    @Test
    void testGetRecords_Empty() { // Renamed from testGetAllRecords_Empty
        List<ExampleEntity> entities = exampleTableService.getRecords();
        assertNotNull(entities);
        assertTrue(entities.isEmpty(), "Should return an empty list when no records exist.");
    }

    @Test
    void testUpdateRecord() {
        ExampleEntity createdEntity = exampleTableService.createRecord("Initial Data");
        assertNotNull(createdEntity.getId(), "Created entity must have an ID.");
        String updatedData = "Updated Data";

        ExampleEntity updatedEntity = exampleTableService.updateRecord(createdEntity.getId(), updatedData);

        assertNotNull(updatedEntity, "Updated entity should not be null.");
        assertEquals(createdEntity.getId(), updatedEntity.getId());
        assertEquals(updatedData, updatedEntity.getName(), "Name should be updated.");

        // Verify in DB
        ExampleEntity dbEntity = DB.find(ExampleEntity.class, createdEntity.getId());
        assertNotNull(dbEntity, "Entity should still exist in DB.");
        assertEquals(updatedData, dbEntity.getName(), "Name should be updated in DB.");
    }

    @Test
    void testUpdateRecord_NotFound() {
        ExampleEntity updatedEntity = exampleTableService.updateRecord(-1L, "Data for non-existent record");
        assertNull(updatedEntity, "Updating a non-existent record should return null.");
    }

    @Test
    void testDeleteRecord() {
        ExampleEntity createdEntity = exampleTableService.createRecord("Data to be Deleted");
        assertNotNull(createdEntity.getId(), "Created entity must have an ID.");
        Long recordId = createdEntity.getId();

        boolean deleted = exampleTableService.deleteRecord(recordId);
        assertTrue(deleted, "Delete method should return true for successful deletion.");

        // Verify in DB
        ExampleEntity dbEntity = DB.find(ExampleEntity.class, recordId);
        assertNull(dbEntity, "Entity should be deleted from DB.");
    }

    @Test
    void testDeleteRecord_NotFound() {
        boolean deleted = exampleTableService.deleteRecord(-1L); // Non-existent ID
        assertFalse(deleted, "Delete method should return false for non-existent ID.");
    }

    @Test
    void testCreatedAtTimestamp() {
        // Get current time before creation, allow for some precision differences with DB
        Instant beforeCreationInstant = Instant.now().minusSeconds(1); // a little buffer

        ExampleEntity createdEntity = exampleTableService.createRecord("Timestamp Test");
        assertNotNull(createdEntity.getCreatedAt(), "CreatedAt should be automatically set.");

        Instant createdAtInstant = createdEntity.getCreatedAt().toInstant();
        Instant afterCreationInstant = Instant.now().plusSeconds(1); // a little buffer

        assertTrue(createdAtInstant.isAfter(beforeCreationInstant) || createdAtInstant.equals(beforeCreationInstant),
                "CreatedAt should be after or equal to the time before creation.");
        assertTrue(createdAtInstant.isBefore(afterCreationInstant) || createdAtInstant.equals(afterCreationInstant),
                "CreatedAt should be before or equal to the time after creation (accounting for test execution time).");

        // A more direct check: createdAt should be very close to 'now' (within a few seconds)
        long diffMillis = Math.abs(Timestamp.from(Instant.now()).getTime() - createdEntity.getCreatedAt().getTime());
        assertTrue(diffMillis < 5000, "CreatedAt should be very recent (within 5 seconds).");
    }
}
