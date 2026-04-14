package com.sergiovitorino.springbootjwt.ui.rest.controller;

import com.sergiovitorino.springbootjwt.application.service.RefreshTokenService;
import com.sergiovitorino.springbootjwt.application.service.RefreshTokenService.RefreshResult;
import com.sergiovitorino.springbootjwt.domain.exception.InvalidRefreshTokenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    public AuthController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            RefreshResult result = refreshTokenService.refreshAccessToken(request.refreshToken());
            return ResponseEntity.ok(new TokenResponse(result.accessToken(), result.refreshToken()));
        } catch (InvalidRefreshTokenException e) {
            return ResponseEntity.status(401).build();
        }
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record TokenResponse(String accessToken, String refreshToken) {
    }
}
