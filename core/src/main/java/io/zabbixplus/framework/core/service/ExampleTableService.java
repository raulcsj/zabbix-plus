package io.zabbixplus.framework.core.service; // Updated package

import org.jooq.DSLContext;
// Import generated classes once available, e.g.
// import static io.zabbixplus.framework.database.generated.tables.ExampleTable.EXAMPLE_TABLE; // Updated comment
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ExampleTableService {

    @Autowired
    private DSLContext dsl;

    @Transactional
    public void createRecord(String name) {
        dsl.insertInto(org.jooq.impl.DSL.table("example_table"), org.jooq.impl.DSL.field("name"))
           .values(name)
           .execute();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecords() {
        return dsl.select(org.jooq.impl.DSL.field("id"), org.jooq.impl.DSL.field("name"), org.jooq.impl.DSL.field("created_at"))
                  .from(org.jooq.impl.DSL.table("example_table"))
                  .fetchMaps();
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
    public List<io.zabbixplus.framework.database.generated.tables.pojos.ExampleTable> getRecordsWithGeneratedCode() { // Updated comment
        return dsl.selectFrom(EXAMPLE_TABLE)
                  .fetchInto(io.zabbixplus.framework.database.generated.tables.pojos.ExampleTable.class); // Updated comment
    }
    */
}
