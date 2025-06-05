package io.zabbixplus.framework.core.service;

import io.zabbixplus.framework.core.CoreApplication;
import io.zabbixplus.framework.database.tables.records.ExampleTableRecord;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional; // Optional: if tests modify data and need rollback

import java.time.LocalDateTime;
import java.util.List;

import static io.zabbixplus.framework.database.tables.ExampleTable.EXAMPLE_TABLE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CoreApplication.class) // Specify your main application class
@ActiveProfiles("test")
@Transactional // Rollback transactions after each test
class ExampleTableServiceTest {

    @Autowired
    private ExampleTableService exampleTableService;

    @Autowired
    private DSLContext dsl; // For direct DB interaction/verification if needed

    @BeforeEach
    void setUp() {
        // Clean up the table before each test if not relying solely on @Transactional
        dsl.deleteFrom(EXAMPLE_TABLE).execute();
    }

    @Test
    void testCreateRecord() {
        String testData = "Test Data Create";
        ExampleTableRecord createdRecord = exampleTableService.createRecord(testData);

        assertNotNull(createdRecord);
        assertNotNull(createdRecord.getId());
        assertEquals(testData, createdRecord.getData());
        assertNotNull(createdRecord.getCreatedAt());

        // Verify in DB
        ExampleTableRecord dbRecord = dsl.selectFrom(EXAMPLE_TABLE)
                                         .where(EXAMPLE_TABLE.ID.eq(createdRecord.getId()))
                                         .fetchOne();
        assertNotNull(dbRecord);
        assertEquals(testData, dbRecord.getData());
    }

    @Test
    void testGetRecordById() {
        String testData = "Test Data Get By Id";
        ExampleTableRecord newRecord = exampleTableService.createRecord(testData);

        ExampleTableRecord foundRecord = exampleTableService.getRecordById(newRecord.getId());

        assertNotNull(foundRecord);
        assertEquals(newRecord.getId(), foundRecord.getId());
        assertEquals(testData, foundRecord.getData());
    }

    @Test
    void testGetRecordById_NotFound() {
        ExampleTableRecord foundRecord = exampleTableService.getRecordById(-1L); // Non-existent ID
        assertNull(foundRecord);
    }

    @Test
    void testGetAllRecords() {
        exampleTableService.createRecord("Record 1");
        exampleTableService.createRecord("Record 2");

        List<ExampleTableRecord> records = exampleTableService.getAllRecords();

        assertNotNull(records);
        assertEquals(2, records.size());
    }

    @Test
    void testGetAllRecords_Empty() {
        List<ExampleTableRecord> records = exampleTableService.getAllRecords();
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    void testUpdateRecord() {
        ExampleTableRecord createdRecord = exampleTableService.createRecord("Initial Data");
        String updatedData = "Updated Data";

        ExampleTableRecord updatedRecord = exampleTableService.updateRecord(createdRecord.getId(), updatedData);

        assertNotNull(updatedRecord);
        assertEquals(createdRecord.getId(), updatedRecord.getId());
        assertEquals(updatedData, updatedRecord.getData());

        // Verify in DB
        ExampleTableRecord dbRecord = dsl.selectFrom(EXAMPLE_TABLE)
                                         .where(EXAMPLE_TABLE.ID.eq(createdRecord.getId()))
                                         .fetchOne();
        assertNotNull(dbRecord);
        assertEquals(updatedData, dbRecord.getData());
    }

    @Test
    void testUpdateRecord_NotFound() {
        ExampleTableRecord updatedRecord = exampleTableService.updateRecord(-1L, "Data for non-existent record");
        assertNull(updatedRecord, "Updating a non-existent record should return null or throw an exception, depending on service implementation.");
        // If the service is designed to throw an exception, use assertThrows
        // assertThrows(SomeSpecificException.class, () -> exampleTableService.updateRecord(-1L, "Data"));
    }


    @Test
    void testDeleteRecord() {
        ExampleTableRecord createdRecord = exampleTableService.createRecord("Data to be Deleted");
        Long recordId = createdRecord.getId();

        boolean deleted = exampleTableService.deleteRecord(recordId);
        assertTrue(deleted);

        // Verify in DB
        ExampleTableRecord dbRecord = dsl.selectFrom(EXAMPLE_TABLE)
                                         .where(EXAMPLE_TABLE.ID.eq(recordId))
                                         .fetchOne();
        assertNull(dbRecord);
    }

    @Test
    void testDeleteRecord_NotFound() {
        boolean deleted = exampleTableService.deleteRecord(-1L); // Non-existent ID
        assertFalse(deleted);
    }

    @Test
    void testCreatedAtTimestamp() {
        LocalDateTime beforeCreation = LocalDateTime.now();
        ExampleTableRecord createdRecord = exampleTableService.createRecord("Timestamp Test");
        LocalDateTime afterCreation = LocalDateTime.now();

        assertNotNull(createdRecord.getCreatedAt());
        // Check if created_at is within a reasonable range (e.g., not in the future beyond 'now', not too far in the past)
        // Allow for slight clock differences if 'beforeCreation' and 'afterCreation' are very close to actual creation time.
        assertTrue(createdRecord.getCreatedAt().isEqual(beforeCreation) || createdRecord.getCreatedAt().isAfter(beforeCreation));
        assertTrue(createdRecord.getCreatedAt().isEqual(afterCreation) || createdRecord.getCreatedAt().isBefore(afterCreation));

        // A more robust check might involve fetching the DB server's timestamp if possible,
        // but for H2 in-mem, client and server time are the same.
        // We primarily check that it's set.
    }
}
