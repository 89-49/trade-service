package org.pgsg.trade.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(trade.getBuyerStatus()).isNull();
        assertThat(trade.getSellerStatus()).isNull();

        assertThat(trade.getVersion()).isZero();
    }

    @Test
    @DisplayName("실패: 예약 ID가 null이면 예외가 발생한다.")
    void createTrade_NullReservationId_ThrowsException() {
        assertThatThrownBy(() -> Trade.create(null, validParticipants, validTradedItem))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: 참여자 정보(participants)가 null이면 예외가 발생한다.")
    void createTrade_NullParticipants_ThrowsException() {
        assertThatThrownBy(() -> Trade.create(VALID_RESERVATION_ID, null, validTradedItem))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("실패: 상품 정보(tradedItem)가 null이면 예외가 발생한다.")
    void createTrade_NullTradedItem_ThrowsException() {
        assertThatThrownBy(() -> Trade.create(VALID_RESERVATION_ID, validParticipants, null))
                .isInstanceOf(TradeDomainValidationException.class);
    }
}
