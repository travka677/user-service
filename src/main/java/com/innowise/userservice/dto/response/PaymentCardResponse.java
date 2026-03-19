package com.innowise.userservice.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentCardResponse {
    private UUID id;
    private UUID userId;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
