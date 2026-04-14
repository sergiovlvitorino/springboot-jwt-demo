package com.sergiovitorino.springbootjwt.infrastructure.validations;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para SafeHtmlValidator.
 *
 * Regra: rejeita qualquer string que contenha tags HTML (padrão <...>).
 * null é aceito — validação de presença é responsabilidade do @NotNull.
 */
class SafeHtmlValidatorTest {

    private SafeHtmlValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new SafeHtmlValidator();
        validator.initialize(null);
    }

    // --- Casos válidos ---

    @Test
    void isValid_null_returnsTrue() {
        // null é aceito; @NotNull é responsável por rejeitar ausência de valor
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void isValid_emptyString_returnsTrue() {
        assertTrue(validator.isValid("", context));
    }

    @Test
    void isValid_plainText_returnsTrue() {
        assertTrue(validator.isValid("hello world", context));
    }

    @Test
    void isValid_textWithNumbers_returnsTrue() {
        assertTrue(validator.isValid("user123 test", context));
    }

    @Test
    void isValid_textWithSpecialCharsButNoTags_returnsTrue() {
        // Caracteres especiais que não formam tags HTML devem ser aceitos
        assertTrue(validator.isValid("Price: $10 & tax", context));
    }

    @Test
    void isValid_comparisonOperatorLessThan_returnsTrue() {
        // "5 < 10" não possui o fechamento ">", portanto o regex <[^>]+> não casa
        assertTrue(validator.isValid("5 < 10", context));
    }

    @Test
    void isValid_htmlEntities_returnsTrue() {
        // Entities HTML codificadas (&lt;script&gt;) não contêm o padrão <...>
        assertTrue(validator.isValid("&lt;script&gt;alert('xss')&lt;/script&gt;", context));
    }

    // --- Casos inválidos ---

    @Test
    void isValid_scriptTag_returnsFalse() {
        assertFalse(validator.isValid("<script>alert('xss')</script>", context));
    }

    @Test
    void isValid_divTag_returnsFalse() {
        assertFalse(validator.isValid("<div>content</div>", context));
    }

    @Test
    void isValid_imgTagWithOnerror_returnsFalse() {
        assertFalse(validator.isValid("<img src=x onerror=alert(1)>", context));
    }

    @Test
    void isValid_anchorTag_returnsFalse() {
        assertFalse(validator.isValid("<a href='http://evil.com'>click</a>", context));
    }

    @Test
    void isValid_selfClosingTag_returnsFalse() {
        assertFalse(validator.isValid("<br/>", context));
    }

    @Test
    void isValid_htmlTagEmbeddedInText_returnsFalse() {
        // Tag embutida no meio de texto legítimo ainda deve ser rejeitada
        assertFalse(validator.isValid("Hello <b>world</b>!", context));
    }

    @ParameterizedTest(name = "isValid({0}) deve retornar false")
    @ValueSource(strings = {
        "<script>",
        "<SCRIPT>",
        "<div class='x'>",
        "<input type='text' />",
        "<style>body{}</style>",
        "<iframe src='evil.com'></iframe>"
    })
    void isValid_variousHtmlTags_returnsFalse(String html) {
        assertFalse(validator.isValid(html, context));
    }
}
