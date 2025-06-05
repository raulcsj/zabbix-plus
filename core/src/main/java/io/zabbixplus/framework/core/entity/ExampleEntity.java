package io.zabbixplus.framework.core.entity;

import io.ebean.annotation.WhenCreated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp; // Standard JDBC Timestamp

@Entity
@Table(name = "example_table") // Ensure this matches your actual table name
public class ExampleEntity {

    @Id
    private Long id;

    private String name;

    @WhenCreated // Ebean annotation to automatically set timestamp on creation
    private Timestamp createdAt;

    // Standard getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
