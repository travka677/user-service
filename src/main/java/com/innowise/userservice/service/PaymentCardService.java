package com.innowise.userservice.service;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.specification.PaymentCardSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;

    public PaymentCardService(PaymentCardRepository paymentCardRepository, UserRepository userRepository) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
    }

    public PaymentCard getCardById(UUID id) {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
    }

    public List<PaymentCard> getCardsByUserId(UUID userId) {
        return paymentCardRepository.findAllByUserId(userId);
    }

    public Page<PaymentCard> getAllCards(String holder, Boolean active, Pageable pageable) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasHolder(holder))
                .and(PaymentCardSpecification.isActive(active));
        return paymentCardRepository.findAll(spec, pageable);
    }

    @Transactional
    public PaymentCard updateCard(UUID id, PaymentCard updated) {
        PaymentCard card = getCardById(id);
        card.setNumber(updated.getNumber());
        card.setHolder(updated.getHolder());
        card.setExpirationDate(updated.getExpirationDate());
        return paymentCardRepository.save(card);
    }

    @Transactional
    public PaymentCard setCardActive(UUID id, Boolean active) {
        PaymentCard card = getCardById(id);
        card.setActive(active);
        return paymentCardRepository.save(card);
    }

    public void deleteCard(UUID id) {
        paymentCardRepository.deleteById(id);
    }
}
