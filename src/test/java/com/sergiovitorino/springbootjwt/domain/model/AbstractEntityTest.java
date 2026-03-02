package com.sergiovitorino.springbootjwt.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AbstractEntityTest {

    static class TestEntity extends AbstractEntity {
        // concrete type for testing AbstractEntity behavior
    }

    @Test
    void equalsHashCodeToStringAndGettersSetters() {
        UUID createdBy = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        UUID disabledBy = UUID.randomUUID();

        LocalDateTime createdAt = LocalDateTime.of(2024, Month.JANUARY, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, Month.FEBRUARY, 1, 11, 0);
        LocalDateTime disabledAt = LocalDateTime.of(2024, Month.MARCH, 1, 12, 0);

        TestEntity e1 = new TestEntity();
        e1.setUserIdCreatedAt(createdBy);
        e1.setUserIdUpdatedAt(updatedBy);
        e1.setUserIdDisabledAt(disabledBy);
        e1.setDateCreatedAt(createdAt);
        e1.setDateUpdatedAt(updatedAt);
        e1.setDateDisabledAt(disabledAt);

        TestEntity e2 = new TestEntity();
        e2.setUserIdCreatedAt(createdBy);
        e2.setUserIdUpdatedAt(updatedBy);
        e2.setUserIdDisabledAt(disabledBy);
        e2.setDateCreatedAt(createdAt);
        e2.setDateUpdatedAt(updatedAt);
        e2.setDateDisabledAt(disabledAt);

        assertEquals(createdBy, e1.getUserIdCreatedAt());
        assertEquals(updatedBy, e1.getUserIdUpdatedAt());
        assertEquals(disabledBy, e1.getUserIdDisabledAt());
        assertEquals(createdAt, e1.getDateCreatedAt());
        assertEquals(updatedAt, e1.getDateUpdatedAt());
        assertEquals(disabledAt, e1.getDateDisabledAt());

        assertEquals(e1, e1);
        assertNotEquals(e1, null);
        assertNotEquals(e1, "x");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());

        e2.setUserIdUpdatedAt(UUID.randomUUID());
        assertNotEquals(e1, e2);

        String s = e1.toString();
        assertTrue(s.contains("AbstractEntity{"));
        assertTrue(s.contains("userIdCreatedAt="));
    }
}
