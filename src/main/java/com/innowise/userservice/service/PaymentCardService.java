package com.innowise.userservice.service;

import com.innowise.userservice.cache.CacheNames;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.AccessDeniedException;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.specification.PaymentCardSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;

    public PaymentCardService(PaymentCardRepository paymentCardRepository) {
        this.paymentCardRepository = paymentCardRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USER_CARDS, key = "#id")
    public PaymentCard getCardById(UUID id) {
        return paymentCardRepository.findWithUserById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USER_CARDS, key = "'list_' + #userId")
    public List<PaymentCard> getCardsByUserId(UUID userId) {
        return paymentCardRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<PaymentCard> getAllCards(String holder, Boolean active, Pageable pageable) {
        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasHolder(holder))
                .and(PaymentCardSpecification.isActive(active));
        return paymentCardRepository.findAll(spec, pageable);
    }

    @Caching(
            put = @CachePut(value = CacheNames.USER_CARDS, key = "#id"),
            evict = @CacheEvict(value = CacheNames.USER_CARDS, key = "'list_' + #result.user.id")
    )
    public PaymentCard updateCard(UUID id, PaymentCard updated) {
        PaymentCard card = paymentCardRepository.findWithUserById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
        card.setNumber(updated.getNumber());
        card.setHolder(updated.getHolder());
        card.setExpirationDate(updated.getExpirationDate());
        return paymentCardRepository.save(card);
    }

    @Caching(
            put = @CachePut(value = CacheNames.USER_CARDS, key = "#id"),
            evict = @CacheEvict(value = CacheNames.USER_CARDS, key = "'list_' + #result.user.id")
    )
    public PaymentCard setCardActive(UUID id, Boolean active) {
        PaymentCard card = paymentCardRepository.findWithUserById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));
        card.setActive(active);
        return paymentCardRepository.save(card);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_CARDS, key = "#id"),
            @CacheEvict(value = CacheNames.USER_CARDS, key = "'list_' + #userId")
    })
    public void deleteCard(UUID id, UUID userId) {
        PaymentCard card = paymentCardRepository.findWithUserById(id)
                .orElseThrow(() -> new NotFoundException("Card not found: " + id));

        if (card.getUser() == null || !userId.equals(card.getUser().getId())) {
            throw new AccessDeniedException("User " + userId + " is not the owner of card " + id);
        }

        card.setActive(false);
        paymentCardRepository.save(card);
    }
}
