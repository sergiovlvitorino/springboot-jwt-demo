package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenHasherTest {

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = RefreshTokenHasher.generateToken();
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_returnsDifferentTokensEachTime() {
        String token1 = RefreshTokenHasher.generateToken();
        String token2 = RefreshTokenHasher.generateToken();
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void generateToken_returnsBase64UrlEncodedString() {
        String token = RefreshTokenHasher.generateToken();
        // Base64 URL without padding: only A-Z, a-z, 0-9, -, _
        assertThat(token).matches("[A-Za-z0-9\\-_]+");
    }

    @Test
    void hash_returnsSha256HexOf64Chars() {
        String hash = RefreshTokenHasher.hash("some-raw-token");
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void hash_sameInputProducesSameHash() {
        String raw = RefreshTokenHasher.generateToken();
        assertThat(RefreshTokenHasher.hash(raw)).isEqualTo(RefreshTokenHasher.hash(raw));
    }

    @Test
    void hash_differentInputsProduceDifferentHashes() {
        String hash1 = RefreshTokenHasher.hash("token-one");
        String hash2 = RefreshTokenHasher.hash("token-two");
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void hash_generatedTokenProduces64CharHash() {
        String raw = RefreshTokenHasher.generateToken();
        String hash = RefreshTokenHasher.hash(raw);
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }
}
