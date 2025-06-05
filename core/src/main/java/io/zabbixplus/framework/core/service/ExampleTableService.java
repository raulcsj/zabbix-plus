package io.zabbixplus.framework.core.service;

import io.ebean.DB;
import io.zabbixplus.framework.core.entity.ExampleEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExampleTableService {

    // DSLContext and its @Autowired constructor are removed

    @Transactional
    public ExampleEntity createRecord(String name) {
        ExampleEntity newEntity = new ExampleEntity();
        newEntity.setName(name);
        // createdAt will be set automatically by @WhenCreated
        DB.save(newEntity);
        return newEntity;
    }

    @Transactional(readOnly = true)
    public List<ExampleEntity> getRecords() {
        return DB.find(ExampleEntity.class).findList();
    }

    @Transactional(readOnly = true)
    public ExampleEntity getRecordById(Long id) {
        return DB.find(ExampleEntity.class, id);
    }

    @Transactional
    public ExampleEntity updateRecord(Long id, String newName) {
        ExampleEntity existingEntity = DB.find(ExampleEntity.class, id);
        if (existingEntity != null) {
            existingEntity.setName(newName);
            DB.update(existingEntity); // or DB.save(existingEntity);
            return existingEntity;
        }
        return null; // Or throw an exception e.g., ResourceNotFoundException
    }

    @Transactional
    public boolean deleteRecord(Long id) {
        // DB.delete(ExampleEntity.class, id) returns the number of rows deleted.
        // For a simple boolean, we can check if the entity exists first, then delete,
        // or rely on the returned count from a direct delete.
        // Direct delete is often more efficient if just checking for > 0.
        int rowsDeleted = DB.delete(ExampleEntity.class, id);
        return rowsDeleted > 0;
        // Alternative:
        // ExampleEntity entityToDelete = DB.find(ExampleEntity.class, id);
        // if (entityToDelete != null) {
        //     DB.delete(entityToDelete);
        //     return true;
        // }
        // return false;
    }
}
