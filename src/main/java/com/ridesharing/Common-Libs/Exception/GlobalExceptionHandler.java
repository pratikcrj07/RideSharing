package com.ridesharing.CommonLibs.Exception.Exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice


public class GlobalExceptionHandler {

    @ExceptionHandler(com.ridesharing.CommonLibs.Exception.Exception.ApiException.class)
    public ResponseEntity<?> handleApi(com.ridesharing.CommonLibs.Exception.Exception.ApiException ex) {

        return ResponseEntity.badRequest().body(
                new com.ridesharing.CommonLibs.Exception.Exception.ErrorResponse(400, ex.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {
        return ResponseEntity.internalServerError().body(
                new com.ridesharing.CommonLibs.Exception.Exception.ErrorResponse(500, "Something went wrong")
        );
    }
}


