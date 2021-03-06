package com.capgemini.user.exception;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class FriendManagementExceptionAdvice {

	@ExceptionHandler(FriendManagementAPIResourceNotFound.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public @ResponseBody FriendManagementException handleResourceNotFound(final FriendManagementAPIResourceNotFound exception,
			final HttpServletRequest request) {

		final FriendManagementException error = new FriendManagementException();
		error.setErrorMessage(exception.getMessage());
		error.callerURL(request.getRequestURI());

		return error;
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody FriendManagementException handleException(final Exception exception,
			final HttpServletRequest request) {

		final FriendManagementException error = new FriendManagementException();
		error.setErrorMessage(exception.getMessage());
		error.callerURL(request.getRequestURI());

		return error;
	}
	
		@ExceptionHandler
	    @ResponseBody
	    @ResponseStatus(HttpStatus.BAD_REQUEST)
	    public Map<String, Object> handle(MethodArgumentNotValidException exception) {
	        return error(exception.getBindingResult().getFieldErrors()
	                .stream()
	                .map(FieldError::getDefaultMessage)
	                .collect(Collectors.toList()));
	    }


	    @ExceptionHandler
	    @ResponseBody
	    @ResponseStatus(HttpStatus.BAD_REQUEST)
	    public Map<?, ?> handle(ConstraintViolationException exception) {
	        return error(exception.getConstraintViolations()
	                .stream()
	                .map(ConstraintViolation::getMessage)
	                .collect(Collectors.toList()));
	    }

	    private Map<String, Object> error(Object message) {
	        return Collections.singletonMap("error", message);
	    }
	
}
