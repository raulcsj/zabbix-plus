package com.csj.framework.core.service;

import org.jooq.DSLContext;
// Import generated classes once available, e.g.
// import static com.csj.framework.database.generated.tables.ExampleTable.EXAMPLE_TABLE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ExampleTableService {

    @Autowired
    private DSLContext dsl; // Relying on Spring Boot auto-configuration for DSLContext

    // This method will use plain SQL string with jOOQ until code generation is run
    @Transactional
    public void createRecord(String name) {
        // Until generated code is available:
        dsl.insertInto(org.jooq.impl.DSL.table("example_table"), org.jooq.impl.DSL.field("name"))
           .values(name)
           .execute();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecords() {
        // Until generated code is available:
        return dsl.select(org.jooq.impl.DSL.field("id"), org.jooq.impl.DSL.field("name"), org.jooq.impl.DSL.field("created_at"))
                  .from(org.jooq.impl.DSL.table("example_table"))
                  .fetchMaps(); // Fetches as List<Map<String, Object>>
    }

    // Example of how it would look with generated code:
    /*
    @Transactional
    public void createRecordWithGeneratedCode(String name) {
        dsl.insertInto(EXAMPLE_TABLE, EXAMPLE_TABLE.NAME)
           .values(name)
           .execute();
    }

    @Transactional(readOnly = true)
    public List<com.csj.framework.database.generated.tables.pojos.ExampleTable> getRecordsWithGeneratedCode() {
        return dsl.selectFrom(EXAMPLE_TABLE)
                  .fetchInto(com.csj.framework.database.generated.tables.pojos.ExampleTable.class);
    }
    */
}
