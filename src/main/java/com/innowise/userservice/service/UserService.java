package com.innowise.userservice.service;

import com.innowise.userservice.cache.CacheNames;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.specification.UserSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserService {

    private static final int MAX_CARDS_PER_USER = 5;

    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;

    public UserService(UserRepository userRepository,
                       PaymentCardRepository paymentCardRepository) {
        this.userRepository = userRepository;
        this.paymentCardRepository = paymentCardRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.USER, key = "#id")
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));
        return userRepository.findAll(spec, pageable);
    }

    @CachePut(value = CacheNames.USER, key = "#id")
    public User updateUser(UUID id, User updated) {
        User user = getUserById(id);
        user.setName(updated.getName());
        user.setSurname(updated.getSurname());
        user.setBirthDate(updated.getBirthDate());
        user.setEmail(updated.getEmail());
        return userRepository.save(user);
    }

    @CachePut(value = CacheNames.USER, key = "#id")
    public User setUserActive(UUID id, Boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        return userRepository.save(user);
    }

    @CacheEvict(value = CacheNames.USER_CARDS, key = "'list_' + #userId")
    public PaymentCard addCardToUser(UUID userId, PaymentCard card) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (paymentCardRepository.countByUserId(userId) >= MAX_CARDS_PER_USER) {
            throw new CardLimitExceededException(
                    "User already has maximum number of cards: " + MAX_CARDS_PER_USER);
        }

        card.setUser(user);
        return paymentCardRepository.save(card);
    }
}
