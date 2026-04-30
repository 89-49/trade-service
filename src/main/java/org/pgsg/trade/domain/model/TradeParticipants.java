package org.pgsg.trade.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;
import org.pgsg.trade.domain.exception.TradeErrorCode;

import java.util.UUID;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeParticipants {

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private UUID buyerId;

    @Column(name = "buyer_name", nullable = false, length = 255, updatable = false)
    private String buyerName;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "seller_name", nullable = false, length = 255, updatable = false)
    private String sellerName;

    @Builder(access = AccessLevel.PRIVATE)
    private TradeParticipants(UUID buyerId, String buyerName, UUID sellerId, String sellerName) {
        validateBuyerId(buyerId);
        validateBuyerName(buyerName);
        validateSellerId(sellerId);
        validateSellerName(sellerName);
        validateParticipants(buyerId, sellerId);

        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
    }

    public static TradeParticipants of(UUID buyerId, String buyerName, UUID sellerId, String sellerName) {
        return TradeParticipants.builder()
                .buyerId(buyerId)
                .buyerName(buyerName)
                .sellerId(sellerId)
                .sellerName(sellerName)
                .build();
    }

    // TODO: 리팩토링 - 검증 로직을 별도의 Validator 클래스로 분리
    private void validateBuyerId(UUID buyerId) {
        if (buyerId == null) {
            throw new TradeDomainValidationException(TradeErrorCode.BUYER_ID_REQUIRED);
        }
    }

    private void validateBuyerName(String buyerName) {
        if (buyerName == null || buyerName.isBlank()) {
            throw new TradeDomainValidationException(TradeErrorCode.BUYER_NAME_REQUIRED);
        }
        if (buyerName.length() > 255) {
            throw new TradeDomainValidationException(TradeErrorCode.BUYER_NAME_LENGTH_EXCEEDED);
        }
    }

    private void validateSellerId(UUID sellerId) {
        if (sellerId == null) {
            throw new TradeDomainValidationException(TradeErrorCode.SELLER_ID_REQUIRED);
        }
    }

    private void validateSellerName(String sellerName) {
        if (sellerName == null || sellerName.isBlank()) {
            throw new TradeDomainValidationException(TradeErrorCode.SELLER_NAME_REQUIRED);
        }
        if (sellerName.length() > 255) {
            throw new TradeDomainValidationException(TradeErrorCode.SELLER_NAME_LENGTH_EXCEEDED);
        }
    }

    private void validateParticipants(UUID buyerId, UUID sellerId) {
        if (buyerId.equals(sellerId)) {
            throw new TradeDomainValidationException(TradeErrorCode.PARTICIPANTS_SAME_PERSON);
        }
    }
}
