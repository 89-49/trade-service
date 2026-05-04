package org.pgsg.trade.application.dto.command;

import java.util.UUID;

public record CreateTradeCommand(
        UUID reservationId,
        UUID productId,
        String productName,
        Long productPrice,
        UUID sellerId,
        String sellerNickName,
        UUID buyerId,
        String buyerNickName
) {
}
