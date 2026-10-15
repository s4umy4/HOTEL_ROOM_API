package com.example.demo;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handle(Exception e) {
		int status;
		String message;
		if (e instanceof ApiException a) {
			status = a.getStatus();
			message = a.getMessage();
		} else if (e instanceof HttpMessageNotReadableException) {
			status = 400;
			message = "Invalid or missing JSON body";
		} else if (e instanceof ErrorResponse er) {
			int s = er.getStatusCode().value();
			if (s == 405) {
				s = 404;
			} else if (s == 415 || s == 406) {
				s = 400;
			}
			status = s;
			message = s == 404 ? "Route not found" : "Bad request";
		} else {
			status = 500;
			message = "Internal server error";
		}
		return ResponseEntity.status(status)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("error", message));
	}
}
