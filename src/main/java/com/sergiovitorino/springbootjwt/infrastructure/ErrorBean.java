package com.sergiovitorino.springbootjwt.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorBean {

    private String className;
    private String fieldError;
    private String message;

}
