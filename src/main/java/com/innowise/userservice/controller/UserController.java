package com.innowise.userservice.controller;

import com.innowise.userservice.dto.request.PaymentCardRequest;
import com.innowise.userservice.dto.request.UserRequest;
import com.innowise.userservice.dto.response.PaymentCardResponse;
import com.innowise.userservice.dto.response.UserResponse;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PaymentCardMapper paymentCardMapper;
    private final PaymentCardService paymentCardService;

    public UserController(UserService userService, UserMapper userMapper, PaymentCardMapper paymentCardMapper, PaymentCardService paymentCardService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.paymentCardMapper = paymentCardMapper;
        this.paymentCardService = paymentCardService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
        User saved = userService.createUser(userMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userMapper.toResponse(userService.getUserById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(name, surname, pageable)
                .map(userMapper::toResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
                                                   @RequestBody @Valid UserRequest request) {
        User updated = userService.updateUser(id, userMapper.toEntity(request));
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserResponse> setUserActive(@PathVariable UUID id,
                                                      @RequestParam Boolean active) {
        return ResponseEntity.ok(userMapper.toResponse(userService.setUserActive(id, active)));
    }

    @PostMapping("/{userId}/cards")
    public ResponseEntity<PaymentCardResponse> addCard(@PathVariable UUID userId,
                                                       @RequestBody @Valid PaymentCardRequest request) {
        PaymentCard card = userService.addCardToUser(userId, paymentCardMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardMapper.toResponse(card));
    }

    @GetMapping("/{userId}/cards")
    public ResponseEntity<List<PaymentCardResponse>> getCardsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(paymentCardService.getCardsByUserId(userId)
                .stream()
                .map(paymentCardMapper::toResponse)
                .collect(Collectors.toList()));
    }
}
