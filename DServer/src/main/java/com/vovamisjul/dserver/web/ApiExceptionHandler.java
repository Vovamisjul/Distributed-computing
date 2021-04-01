package com.vovamisjul.dserver.web;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ControllerAdvice
public class ApiExceptionHandler {
    private static Logger LOG = LogManager.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, HttpMessageNotReadableException ex) {
        return new ResponseEntity<>("Malformed JSON request", BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, MethodArgumentNotValidException ex) {
        return new ResponseEntity<>("Missing/incorrect JSON parameters", BAD_REQUEST);
    }

    @ExceptionHandler(MismatchedInputException.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, MismatchedInputException ex) {
        return new ResponseEntity<>("Missing/incorrect JSON parameters", BAD_REQUEST);
    }

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, SignatureVerificationException ex) {
        return new ResponseEntity<>(UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, Exception ex) {
        LOG.error(ex.getMessage(), ex);
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }
}
