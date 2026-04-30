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
public class TradedItem {

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 255, updatable = false)
    private String productName;

    @Column(name = "product_price", nullable = false, updatable = false)
    private Long productPrice;

    @Builder(access = AccessLevel.PRIVATE)
    private TradedItem(UUID productId, String productName, Long productPrice) {
        validateProductId(productId);
        validateProductName(productName);
        validateProductPrice(productPrice);

        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
    }

    public static TradedItem of(UUID productId, String productName, Long productPrice) {
        return TradedItem.builder()
                .productId(productId)
                .productName(productName)
                .productPrice(productPrice)
                .build();
    }

    // TODO: 리팩토링 - 검증 로직을 별도의 Validator 클래스로 분리
    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new TradeDomainValidationException(TradeErrorCode.TRADED_ITEM_ID_REQUIRED);
        }
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new TradeDomainValidationException(TradeErrorCode.TRADED_ITEM_NAME_REQUIRED);
        }
        if (productName.length() > 255) {
            throw new TradeDomainValidationException(TradeErrorCode.TRADED_ITEM_NAME_LENGTH_EXCEEDED);
        }
    }

    private void validateProductPrice(Long productPrice) {
        if (productPrice == null) {
            throw new TradeDomainValidationException(TradeErrorCode.TRADED_ITEM_PRICE_REQUIRED);
        }
        if (productPrice <= 0) {
            throw new TradeDomainValidationException(TradeErrorCode.TRADED_ITEM_PRICE_INVALID_RANGE);
        }
    }
}
