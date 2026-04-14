package com.sergiovitorino.springbootjwt.infrastructure.validations;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para StrongPasswordValidator.
 *
 * Regra: senha válida deve ter >= 8 caracteres, pelo menos 1 letra maiúscula,
 * 1 letra minúscula, 1 dígito e 1 caractere especial (@$!%*?&).
 *
 * Quando inválida, o validator desabilita a mensagem padrão e constrói uma
 * mensagem customizada indicando quais requisitos estão faltando.
 */
class StrongPasswordValidatorTest {

    private StrongPasswordValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new StrongPasswordValidator();
        validator.initialize(null);

        // Configura o mock do ConstraintValidatorContext para suportar o fluxo
        // de mensagem customizada: disableDefaultConstraintViolation() +
        // buildConstraintViolationWithTemplate(...).addConstraintViolation()
        doNothing().when(context).disableDefaultConstraintViolation();
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    // -------------------------------------------------------------------------
    // Casos inválidos — retorno false
    // -------------------------------------------------------------------------

    @Test
    void isValid_null_returnsFalse() {
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void isValid_emptyString_returnsFalse() {
        assertFalse(validator.isValid("", context));
    }

    @Test
    void isValid_onlyLowercase_returnsFalse() {
        assertFalse(validator.isValid("abcdefgh", context));
    }

    @Test
    void isValid_onlyUppercase_returnsFalse() {
        assertFalse(validator.isValid("ABCDEFGH", context));
    }

    @Test
    void isValid_onlyDigits_returnsFalse() {
        assertFalse(validator.isValid("12345678", context));
    }

    @Test
    void isValid_missingSpecialChar_returnsFalse() {
        // Tem maiúscula, minúscula, dígito, mas sem caractere especial
        assertFalse(validator.isValid("Abcdefg1", context));
    }

    @Test
    void isValid_tooShort_returnsFalse() {
        // 7 chars com todos os tipos de caractere — ainda inválido por tamanho
        assertFalse(validator.isValid("Ab!1234", context));
    }

    @Test
    void isValid_missingDigit_returnsFalse() {
        assertFalse(validator.isValid("Abcdef!@", context));
    }

    @Test
    void isValid_missingUppercase_returnsFalse() {
        assertFalse(validator.isValid("abcdef!1", context));
    }

    @Test
    void isValid_missingLowercase_returnsFalse() {
        assertFalse(validator.isValid("ABCDEF!1", context));
    }

    @ParameterizedTest(name = "senha inválida: \"{0}\"")
    @ValueSource(strings = {
        "short",
        "nouppercase1!",
        "NOLOWERCASE1!",
        "NoSpecialChar1",
        "NoDigit!@Abc"
    })
    void isValid_invalidPasswords_returnsFalse(String password) {
        assertFalse(validator.isValid(password, context));
    }

    // -------------------------------------------------------------------------
    // Casos válidos — retorno true
    // -------------------------------------------------------------------------

    @Test
    void isValid_allRequirementsMet_8chars_returnsTrue() {
        // 8 chars: maiúscula, minúscula, dígito, especial
        assertTrue(validator.isValid("Abcdef!1", context));
    }

    @Test
    void isValid_seedPassword_returnsTrue() {
        // Senha usada no seed de dados do projeto
        assertTrue(validator.isValid("Test@1234", context));
    }

    @Test
    void isValid_longComplexPassword_returnsTrue() {
        assertTrue(validator.isValid("MyS3cur3P@ssw0rd!", context));
    }

    @ParameterizedTest(name = "senha válida: \"{0}\"")
    @ValueSource(strings = {
        "Abcdef!1",
        "Test@1234",
        "Str0ng!Pass",
        "C0mpl3x$Word",
        "P@ssw0rd123"
    })
    void isValid_validPasswords_returnsTrue(String password) {
        assertTrue(validator.isValid(password, context));
    }

    // -------------------------------------------------------------------------
    // Verificação das mensagens de erro customizadas
    // -------------------------------------------------------------------------

    @Test
    void isValid_null_doesNotBuildConstraintViolation() {
        // null retorna false imediatamente sem chamar buildConstraintViolationWithTemplate
        validator.isValid(null, context);
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_emptyString_doesNotBuildConstraintViolation() {
        // string vazia retorna false imediatamente sem chamar buildConstraintViolationWithTemplate
        validator.isValid("", context);
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_invalidPassword_disablesDefaultConstraintViolation() {
        validator.isValid("abcdefgh", context);
        verify(context, times(1)).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_invalidPassword_buildsConstraintViolation() {
        validator.isValid("abcdefgh", context);
        verify(context, times(1)).buildConstraintViolationWithTemplate(anyString());
        verify(violationBuilder, times(1)).addConstraintViolation();
    }

    @Test
    void isValid_onlyLowercase_messageContainsUppercaseRequirement() {
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        validator.isValid("abcdefgh", context);

        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        String message = messageCaptor.getValue();

        assertTrue(message.startsWith("Password must contain: "),
            "Mensagem deve iniciar com o prefixo padrão");
        assertTrue(message.contains("uppercase letter"),
            "Mensagem deve indicar ausência de letra maiúscula");
        assertTrue(message.contains("number"),
            "Mensagem deve indicar ausência de dígito");
        assertTrue(message.contains("special character"),
            "Mensagem deve indicar ausência de caractere especial");
    }

    @Test
    void isValid_tooShort_messageContainsSizeRequirement() {
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Senha de 7 chars com todos os tipos — só falha no tamanho
        validator.isValid("Ab!1234", context);

        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        String message = messageCaptor.getValue();

        assertTrue(message.contains("at least 8 characters"),
            "Mensagem deve indicar requisito de tamanho mínimo");
    }

    @Test
    void isValid_missingSpecialChar_messageContainsSpecialCharRequirement() {
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        validator.isValid("Abcdefg1", context);

        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        String message = messageCaptor.getValue();

        assertTrue(message.contains("special character (@$!%*?&)"),
            "Mensagem deve indicar quais caracteres especiais são aceitos");
    }

    @Test
    void isValid_validPassword_neverCallsDisableDefaultConstraintViolation() {
        // Senha válida não deve tocar no ConstraintValidatorContext
        validator.isValid("Test@1234", context);
        verify(context, never()).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    // -------------------------------------------------------------------------
    // Edge cases de fronteira
    // -------------------------------------------------------------------------

    @Test
    void isValid_exactlyEightCharsAllTypes_returnsTrue() {
        // Exatamente no limite mínimo de 8 caracteres com todos os requisitos
        assertTrue(validator.isValid("Aa1@aaaa", context));
    }

    @Test
    void isValid_sevenCharsAllTypes_returnsFalse() {
        // Um char abaixo do mínimo
        assertFalse(validator.isValid("Aa1@aaa", context));
    }

    @ParameterizedTest(name = "especial válido: \"Ab1{0}cdef\"")
    @CsvSource({
        "@", "$", "!", "%", "*", "?", "&"
    })
    void isValid_eachAllowedSpecialChar_returnsTrue(String specialChar) {
        // Cada caractere especial aceito pelo regex deve individualmente validar
        String password = "Ab1" + specialChar + "cdef";
        assertTrue(validator.isValid(password, context),
            "Senha com caractere especial '" + specialChar + "' deve ser válida");
    }
}
