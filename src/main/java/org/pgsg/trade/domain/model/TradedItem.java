package org.pgsg.trade.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;

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

    // TODO: 리팩토링 필요 - 검증 로직을 별도의 Validator 클래스로 분리, 에러 메시지 상수화
    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new TradeDomainValidationException("productId는 필수입니다.");
        }
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new TradeDomainValidationException("productName은 필수입니다.");
        }
        if (productName.length() > 255) {
            throw new TradeDomainValidationException("productName은 255자 이하이어야 합니다.");
        }
    }

    private void validateProductPrice(Long productPrice) {
        if (productPrice == null) {
            throw new TradeDomainValidationException("productPrice는 필수입니다.");
        }
        if (productPrice <= 0) {
            throw new TradeDomainValidationException("productPrice는 0보다 커야 합니다.");
        }
    }
}
