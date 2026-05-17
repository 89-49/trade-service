package org.pgsg.trade.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;
import org.pgsg.trade.domain.exception.TradeErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Trade 도메인 단위 테스트")
class TradeTest {

    private static final UUID VALID_RESERVATION_ID = UUID.randomUUID();
    private static final UUID VALID_BUYER_ID = UUID.randomUUID();
    private static final String VALID_BUYER_NAME = "성공하는 구매자명";
    private static final UUID VALID_SELLER_ID = UUID.randomUUID();
    private static final String VALID_SELLER_NAME = "성공하는 판매자명";

    private TradeParticipants validParticipants;
    private TradedItem validTradedItem;

    @BeforeEach
    void setUp() {
        validParticipants = TradeParticipants.of(VALID_BUYER_ID, VALID_BUYER_NAME, VALID_SELLER_ID, VALID_SELLER_NAME);
        validTradedItem = TradedItem.of(UUID.randomUUID(), "테스트 상품", 15000L);
    }

    @Test
    @DisplayName("성공: 유효한 데이터가 주어지면 TRADING 상태의 거래가 생성된다.")
    void createTrade_Success() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);

        assertThat(trade.getReservationId()).isEqualTo(VALID_RESERVATION_ID);
        assertThat(trade.getParticipants()).isEqualTo(validParticipants);
        assertThat(trade.getTradedItem()).isEqualTo(validTradedItem);

        assertThat(trade.getStatus()).isEqualTo(TradeStatus.TRADING);
        assertThat(trade.getBuyerStatus()).isEqualTo(ParticipantStatus.TRADING);
        assertThat(trade.getSellerStatus()).isEqualTo(ParticipantStatus.TRADING);

        assertThat(trade.getVersion()).isNull();
    }

    @Test
    @DisplayName("실패: 예약 ID가 null이면 예외가 발생한다.")
    void createTrade_NullReservationId_ThrowsException() {
        assertThatThrownBy(() -> Trade.create(null, validParticipants, validTradedItem))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.RESERVATION_ID_REQUIRED);
    }

    @Test
    @DisplayName("실패: 참여자 정보(participants)가 null이면 예외가 발생한다.")
    void createTrade_NullParticipants_ThrowsException() {
        assertThatThrownBy(() -> Trade.create(VALID_RESERVATION_ID, null, validTradedItem))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.PARTICIPANTS_REQUIRED);
    }

    @Test
    @DisplayName("실패: 상품 정보(tradedItem)가 null이면 예외가 발생한다.")
    void createTrade_NullTradedItem_ThrowsException() {
        assertThatThrownBy(() -> Trade.create(VALID_RESERVATION_ID, validParticipants, null))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADED_ITEM_REQUIRED);
    }

    @Test
    @DisplayName("성공: 구매자와 판매자가 모두 완료하면 거래가 완료된다.")
    void completeBy_AllParticipantsCompleted_CompletesTrade() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);

        boolean completedByBuyer = trade.completeBy(VALID_BUYER_ID);
        boolean completedBySeller = trade.completeBy(VALID_SELLER_ID);

        assertThat(completedByBuyer).isFalse();
        assertThat(completedBySeller).isTrue();
        assertThat(trade.getBuyerStatus()).isEqualTo(ParticipantStatus.COMPLETED);
        assertThat(trade.getSellerStatus()).isEqualTo(ParticipantStatus.COMPLETED);
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.COMPLETED);
    }

    @Test
    @DisplayName("실패: 거래 참여자가 아닌 사용자가 완료하면 예외가 발생한다.")
    void completeBy_NotParticipant_ThrowsException() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);

        assertThatThrownBy(() -> trade.completeBy(UUID.randomUUID()))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_PARTICIPANT_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 완료 사용자 ID가 null이면 예외가 발생한다.")
    void completeBy_NullParticipantId_ThrowsException() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);

        assertThatThrownBy(() -> trade.completeBy(null))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_PARTICIPANT_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 이미 완료된 거래를 다시 완료하면 예외가 발생한다.")
    void completeBy_AlreadyCompletedTrade_ThrowsException() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);
        trade.completeBy(VALID_BUYER_ID);
        trade.completeBy(VALID_SELLER_ID);

        assertThatThrownBy(() -> trade.completeBy(VALID_BUYER_ID))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ALREADY_CLOSED);
    }

    @Test
    @DisplayName("성공: 구매자가 거래를 취소하면 거래가 취소된다.")
    void cancelBy_BuyerCancels_CancelsTrade() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);

        trade.cancelBy(VALID_BUYER_ID);

        assertThat(trade.getBuyerStatus()).isEqualTo(ParticipantStatus.CANCELLED);
        assertThat(trade.getSellerStatus()).isEqualTo(ParticipantStatus.TRADING);
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.CANCELLED);
    }

    @Test
    @DisplayName("성공: 판매자가 거래를 취소하면 거래가 취소된다.")
    void cancelBy_SellerCancels_CancelsTrade() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);

        trade.cancelBy(VALID_SELLER_ID);

        assertThat(trade.getBuyerStatus()).isEqualTo(ParticipantStatus.TRADING);
        assertThat(trade.getSellerStatus()).isEqualTo(ParticipantStatus.CANCELLED);
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.CANCELLED);
    }

    @Test
    @DisplayName("성공: 한 참여자가 완료 시도를 했더라도 다른 참여자가 취소하면 전체 거래는 취소 상태가 된다.")
    void cancelBy_OneParticipantCancelsWhileOtherIntendsToComplete_CancelsTrade() {
        // given
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);
        
        // 구매자는 완료 시도 (상태는 여전히 TRADING)
        trade.completeBy(VALID_BUYER_ID);
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.TRADING);
        assertThat(trade.getBuyerStatus()).isEqualTo(ParticipantStatus.COMPLETED);

        // when
        // 판매자가 취소 시도
        trade.cancelBy(VALID_SELLER_ID);

        // then
        // 전체 거래 상태는 CANCELLED가 되어야 함
        assertAll(
                () -> assertThat(trade.getStatus()).isEqualTo(TradeStatus.CANCELLED),
                () -> assertThat(trade.getBuyerStatus()).isEqualTo(ParticipantStatus.COMPLETED),
                () -> assertThat(trade.getSellerStatus()).isEqualTo(ParticipantStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("실패: 거래 참여자가 아닌 사용자가 취소하면 예외가 발생한다.")
    void cancelBy_NotParticipant_ThrowsException() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);

        assertThatThrownBy(() -> trade.cancelBy(UUID.randomUUID()))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_PARTICIPANT_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 취소 사용자 ID가 null이면 예외가 발생한다.")
    void cancelBy_NullParticipantId_ThrowsException() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);

        assertThatThrownBy(() -> trade.cancelBy(null))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_PARTICIPANT_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 이미 취소된 거래를 다시 취소하면 예외가 발생한다.")
    void cancelBy_AlreadyCancelledTrade_ThrowsException() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);
        trade.cancelBy(VALID_BUYER_ID);

        assertThatThrownBy(() -> trade.cancelBy(VALID_SELLER_ID))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ALREADY_CLOSED);
    }

    @Test
    @DisplayName("실패: 이미 완료된 거래를 취소하면 예외가 발생한다.")
    void cancelBy_AlreadyCompletedTrade_ThrowsException() {
        Trade trade = Trade.create(VALID_RESERVATION_ID, validParticipants, validTradedItem);
        trade.completeBy(VALID_BUYER_ID);
        trade.completeBy(VALID_SELLER_ID);

        assertThatThrownBy(() -> trade.cancelBy(VALID_BUYER_ID))
                .isInstanceOf(TradeDomainValidationException.class)
                .extracting(e -> ((TradeDomainValidationException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ALREADY_CLOSED);
    }
}
