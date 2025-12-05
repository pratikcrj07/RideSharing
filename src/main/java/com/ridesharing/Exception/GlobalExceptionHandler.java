package com.ridesharing.Exception;

import com.nimbusds.oauth2.sdk.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice


    public class GlobalExceptionHandler {

        @ExceptionHandler(ApiException.class)
        public ResponseEntity<?> handleApi(ApiException ex) {

            return ResponseEntity.badRequest().body(
                    new ErrorResponse(400, ex.getMessage())
            );
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleOther(Exception ex) {
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse(500, "Something went wrong")
            );
        }
    }

}
