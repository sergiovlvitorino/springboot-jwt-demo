package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskerTest {

    @Test
    void maskEmail_null_returnsAnonymized() {
        assertThat(PiiMasker.maskEmail(null)).isEqualTo("***");
    }

    @Test
    void maskEmail_withoutAtSign_returnsAnonymized() {
        assertThat(PiiMasker.maskEmail("noemail")).isEqualTo("***");
    }

    @Test
    void maskEmail_singleCharBeforeAt_returnsMaskedWithStar() {
        assertThat(PiiMasker.maskEmail("a@x.com")).isEqualTo("*@x.com");
    }

    @Test
    void maskEmail_normalEmail_masksLocalPart() {
        assertThat(PiiMasker.maskEmail("abc@def.com")).isEqualTo("a***@def.com");
    }

    @Test
    void maskEmail_longEmail_masksLocalPart() {
        assertThat(PiiMasker.maskEmail("sergio@example.com")).isEqualTo("s***@example.com");
    }

    @Test
    void maskEmail_empty_returnsAnonymized() {
        assertThat(PiiMasker.maskEmail("")).isEqualTo("***");
    }

    @Test
    void maskEmail_atSignOnly_returnsAnonymized() {
        assertThat(PiiMasker.maskEmail("@domain.com")).isEqualTo("*@domain.com");
    }
}
