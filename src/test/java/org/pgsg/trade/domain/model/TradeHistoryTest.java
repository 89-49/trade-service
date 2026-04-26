package org.pgsg.trade.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TradeHistory 도메인 단위 테스트 ")
class TradeHistoryTest {

    private static final UUID TRADE_ID = UUID.randomUUID();
    private static final TradeStatus PREVIOUS_STATUS = TradeStatus.TRADING;
    private static final TradeStatus NEW_STATUS = TradeStatus.CANCELLED;

    @Test
    @DisplayName("성공: 모든 정보가 도메인 규칙에 맞게 입력되면 이력이 생성된다.")
    void createHistory_AllValid_Success() {
        TradeHistory history = TradeHistory.create(
                TRADE_ID, PREVIOUS_STATUS, NEW_STATUS,
                CancellerType.BUYER, UUID.randomUUID(),
                CancelReasonType.CHANGE_OF_MIND, "단순 변심입니다."
        );

        assertThat(history.getTradeId()).isEqualTo(TRADE_ID);
        assertThat(history.getCancelReasonType()).isEqualTo(CancelReasonType.CHANGE_OF_MIND);
    }

    @Test
    @DisplayName("성공: 이전 상태(previousStatus)가 null인 초기 생성 이력도 허용한다.")
    void createHistory_NullPreviousStatus_Success() {
        TradeHistory history = TradeHistory.create(
                TRADE_ID, null, TradeStatus.TRADING,
                null, null, null, null
        );

        assertThat(history.getPreviousStatus()).isNull();
        assertThat(history.getNewStatus()).isEqualTo(TradeStatus.TRADING);
    }

    @Test
    @DisplayName("실패: tradeId가 없으면 예외가 발생한다.")
    void createHistory_NullTradeId_ThrowsException() {
        assertThatThrownBy(() -> TradeHistory.create(
                null, PREVIOUS_STATUS, NEW_STATUS, null, null, null, null))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: newStatus가 없으면 예외가 발생한다.")
    void createHistory_NullNewStatus_ThrowsException() {
        assertThatThrownBy(() -> TradeHistory.create(
                TRADE_ID, PREVIOUS_STATUS, null, null, null, null, null))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @ParameterizedTest
    @EnumSource(CancellerType.class)
    @DisplayName("실패: 취소 주체는 지정되었으나 취소자 ID가 null이면 예외가 발생한다.")
    void createHistory_CancellerWithoutId_ThrowsException(CancellerType type) {
        assertThatThrownBy(() -> TradeHistory.create(
                TRADE_ID, PREVIOUS_STATUS, NEW_STATUS,
                type, null, CancelReasonType.ETC, "상세 사유"))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: 구매자가 판매자 전용 사유를 선택하면 예외가 발생한다.")
    void createHistory_BuyerWithSellerReason_ThrowsException() {
        assertThatThrownBy(() -> TradeHistory.create(
                TRADE_ID, PREVIOUS_STATUS, NEW_STATUS,
                CancellerType.BUYER, UUID.randomUUID(),
                CancelReasonType.PRODUCT_ISSUE, null))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: 판매자가 구매자 전용 사유를 선택하면 예외가 발생한다.")
    void createHistory_SellerWithBuyerReason_ThrowsException() {
        assertThatThrownBy(() -> TradeHistory.create(
                TRADE_ID, PREVIOUS_STATUS, NEW_STATUS,
                CancellerType.SELLER, UUID.randomUUID(),
                CancelReasonType.REFUND_REQUEST, null))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @ParameterizedTest
    @EnumSource(value = CancellerType.class, names = {"BUYER", "SELLER"})
    @DisplayName("실패: 일반 사용자가 시스템 전용 사유를 선택하면 예외가 발생한다.")
    void createHistory_UserWithSystemReason_ThrowsException(CancellerType userType) {
        assertThatThrownBy(() -> TradeHistory.create(
                TRADE_ID, PREVIOUS_STATUS, NEW_STATUS,
                userType, UUID.randomUUID(),
                CancelReasonType.POLICY_VIOLATION, null))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("실패: 사유가 ETC일 때 상세 사유가 누락되거나 공백이면 예외가 발생한다.")
    void createHistory_EtcWithoutDetail_ThrowsException(String invalidDetail) {
        assertThatThrownBy(() -> TradeHistory.create(
                TRADE_ID, PREVIOUS_STATUS, NEW_STATUS,
                CancellerType.BUYER, UUID.randomUUID(),
                CancelReasonType.ETC, invalidDetail))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("성공: 사유가 ETC가 아닐 때는 상세 사유가 없어도 생성에 성공한다.")
    void createHistory_NoneEtcWithoutDetail_Success() {
        TradeHistory history = TradeHistory.create(
                TRADE_ID, PREVIOUS_STATUS, NEW_STATUS,
                CancellerType.BUYER, UUID.randomUUID(),
                CancelReasonType.CHANGE_OF_MIND, null
        );

        assertThat(history.getCancelReasonDetail()).isNull();
    }

}
