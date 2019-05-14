package com.sergiovitorino.springbootjwt.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequestScope
public class ResponseBuilder {

	private static final String EXCEPTION = "exception";
	private static final String VALIDATION = "validation";
	private Map<String, String> messages = new HashMap<>();
	private Boolean validationError = false;
	private Object resultObject;
	
	@Autowired private ObjectMapper mapper;

	public ResponseBuilder load(Object object) {
		if(object instanceof Exception) {
			final Exception exception = (Exception) object;
			messages.put(EXCEPTION, exception.getMessage());
		} else if(object instanceof BindingResult) {
			final BindingResult bindingResult = (BindingResult) object;
			bindingResult.getAllErrors().iterator().forEachRemaining(error -> {
				messages.put(VALIDATION, error.getDefaultMessage());
			});
			validationError = Boolean.TRUE;
		} else {
			resultObject = object;
		}
		return this;
	}

	public ResponseEntity<?> build() {
		if (resultObject != null) {
			try {
				return ResponseEntity.ok(mapper.writeValueAsString(resultObject));
			} catch (JsonProcessingException e) {
				resultObject = null;
				load(e);
				build();
			}
		}
		final StringBuilder sbFinalMessage = new StringBuilder();
		sbFinalMessage.append("{");
		Set<String> keys = messages.keySet();
		for (String key : keys) {
			sbFinalMessage.append("\"" + key + "\":");
			sbFinalMessage.append("\"" + messages.get(key) + "\",");
		}
		sbFinalMessage.append("}");
		final String finalMessage = sbFinalMessage.toString().replaceAll(",}", "}");
		final HttpStatus status = validationError ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(status).body(finalMessage);
	}

}
