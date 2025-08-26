package com.getgoods.aggeregatorservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {
    String message;
    String desc;
    LocalDateTime timestamp;
}
