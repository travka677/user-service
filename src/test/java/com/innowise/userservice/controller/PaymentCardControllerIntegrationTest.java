package com.innowise.userservice.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.innowise.userservice.BaseIntegrationTest;
import com.innowise.userservice.dto.request.PaymentCardRequest;
import com.innowise.userservice.dto.request.UserRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import com.innowise.userservice.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCardControllerIntegrationTest extends BaseIntegrationTest {

    private static final AtomicLong CARD_COUNTER = new AtomicLong(1000_0000_0000_0000L);

    private String uniqueCardNumber() {
        return String.valueOf(CARD_COUNTER.getAndIncrement());
    }

    private UserResponse createUser() {
        UserRequest request = new UserRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setEmail("ivan+" + System.nanoTime() + "@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        ResponseEntity<UserResponse> response = restTemplate.postForEntity("/users", request, UserResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private PaymentCardRequest buildCardRequest() {
        PaymentCardRequest request = new PaymentCardRequest();
        request.setNumber(uniqueCardNumber());
        request.setHolder("Ivan Ivanov");
        request.setExpirationDate(LocalDate.now().plusYears(2));
        return request;
    }

    private PaymentCardResponse addCard(UUID userId) {
        PaymentCardRequest request = buildCardRequest();
        ResponseEntity<Void> postResponse = restTemplate.postForEntity(
                "/users/" + userId + "/cards", request, Void.class);

        assertThat(postResponse.getStatusCode())
                .as("Check if card creation failed with 500. If it did, check Redis/Serialization.")
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<TestPageResponse> response = restTemplate.getForEntity(
                "/cards?active=true", TestPageResponse.class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotEmpty();

        return response.getBody().getContent().get(0);
    }

    @Test
    @DisplayName("Should return 200 and card when exists")
    void getCardById() {
        UserResponse user = createUser();
        PaymentCardResponse card = addCard(user.getId());
        ResponseEntity<PaymentCardResponse> response = restTemplate.getForEntity("/cards/" + card.getId(), PaymentCardResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(card.getId());
    }

    @Test
    @DisplayName("Should return 404 when card not exists")
    void getCardByIdNotFound() {
        ResponseEntity<Void> response = restTemplate.getForEntity("/cards/" + UUID.randomUUID(), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 200 and updated card")
    void updateCard() {
        UserResponse user = createUser();
        PaymentCardResponse card = addCard(user.getId());
        PaymentCardRequest updateRequest = buildCardRequest();
        updateRequest.setHolder("Petr Petrov");

        ResponseEntity<PaymentCardResponse> response = restTemplate.exchange("/cards/" + card.getId(), HttpMethod.PUT, new HttpEntity<>(updateRequest), PaymentCardResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getHolder()).isEqualTo("Petr Petrov");
    }

    @Test
    @DisplayName("Should return 200 and deactivated card")
    void setCardActive() {
        UserResponse user = createUser();
        PaymentCardResponse card = addCard(user.getId());
        ResponseEntity<PaymentCardResponse> response = restTemplate.exchange("/cards/" + card.getId() + "/active?active=false", HttpMethod.PATCH, HttpEntity.EMPTY, PaymentCardResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getActive()).isFalse();
    }

    @Test
    @DisplayName("Should return 204 and deactivate card on delete")
    void deleteCard() {
        UserResponse user = createUser();
        PaymentCardResponse card = addCard(user.getId());
        ResponseEntity<Void> deleteResponse = restTemplate.exchange("/cards/" + card.getId() + "?userId=" + user.getId(), HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<PaymentCardResponse> getResponse = restTemplate.getForEntity("/cards/" + card.getId(), PaymentCardResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getActive()).isFalse();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TestPageResponse {
        private List<PaymentCardResponse> content;
        public List<PaymentCardResponse> getContent() { return content; }
        public void setContent(List<PaymentCardResponse> content) { this.content = content; }
    }
}
