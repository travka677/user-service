package com.innowise.userservice.service;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.specification.UserSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private static final int MAX_CARDS_PER_USER = 5;

    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;

    public UserService(UserRepository userRepository, PaymentCardRepository paymentCardRepository) {
        this.userRepository = userRepository;
        this.paymentCardRepository = paymentCardRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public Page<User> getAllUsers(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));
        return userRepository.findAll(spec, pageable);
    }

    @Transactional
    public User updateUser(UUID id, User updated) {
        User user = getUserById(id);
        user.setName(updated.getName());
        user.setSurname(updated.getSurname());
        user.setBirthDate(updated.getBirthDate());
        user.setEmail(updated.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public User setUserActive(UUID id, Boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public PaymentCard addCardToUser(UUID userId, PaymentCard card) {
        User user = getUserById(userId);
        if (paymentCardRepository.countByUserId(userId) >= MAX_CARDS_PER_USER) {
            throw new CardLimitExceededException("User already has maximum number of cards: " + MAX_CARDS_PER_USER);
        }

        card.setUser(user);
        return paymentCardRepository.save(card);
    }
}
