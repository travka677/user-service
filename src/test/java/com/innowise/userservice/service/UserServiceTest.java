package com.innowise.userservice.service;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setEmail("ivan@example.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setActive(true);
    }

    @Test
    @DisplayName("Should save and return user")
    void createUser() {
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should return user when exists")
    void getUserById() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not exists")
    void getUserByIdNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    @DisplayName("Should return page of users")
    void getAllUsers() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<User> result = userService.getAllUsers("Ivan", null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should update user fields and return updated user")
    void updateUser() {
        User updated = new User();
        updated.setName("Petr");
        updated.setSurname("Petrov");
        updated.setEmail("petr@example.com");
        updated.setBirthDate(LocalDate.of(1995, 5, 5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(userId, updated);

        assertThat(result.getName()).isEqualTo("Petr");
        assertThat(result.getSurname()).isEqualTo("Petrov");
        assertThat(result.getEmail()).isEqualTo("petr@example.com");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should set user active status")
    void setUserActive() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.setUserActive(userId, false);

        assertThat(result.getActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should save card when user is under limit")
    void addCardToUser() {
        PaymentCard card = new PaymentCard();
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(3);
        when(paymentCardRepository.save(card)).thenReturn(card);

        PaymentCard result = userService.addCardToUser(userId, card);

        assertThat(result.getUser()).isEqualTo(user);
        verify(paymentCardRepository).save(card);
    }

    @Test
    @DisplayName("Should throw CardLimitExceededException when user is at limit")
    void addCardToUserLimitExceeded() {
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(5);

        assertThatThrownBy(() -> userService.addCardToUser(userId, new PaymentCard()))
                .isInstanceOf(CardLimitExceededException.class);

        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not exists on add card")
    void addCardToUserNotFound() {
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addCardToUser(userId, new PaymentCard()))
                .isInstanceOf(NotFoundException.class);
    }
}
