package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID>, JpaSpecificationExecutor<PaymentCard> {
    List<PaymentCard> findAllByUserId(UUID userId);
    int countByUserId(UUID userId);

    @Query("SELECT c FROM PaymentCard c WHERE c.user.id = :userId AND c.active = true")
    List<PaymentCard> findActiveCardsByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT * FROM payment_cards WHERE user_id = :userId", nativeQuery = true)
    List<PaymentCard> findAllByUserIdNative(@Param("userId") UUID userId);
}
