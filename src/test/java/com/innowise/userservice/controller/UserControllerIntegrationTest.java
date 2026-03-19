package com.innowise.userservice.controller;

import com.innowise.userservice.BaseIntegrationTest;
import com.innowise.userservice.dto.request.PaymentCardRequest;
import com.innowise.userservice.dto.request.UserRequest;
import com.innowise.userservice.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    private static final AtomicLong CARD_COUNTER = new AtomicLong(2000_0000_0000_0000L);

    private String uniqueCardNumber() {
        return String.valueOf(CARD_COUNTER.getAndIncrement());
    }

    private UserRequest buildUserRequest() {
        UserRequest request = new UserRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setEmail("ivan+" + System.nanoTime() + "@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        return request;
    }

    private UserResponse buildUser() {
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "/users", buildUserRequest(), UserResponse.class);
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

    @Test
    @DisplayName("Should return 201 and created user")
    void createUser() {
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "/users", buildUserRequest(), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Ivan");
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    @DisplayName("Should return 200 and user when exists")
    void getUserById() {
        UserResponse created = buildUser();

        ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                "/users/" + created.getId(), UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("Should return 404 when user not exists")
    void getUserByIdNotFound() {
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "/users/" + UUID.randomUUID(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 200 and updated user")
    void updateUser() {
        UserResponse created = buildUser();

        UserRequest updateRequest = buildUserRequest();
        updateRequest.setName("Petr");
        updateRequest.setSurname("Petrov");

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/users/" + created.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Petr");
    }

    @Test
    @DisplayName("Should return 200 and deactivated user")
    void setUserActive() {
        UserResponse created = buildUser();

        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/users/" + created.getId() + "/active?active=false",
                HttpMethod.PATCH,
                HttpEntity.EMPTY,
                UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getActive()).isFalse();
    }

    @Test
    @DisplayName("Should return 201 and card on add card to user")
    void addCardToUser() {
        UserResponse created = buildUser();

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/users/" + created.getId() + "/cards", buildCardRequest(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Should return 409 when card limit exceeded")
    void addCardToUserLimitExceeded() {
        UserResponse created = buildUser();

        for (int i = 0; i < 5; i++) {
            restTemplate.postForEntity(
                    "/users/" + created.getId() + "/cards", buildCardRequest(), Void.class);
        }

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/users/" + created.getId() + "/cards", buildCardRequest(), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
