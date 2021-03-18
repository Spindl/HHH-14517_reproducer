package org.hibernate.bugs;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "TestEntity")
@Table(name = "testEntity")
public class TestEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String stringProperty;

    public TestEntity() {
    }

    public TestEntity(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }
}
