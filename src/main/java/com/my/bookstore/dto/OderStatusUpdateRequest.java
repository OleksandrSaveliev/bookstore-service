package com.my.bookstore.dto;

import com.my.bookstore.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OderStatusUpdateRequest {
    @NotNull(message = "{validation.order.status.required}")
    private OrderStatus status;
}
