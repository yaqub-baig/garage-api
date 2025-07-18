package com.autoadda.apis.garage.exception;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.autoadda.apis.garage.response.GenericResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<GenericResponse> handleValidation(MethodArgumentNotValidException ex) {
		String errors = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage()).collect(Collectors.joining("; "));
		log.warn("Validation failed: {}", errors);
		return buildResponse(ex, HttpStatus.BAD_REQUEST, ErrorMessage.ERROR_VALIDATION_FAILED + ": " + errors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<GenericResponse> handleConstraintViolation(ConstraintViolationException ex) {
		log.warn("Constraint violation: {}", ex.getMessage());
		return buildResponse(ex, HttpStatus.BAD_REQUEST,
				ErrorMessage.ERROR_CONSTRAINT_VIOLATION + ": " + ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<GenericResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		log.error("Data integrity violation", ex);
		return buildResponse(ex, HttpStatus.CONFLICT, ErrorMessage.ERROR_DATA_INTEGRITY);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<GenericResponse> handleUnreadableJson(HttpMessageNotReadableException ex) {
		log.warn("Malformed request payload", ex);
		return buildResponse(ex, HttpStatus.BAD_REQUEST, ErrorMessage.ERROR_UNREADABLE_JSON);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<GenericResponse> handleMissingParams(MissingServletRequestParameterException ex) {
		String msg = ErrorMessage.ERROR_MISSING_PARAMETER + ": " + ex.getParameterName();
		log.warn(msg, ex);
		return buildResponse(ex, HttpStatus.BAD_REQUEST, msg);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<GenericResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
		log.warn("Unsupported HTTP method", ex);
		return buildResponse(ex, HttpStatus.METHOD_NOT_ALLOWED, ErrorMessage.ERROR_METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<GenericResponse> handleEntityNotFound(EntityNotFoundException ex) {
		log.warn("Entity not found", ex);
		return buildResponse(ex, HttpStatus.NOT_FOUND, ErrorMessage.ERROR_NOT_FOUND);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<GenericResponse> handleAccessDenied(AccessDeniedException ex) {
		log.warn("Access denied", ex);
		return buildResponse(ex, HttpStatus.FORBIDDEN, ErrorMessage.ERROR_FORBIDDEN);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<GenericResponse> handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("Illegal argument", ex);
		return buildResponse(ex, HttpStatus.BAD_REQUEST, ErrorMessage.ERROR_BAD_REQUEST);
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<GenericResponse> handleUnauthorized(UnauthorizedAccessException ex) {
		log.warn("Unauthorized access", ex);
		return buildResponse(ex, HttpStatus.UNAUTHORIZED, ErrorMessage.ERROR_UNAUTHORIZED);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<GenericResponse> handleResourceNotFound(ResourceNotFoundException ex) {
		log.warn("Resource not found", ex);
		return buildResponse(ex, HttpStatus.NOT_FOUND, ErrorMessage.ERROR_NOT_FOUND);
	}

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<GenericResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
		log.warn("Customer not found", ex);
		return buildResponse(ex, HttpStatus.NOT_FOUND, ErrorMessage.ERROR_NOT_FOUND);
	}

	@ExceptionHandler(GarageNotFoundException.class)
	public ResponseEntity<GenericResponse> handleGarageNotFound(GarageNotFoundException ex) {
		log.warn("Garage not found", ex);
		return buildResponse(ex, HttpStatus.NOT_FOUND, ErrorMessage.ERROR_NOT_FOUND);
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<GenericResponse> handleMemberNotFound(MemberNotFoundException ex) {
		log.warn("Member not found", ex);
		return buildResponse(ex, HttpStatus.NOT_FOUND, ErrorMessage.ERROR_NOT_FOUND);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<GenericResponse> handleConflict(ConflictException ex) {
		log.warn("Conflict error", ex);
		return buildResponse(ex, HttpStatus.CONFLICT, ErrorMessage.ERROR_DATA_INTEGRITY);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<GenericResponse> handleGenericException(Exception ex) {
		log.error("Unhandled exception", ex);
		return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.ERROR_INTERNAL_SERVER);
	}

	private ResponseEntity<GenericResponse> buildResponse(Exception ex, HttpStatus status, String message) {
		return ResponseEntity.status(status).body(GenericResponse.builder().status("failed")
				.code(String.valueOf(status.value())).message(message).build());
	}
}