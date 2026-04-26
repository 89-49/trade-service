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

@DisplayName("Trade 도메인의 TradeParticipants VO 테스트")
class TradeParticipantsTest {

    private static final UUID VALID_BUYER_ID = UUID.randomUUID();
    private static final String VALID_BUYER_NAME = "성공하는 구매자명";
    private static final UUID VALID_SELLER_ID = UUID.randomUUID();
    private static final String VALID_SELLER_NAME = "성공하는 판매자명";

    @Test
    @DisplayName("성공: 유효한 데이터가 주어지면 TradeParticipants 생성에 성공한다.")
    void createTradeParticipants_Success() {
        TradeParticipants participants = TradeParticipants.of(VALID_BUYER_ID, VALID_BUYER_NAME, VALID_SELLER_ID, VALID_SELLER_NAME);

        assertThat(participants.getBuyerId()).isEqualTo(VALID_BUYER_ID);
        assertThat(participants.getBuyerName()).isEqualTo(VALID_BUYER_NAME);
        assertThat(participants.getSellerId()).isEqualTo(VALID_SELLER_ID);
        assertThat(participants.getSellerName()).isEqualTo(VALID_SELLER_NAME);
    }

    @Test
    @DisplayName("성공: buyer_name과 seller_name이 유효한 데이터가 주어지면 TradeParticipants 생성에 성공한다.")
    void createTradeParticipants_255CharParticipantsName_Success() {
        String longName = "A".repeat(255);
        TradeParticipants participants = TradeParticipants.of(VALID_BUYER_ID, longName, VALID_SELLER_ID, longName);

        assertThat(participants.getBuyerId()).isEqualTo(VALID_BUYER_ID);
        assertThat(participants.getBuyerName()).isEqualTo(longName);
        assertThat(participants.getSellerId()).isEqualTo(VALID_SELLER_ID);
        assertThat(participants.getSellerName()).isEqualTo(longName);
    }

    @Test
    @DisplayName("실패: buyer_id가 null이면 예외가 발생한다.")
    void createTradeParticipants_NullBuyerId_ThrowsException() {
        assertThatThrownBy(() -> TradeParticipants.of(null, VALID_BUYER_NAME, VALID_SELLER_ID, VALID_SELLER_NAME))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("실패: buyer_name이 null, 빈 문자열, 혹은 공백으로만 이루어져 있으면 예외가 발생한다.")
    void createTradeParticipants_InvalidBuyerName_ThrowsException(String invalidName) {
        assertThatThrownBy(() -> TradeParticipants.of(VALID_BUYER_ID, invalidName, VALID_SELLER_ID, VALID_SELLER_NAME))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: buyer_name이 255자보다 길면 예외가 발생한다.")
    void createTradeParticipants_TooLongBuyerName_ThrowsException() {
        String longName = "A".repeat(256);

        assertThatThrownBy(() -> TradeParticipants.of(VALID_BUYER_ID, longName, VALID_SELLER_ID, VALID_SELLER_NAME))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: seller_id가 null이면 예외가 발생한다.")
    void createTradeParticipants_NullSellerId_ThrowsException() {
        assertThatThrownBy(() -> TradeParticipants.of(VALID_BUYER_ID, VALID_BUYER_NAME, null, VALID_SELLER_NAME))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("실패: seller_name이 null, 빈 문자열, 혹은 공백으로만 이루어져 있으면 예외가 발생한다.")
    void createTradeParticipants_InvalidSellerName_ThrowsException(String invalidName) {
        assertThatThrownBy(() -> TradeParticipants.of(VALID_BUYER_ID, VALID_BUYER_NAME, VALID_SELLER_ID, invalidName))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: seller_name이 255자보다 길면 예외가 발생한다.")
    void createTradeParticipants_TooLongSellerName_ThrowsException() {
        String longName = "A".repeat(256);

        assertThatThrownBy(() -> TradeParticipants.of(VALID_BUYER_ID, VALID_BUYER_NAME, VALID_SELLER_ID, longName))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: buyer_id와 seller_id가 같으면 예외가 발생한다.")
    void createTradeParticipants_SameBuyerAndSeller_ThrowsException() {
        UUID sameId = UUID.randomUUID();

        assertThatThrownBy(() -> TradeParticipants.of(sameId, VALID_BUYER_NAME, sameId, VALID_SELLER_NAME))
                .isInstanceOf(TradeDomainValidationException.class);
    }
}
