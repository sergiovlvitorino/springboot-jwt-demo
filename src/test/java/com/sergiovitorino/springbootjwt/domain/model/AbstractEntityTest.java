package com.sergiovitorino.springbootjwt.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AbstractEntityTest {

    static class TestEntity extends AbstractEntity {
    }

    @Test
    void gettersSettersAndToString() {
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        UUID disabledBy = UUID.randomUUID();

        LocalDateTime createdAt = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, Month.FEBRUARY, 1, 11, 0);
        LocalDateTime disabledAt = LocalDateTime.of(2024, Month.MARCH, 1, 12, 0);

        TestEntity e = new TestEntity();
        e.setUserIdCreatedAt(createdBy);
        e.setUserIdUpdatedAt(updatedBy);
        e.setUserIdDisabledAt(disabledBy);
        e.setDateCreatedAt(createdAt);
        e.setDateUpdatedAt(updatedAt);
        e.setDateDisabledAt(disabledAt);

        assertEquals(createdBy, e.getUserIdCreatedAt());
        assertEquals(updatedBy, e.getUserIdUpdatedAt());
        assertEquals(disabledBy, e.getUserIdDisabledAt());
        assertEquals(createdAt, e.getDateCreatedAt());
        assertEquals(updatedAt, e.getDateUpdatedAt());
        assertEquals(disabledAt, e.getDateDisabledAt());

        String s = e.toString();
        assertTrue(s.contains("AbstractEntity{"));
        assertTrue(s.contains("userIdCreatedAt="));
    }

    @Test
    void onPrePersist_setsDateCreatedAt() {
        TestEntity e = new TestEntity();
        assertNull(e.getDateCreatedAt());
        e.onPrePersist();
        assertNotNull(e.getDateCreatedAt());
    }
}
