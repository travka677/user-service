package com.innowise.userservice.controller;

import com.innowise.userservice.dto.request.PaymentCardRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;
    private final PaymentCardMapper paymentCardMapper;

    public PaymentCardController(PaymentCardService paymentCardService, PaymentCardMapper paymentCardMapper) {
        this.paymentCardService = paymentCardService;
        this.paymentCardMapper = paymentCardMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponse> getCardById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentCardMapper.toResponse(paymentCardService.getCardById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponse>> getAllCards(
            @RequestParam(required = false) String holder,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(
                paymentCardService.getAllCards(holder, active, pageable)
                        .map(paymentCardMapper::toResponse)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponse> updateCard(
            @PathVariable UUID id,
            @RequestBody @Valid PaymentCardRequest request) {
        PaymentCard updated = paymentCardService.updateCard(id, paymentCardMapper.toEntity(request));
        return ResponseEntity.ok(paymentCardMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<PaymentCardResponse> setCardActive(@PathVariable UUID id,
                                                             @RequestParam Boolean active) {
        return ResponseEntity.ok(
                paymentCardMapper.toResponse(paymentCardService.setCardActive(id, active))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id,
                                           @RequestParam UUID userId) {
        paymentCardService.deleteCard(id, userId);
        return ResponseEntity.noContent().build();
    }
}
