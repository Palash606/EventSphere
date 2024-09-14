package com.example.eventsphere.dto;

import com.example.eventsphere.validations.FutureDate;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class EventDto {

    @NotBlank(message = "Event Name must not be blank")
    private String eventName;

    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @FutureDate
    private LocalDate eventDate;

    @NotBlank(message = "Event Location must not be blank")
    private String eventLocation;
}