package com.polidea.shuttle.infrastructure.database;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
abstract public class BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    // We use `Integer` here, so the default value for new entity is `null` which will allow...
    // ... Hibernate to assign new ID for it. Using `int` will result with `0` ID by default.
    protected Integer id;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BaseEntity)) {
            return false;
        }
        BaseEntity that = (BaseEntity) other;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id;
    }

}
