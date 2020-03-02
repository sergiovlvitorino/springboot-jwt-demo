package com.sergiovitorino.springbootjwt.infrastructure.security;

import lombok.Data;

@Data
public class AccountCredentials {

    private String username;
    private String password;

}