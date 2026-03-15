package com.innowise.userservice.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "payment_cards")
public class PaymentCard extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Boolean active = true;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getHolder() { return holder; }
    public void setHolder(String holder) { this.holder = holder; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
