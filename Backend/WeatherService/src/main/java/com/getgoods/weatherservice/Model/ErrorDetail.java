package com.getgoods.weatherservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {
    LocalDateTime timestamp;
    String message;
    String desc;
}
