package com.innowise.userservice.service;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.repository.PaymentCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private PaymentCardService paymentCardService;

    private PaymentCard card;
    private UUID cardId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();
        userId = UUID.randomUUID();

        User user = new User();
        user.setName("Ivan");

        card = new PaymentCard();
        card.setNumber("1234567890123456");
        card.setHolder("Ivan Ivanov");
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setActive(true);
        card.setUser(user);
    }

    @Test
    @DisplayName("Should return card when exists")
    void getCardById() {
        when(paymentCardRepository.findWithUserById(cardId)).thenReturn(Optional.of(card));

        PaymentCard result = paymentCardService.getCardById(cardId);

        assertThat(result).isEqualTo(card);
    }

    @Test
    @DisplayName("Should throw NotFoundException when card not exists")
    void getCardByIdNotFound() {
        when(paymentCardRepository.findWithUserById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.getCardById(cardId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(cardId.toString());
    }

    @Test
    @DisplayName("Should return list of cards by user id")
    void getCardsByUserId() {
        when(paymentCardRepository.findAllByUserId(userId)).thenReturn(List.of(card));

        List<PaymentCard> result = paymentCardService.getCardsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(card);
    }

    @Test
    @DisplayName("Should return page of cards")
    void getAllCards() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(card)));

        Page<PaymentCard> result = paymentCardService.getAllCards("Ivan Ivanov", true, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should update card fields and return updated card")
    void updateCard() {
        PaymentCard updated = new PaymentCard();
        updated.setNumber("9999999999999999");
        updated.setHolder("Petr Petrov");
        updated.setExpirationDate(LocalDate.now().plusYears(3));

        when(paymentCardRepository.findWithUserById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(card)).thenReturn(card);

        PaymentCard result = paymentCardService.updateCard(cardId, updated);

        assertThat(result.getNumber()).isEqualTo("9999999999999999");
        assertThat(result.getHolder()).isEqualTo("Petr Petrov");
        verify(paymentCardRepository).save(card);
    }

    @Test
    @DisplayName("Should set card active status")
    void setCardActive() {
        when(paymentCardRepository.findWithUserById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(card)).thenReturn(card);

        PaymentCard result = paymentCardService.setCardActive(cardId, false);

        assertThat(result.getActive()).isFalse();
        verify(paymentCardRepository).save(card);
    }

    @Test
    @DisplayName("Should set card inactive on delete")
    void deleteCard() {
        when(paymentCardRepository.findWithUserById(cardId)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(card)).thenReturn(card);

        paymentCardService.deleteCard(cardId, userId);

        assertThat(card.getActive()).isFalse();
        verify(paymentCardRepository).save(card);
    }

    @Test
    @DisplayName("Should throw NotFoundException when card not exists on delete")
    void deleteCardNotFound() {
        when(paymentCardRepository.findWithUserById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCardService.deleteCard(cardId, userId))
                .isInstanceOf(NotFoundException.class);
    }
}
