package org.pgsg.trade.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Trade 도메인의 TradedItem VO 테스트")
class TradedItemTest {

    private static final UUID VALID_PRODUCT_ID = UUID.randomUUID();
    private static final String VALID_PRODUCT_NAME = "성공하는 상품명";
    private static final Long VALID_PRODUCT_PRICE = 35000L;

    @Test
    @DisplayName("성공: 유효한 데이터가 주어지면 TradedItem 생성에 성공한다.")
    void createTradedItem_Success() {
        TradedItem tradedItem = TradedItem.of(VALID_PRODUCT_ID, VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE);

        assertThat(tradedItem.getProductId()).isEqualTo(VALID_PRODUCT_ID);
        assertThat(tradedItem.getProductName()).isEqualTo(VALID_PRODUCT_NAME);
        assertThat(tradedItem.getProductPrice()).isEqualTo(VALID_PRODUCT_PRICE);
    }

    @Test
    @DisplayName("성공: 상품명이 255자인 경우 TradedItem 생성에 성공한다.")
    void createTradedItem_255CharProductName_Success() {
        String longName = "A".repeat(255);
        TradedItem tradedItem = TradedItem.of(VALID_PRODUCT_ID, longName, VALID_PRODUCT_PRICE);

        assertThat(tradedItem.getProductId()).isEqualTo(VALID_PRODUCT_ID);
        assertThat(tradedItem.getProductName()).isEqualTo(longName);
        assertThat(tradedItem.getProductPrice()).isEqualTo(VALID_PRODUCT_PRICE);
    }

    @Test
    @DisplayName("실패: product_id가 null이면 예외가 발생한다.")
    void createTradedItem_NullProductId_ThrowsException() {
        assertThatThrownBy(() -> TradedItem.of(null, VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("실패: product_name이 null, 빈 문자열, 혹은 공백으로만 이루어져 있으면 예외가 발생한다.")
    void createTradedItem_NullOrEmptyProductName_ThrowsException(String invalidName) {
        assertThatThrownBy(() -> TradedItem.of(VALID_PRODUCT_ID, invalidName, VALID_PRODUCT_PRICE))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: product_price가 null이면 예외가 발생한다.")
    void createTradedItem_NullProductPrice_ThrowsException() {
        assertThatThrownBy(() -> TradedItem.of(VALID_PRODUCT_ID, VALID_PRODUCT_NAME, null))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: product_price가 0 이하면 예외가 발생한다.")
    void createTradedItem_NegativeOrZeroProductPrice_ThrowsException() {
        assertThatThrownBy(() -> TradedItem.of(VALID_PRODUCT_ID, VALID_PRODUCT_NAME, 0L))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: product_name이 255자보다 길면 예외가 발생한다.")
    void createTradedItem_TooLongProductName_ThrowsException() {
        String longName = "A".repeat(256);

        assertThatThrownBy(() -> TradedItem.of(VALID_PRODUCT_ID, longName, VALID_PRODUCT_PRICE))
                .isInstanceOf(TradeDomainValidationException.class);
    }
}
