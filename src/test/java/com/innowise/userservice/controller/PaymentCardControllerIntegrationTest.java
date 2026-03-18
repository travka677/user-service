package com.innowise.userservice.controller;

import com.innowise.userservice.BaseIntegrationTest;
import com.innowise.userservice.dto.request.PaymentCardRequest;
import com.innowise.userservice.dto.request.UserRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import com.innowise.userservice.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCardControllerIntegrationTest extends BaseIntegrationTest {

    private UserResponse createUser() {
        UserRequest request = new UserRequest();
        request.setName("Ivan");
        request.setSurname("Ivanov");
        request.setEmail("ivan+" + System.nanoTime() + "@example.com");
        request.setBirthDate(LocalDate.of(1990, 1, 1));

        return webTestClient.post().uri("/users")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private PaymentCardResponse addCard(UUID userId) {
        PaymentCardRequest request = new PaymentCardRequest();
        request.setNumber("1234567890123456");
        request.setHolder("Ivan Ivanov");
        request.setExpirationDate(LocalDate.now().plusYears(2));

        return webTestClient.post().uri("/users/" + userId + "/cards")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PaymentCardResponse.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    @DisplayName("Should return 200 and card when exists")
    void getCardById() {
        UserResponse user = createUser();
        PaymentCardResponse card = addCard(user.getId());

        webTestClient.get().uri("/cards/" + card.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentCardResponse.class)
                .value(body -> assertThat(body.getId()).isEqualTo(card.getId()));
    }

    @Test
    @DisplayName("Should return 404 when card not exists")
    void getCardByIdNotFound() {
        webTestClient.get().uri("/cards/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should return 200 and updated card")
    void updateCard() {
        UserResponse user = createUser();
        PaymentCardResponse card = addCard(user.getId());

        PaymentCardRequest updateRequest = new PaymentCardRequest();
        updateRequest.setNumber("9999999999999999");
        updateRequest.setHolder("Petr Petrov");
        updateRequest.setExpirationDate(LocalDate.now().plusYears(3));

        webTestClient.put().uri("/cards/" + card.getId())
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentCardResponse.class)
                .value(body -> {
                    assertThat(body.getHolder()).isEqualTo("Petr Petrov");
                    assertThat(body.getNumber()).isEqualTo("9999999999999999");
                });
    }

    @Test
    @DisplayName("Should return 200 and deactivated card")
    void setCardActive() {
        UserResponse user = createUser();
        PaymentCardResponse card = addCard(user.getId());

        webTestClient.patch().uri("/cards/" + card.getId() + "/active?active=false")
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentCardResponse.class)
                .value(body -> assertThat(body.getActive()).isFalse());
    }

    @Test
    @DisplayName("Should return 204 and deactivate card on delete")
    void deleteCard() {
        UserResponse user = createUser();
        PaymentCardResponse card = addCard(user.getId());

        webTestClient.delete().uri("/cards/" + card.getId() + "?userId=" + user.getId())
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/cards/" + card.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentCardResponse.class)
                .value(body -> assertThat(body.getActive()).isFalse());
    }
}
