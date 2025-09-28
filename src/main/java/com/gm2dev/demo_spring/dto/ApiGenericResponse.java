package com.gm2dev.demo_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiGenericResponse {
    private Boolean success;
    private String message;
}