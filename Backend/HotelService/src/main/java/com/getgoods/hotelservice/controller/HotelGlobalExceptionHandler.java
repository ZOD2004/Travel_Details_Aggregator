package com.getgoods.hotelservice.controller;

import com.getgoods.hotelservice.model.ErrorDetails;
import com.getgoods.hotelservice.util.HotelsNotFoundException;
import com.getgoods.hotelservice.util.ScrapingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class HotelGlobalExceptionHandler extends Exception{

    @ExceptionHandler(HotelsNotFoundException.class)
    public ResponseEntity<ErrorDetails> HotelsNotFoundException(HotelsNotFoundException ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(ex.getMessage(), request.getDescription(true), LocalDateTime.now() );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ScrapingException.class)
    public ResponseEntity<ErrorDetails> ScrapingException(ScrapingException ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(ex.getMessage(), request.getDescription(true), LocalDateTime.now() );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> Exception(Exception ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(ex.getMessage(), request.getDescription(true), LocalDateTime.now() );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
