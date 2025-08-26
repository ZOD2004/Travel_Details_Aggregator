package com.getgoods.placedetails.controller;

import com.getgoods.placedetails.model.ErrorDetails;
import com.getgoods.placedetails.util.ParsingException;
import com.getgoods.placedetails.util.PlaceDetailsNotFoundException;
import com.getgoods.placedetails.util.ScrapingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class PlaceGlobalExceptionHandler extends Exception{

    @ExceptionHandler(ScrapingException.class)
    public ResponseEntity<ErrorDetails> handleScrapingException(ScrapingException e, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(e.getMessage(), request.getDescription(true), LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleException(Exception e, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(e.getMessage(), request.getDescription(true), LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PlaceDetailsNotFoundException.class)
    public ResponseEntity<ErrorDetails> handlePlaceDetailsNotFoundException(PlaceDetailsNotFoundException e, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(e.getMessage(), request.getDescription(true), LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ParsingException.class)
    public ResponseEntity<ErrorDetails> handleParsingException(ParsingException e, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(e.getMessage(), request.getDescription(true), LocalDateTime.now());
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
