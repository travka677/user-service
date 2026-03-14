package com.innowise.userservice.specification;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    public static Specification<PaymentCard> hasHolder(String holder) {
        return (root, query, cb) ->
                holder == null ? null : cb.like(cb.lower(root.get("holder")), "%" + holder.toLowerCase() + "%");
    }

    public static Specification<PaymentCard> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("active"), active);
    }
}
