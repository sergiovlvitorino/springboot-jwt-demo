package com.sergiovitorino.springbootjwt.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@Component
@RequestScope
public class ResponseEntityBuilder {

    private final Validator validator;
    private final ObjectMapper mapper;

    private Object result;
    private BindingResult bindingResult;
    private HttpStatus httpStatusSuccess;
    private HttpStatus httpStatusError;

    @Autowired
    public ResponseEntityBuilder(Validator validator, ObjectMapper mapper) {
        this.validator = validator;
        this.mapper = mapper;
    }

    public ResponseEntity<String> build() {
        try {
            httpStatusSuccess = httpStatusSuccess == null ? HttpStatus.OK : httpStatusSuccess;

            if (bindingResult != null && bindingResult.hasErrors())
                return ResponseEntity.badRequest().body(mapper.writeValueAsString(parse(bindingResult)));

            if (validator.isInvalid())
                return ResponseEntity.status(httpStatusError).body(mapper.writeValueAsString(validator.getErrors()));

            if (result == null) {
                // Se o resultado é null e não há erros no validator, retornar sucesso com dados vazios
                if (!validator.isInvalid()) {
                    // Para listas e contagens, retornar sucesso mesmo sem dados
                    if (httpStatusError == HttpStatus.NOT_FOUND) {
                        return ResponseEntity.status(httpStatusSuccess).body("[]");
                    }
                    validator.addError("No data found");
                }
                return ResponseEntity.status(httpStatusError).body(mapper.writeValueAsString(validator.getErrors()));
            }

            // Verificar se é uma página vazia e retornar sucesso mesmo assim
            if (result instanceof org.springframework.data.domain.Page<?> page) {
                if (page.isEmpty() && !validator.isInvalid()) {
                    // Para páginas vazias, sempre retornar sucesso
                    return ResponseEntity.status(httpStatusSuccess).body(mapper.writeValueAsString(result));
                }
            }

            return ResponseEntity.status(httpStatusSuccess).body(mapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private List<ErrorBean> parse(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorBean(null, fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
    }

    public ResponseEntityBuilder httpStatusSuccess(HttpStatus httpStatusSuccess) {
        this.httpStatusSuccess = httpStatusSuccess;
        return this;
    }

    public ResponseEntityBuilder httpStatusError(HttpStatus httpStatusError) {
        this.httpStatusError = httpStatusError;
        return this;
    }

    public ResponseEntityBuilder result(Object result) {
        this.result = result;
        return this;
    }

    public ResponseEntityBuilder bindingResult(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
        return this;
    }
}
