package ru.practicum.shareit.error.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ErrorResponse {
    @Getter
    String error;

    Map<String, String> validationErrors;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public ErrorResponse(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }

}