package com.getgoods.aggeregatorservice.controller;

import com.getgoods.aggeregatorservice.model.ErrorDetail;
import com.getgoods.aggeregatorservice.util.HotelsNotFoundException;
import com.getgoods.aggeregatorservice.util.PlaceDetailsNotFoundException;
import com.getgoods.aggeregatorservice.util.WeatherNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.awt.geom.RectangularShape;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler extends Exception{

    @ExceptionHandler(HotelsNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleHotelsNotFoundException(HotelsNotFoundException ex, WebRequest request){
        ErrorDetail errorDetail = new ErrorDetail(ex.getMessage(), request.getDescription(true), LocalDateTime.now());
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PlaceDetailsNotFoundException.class)
    public ResponseEntity<ErrorDetail> handlePlaceDetailsNotFoundException(PlaceDetailsNotFoundException ex, WebRequest request){
        ErrorDetail errorDetail = new ErrorDetail(ex.getMessage(), request.getDescription(true), LocalDateTime.now());
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(WeatherNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleWeatherNotFoundException(WeatherNotFoundException ex, WebRequest request){
        ErrorDetail errorDetail = new ErrorDetail(ex.getMessage(), request.getDescription(true), LocalDateTime.now());
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> handleException(Exception ex, WebRequest request){
        ErrorDetail errorDetail = new ErrorDetail(ex.getMessage(), request.getDescription(true), LocalDateTime.now());
        return new ResponseEntity<>(errorDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
