package com.sergiovitorino.springbootjwt.domain.model;

import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para validar o callback @PreUpdate em AbstractEntity.
 * Verifica que dateUpdatedAt é preenchido automaticamente pelo JPA ao atualizar
 * uma entidade, sem necessidade de chamada manual a setDateUpdatedAt().
 */
@DataJpaTest
class PreUpdateAuditTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    /**
     * Cria uma role persistida para uso como FK obrigatória nos testes de User.
     */
    private Role persistRole() {
        Role role = new Role();
        role.setName("ROLE_TEST_" + System.nanoTime());
        entityManager.persist(role);
        return role;
    }

    // --- Testes do callback @PrePersist ---

    @Test
    void onPrePersist_setsDateCreatedAt_andDateUpdatedAt_isNull() {
        // Arrange
        Role role = persistRole();
        User user = buildUser("persist-test@example.com", role);

        // Act
        entityManager.persistAndFlush(user);

        // Assert
        assertNotNull(user.getDateCreatedAt(), "dateCreatedAt deve ser preenchido pelo @PrePersist");
        assertNull(user.getDateUpdatedAt(), "dateUpdatedAt deve ser null após persist (nenhuma atualização ocorreu)");
    }

    // --- Testes do callback @PreUpdate ---

    @Test
    void onPreUpdate_setsDateUpdatedAt_automaticallyAfterMerge() {
        // Arrange — persiste um usuário sem dateUpdatedAt
        Role role = persistRole();
        User user = buildUser("update-test@example.com", role);
        entityManager.persistAndFlush(user);

        assertNull(user.getDateUpdatedAt(), "pré-condição: dateUpdatedAt deve ser null antes da atualização");

        // Act — modifica e faz flush para disparar @PreUpdate
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        user.setName("Nome Atualizado");
        entityManager.flush();

        // Assert
        assertNotNull(user.getDateUpdatedAt(), "dateUpdatedAt deve ser preenchido automaticamente pelo @PreUpdate");
        assertTrue(
            user.getDateUpdatedAt().isAfter(beforeUpdate),
            "dateUpdatedAt deve ser posterior ao momento antes da atualização"
        );
    }

    @Test
    void onPreUpdate_dateUpdatedAt_isAfterDateCreatedAt() {
        // Arrange
        Role role = persistRole();
        User user = buildUser("order-test@example.com", role);
        entityManager.persistAndFlush(user);

        LocalDateTime createdAt = user.getDateCreatedAt();
        assertNotNull(createdAt);

        // Act
        user.setName("Outro Nome");
        entityManager.flush();

        // Assert
        LocalDateTime updatedAt = user.getDateUpdatedAt();
        assertNotNull(updatedAt);
        assertFalse(
            updatedAt.isBefore(createdAt),
            "dateUpdatedAt não deve ser anterior a dateCreatedAt"
        );
    }

    @Test
    void onPreUpdate_multipleUpdates_refreshesDateUpdatedAt() {
        // Arrange
        Role role = persistRole();
        User user = buildUser("multi-update@example.com", role);
        entityManager.persistAndFlush(user);

        // Act — primeira atualização
        user.setName("Primeiro Update");
        entityManager.flush();
        LocalDateTime firstUpdatedAt = user.getDateUpdatedAt();
        assertNotNull(firstUpdatedAt);

        // Act — segunda atualização (aguarda pelo menos 1ms para garantir diferença de timestamp)
        user.setName("Segundo Update");
        entityManager.flush();
        LocalDateTime secondUpdatedAt = user.getDateUpdatedAt();

        // Assert — o segundo dateUpdatedAt deve ser >= ao primeiro (pode ser igual se ocorrer no mesmo milissegundo)
        assertNotNull(secondUpdatedAt);
        assertFalse(
            secondUpdatedAt.isBefore(firstUpdatedAt),
            "o segundo dateUpdatedAt não deve ser anterior ao primeiro"
        );
    }

    @Test
    void onPreUpdate_doesNotModify_dateCreatedAt() {
        // Garante que @PreUpdate não altera dateCreatedAt, que é updatable=false
        Role role = persistRole();
        User user = buildUser("immutable-created@example.com", role);
        entityManager.persistAndFlush(user);

        LocalDateTime originalCreatedAt = user.getDateCreatedAt();
        assertNotNull(originalCreatedAt);

        // Act
        user.setName("Mudança de Nome");
        entityManager.flush();

        // Assert
        assertEquals(
            originalCreatedAt,
            user.getDateCreatedAt(),
            "dateCreatedAt não deve ser alterado pelo @PreUpdate"
        );
    }

    // --- Teste unitário do callback (sem banco) ---

    @Test
    void onPreUpdate_unitTest_setsDateUpdatedAt_onConcreteEntity() {
        // Testa o callback diretamente, sem persistência
        User user = new User();
        assertNull(user.getDateUpdatedAt());

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        user.onPreUpdate();

        assertNotNull(user.getDateUpdatedAt());
        assertTrue(user.getDateUpdatedAt().isAfter(before));
    }

    // --- Helpers ---

    private User buildUser(String email, Role role) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("secret");
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountLocked(false);
        return user;
    }
}
