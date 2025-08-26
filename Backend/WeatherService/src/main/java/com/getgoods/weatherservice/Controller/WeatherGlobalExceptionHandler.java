package com.getgoods.weatherservice.Controller;

import com.getgoods.weatherservice.util.DateOutOfBoundException;
import com.getgoods.weatherservice.Model.ErrorDetail;
import com.getgoods.weatherservice.util.WeatherNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class WeatherGlobalExceptionHandler extends Exception {

    @ExceptionHandler(DateOutOfBoundException.class)
    public ResponseEntity<ErrorDetail> handleDateOutOfBoundException(DateOutOfBoundException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(LocalDateTime.now(), ex.getMessage(),  request.getDescription(true));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WeatherNotFoundException.class)
    public ResponseEntity<ErrorDetail> handleWeatherNotFoundException(WeatherNotFoundException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(LocalDateTime.now(), ex.getMessage(),  request.getDescription(true));
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND);
    }
}
