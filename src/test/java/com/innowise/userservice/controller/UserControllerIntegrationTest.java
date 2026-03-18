package com.innowise.userservice.controller;

import com.innowise.userservice.BaseIntegrationTest;
import com.innowise.userservice.dto.request.PaymentCardRequest;
import com.innowise.userservice.dto.request.UserRequest;
import com.innowise.userservice.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    private UserRequest buildUserRequest() {
        UserRequest request = new UserRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setEmail("ivan+" + System.nanoTime() + "@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        return request;
    }

    private UserResponse buildUser() {
        return webTestClient.post().uri("/users")
                .bodyValue(buildUserRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    @DisplayName("Should return 201 and created user")
    void createUser() {
        webTestClient.post().uri("/users")
                .bodyValue(buildUserRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .value(body -> {
                    assertThat(body.getName()).isEqualTo("Ivan");
                    assertThat(body.getId()).isNotNull();
                });
    }

    @Test
    @DisplayName("Should return 200 and user when exists")
    void getUserById() {
        UserResponse created = buildUser();

        webTestClient.get().uri("/users/" + created.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(body -> assertThat(body.getId()).isEqualTo(created.getId()));
    }

    @Test
    @DisplayName("Should return 404 when user not exists")
    void getUserByIdNotFound() {
        webTestClient.get().uri("/users/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should return 200 and updated user")
    void updateUser() {
        UserResponse created = buildUser();

        UserRequest updateRequest = buildUserRequest();
        updateRequest.setName("Petr");
        updateRequest.setSurname("Petrov");
        updateRequest.setEmail("petr+" + System.nanoTime() + "@example.com");

        webTestClient.put().uri("/users/" + created.getId())
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(body -> assertThat(body.getName()).isEqualTo("Petr"));
    }

    @Test
    @DisplayName("Should return 200 and deactivated user")
    void setUserActive() {
        UserResponse created = buildUser();

        webTestClient.patch().uri("/users/" + created.getId() + "/active?active=false")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(body -> assertThat(body.getActive()).isFalse());
    }

    @Test
    @DisplayName("Should return 201 and card on add card to user")
    void addCardToUser() {
        UserResponse created = buildUser();

        PaymentCardRequest cardRequest = new PaymentCardRequest();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("Ivan Ivanov");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(2));

        webTestClient.post().uri("/users/" + created.getId() + "/cards")
                .bodyValue(cardRequest)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("Should return 409 when card limit exceeded")
    void addCardToUserLimitExceeded() {
        UserResponse created = buildUser();

        PaymentCardRequest cardRequest = new PaymentCardRequest();
        cardRequest.setNumber("1234567890123456");
        cardRequest.setHolder("Ivan Ivanov");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(2));

        for (int i = 0; i < 5; i++) {
            webTestClient.post().uri("/users/" + created.getId() + "/cards")
                    .bodyValue(cardRequest)
                    .exchange();
        }

        webTestClient.post().uri("/users/" + created.getId() + "/cards")
                .bodyValue(cardRequest)
                .exchange()
                .expectStatus().isEqualTo(409);
    }
}
