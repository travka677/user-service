package com.innowise.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "payment_cards")
@Getter
@Setter
@NoArgsConstructor
public class PaymentCard extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Boolean active = true;
}
