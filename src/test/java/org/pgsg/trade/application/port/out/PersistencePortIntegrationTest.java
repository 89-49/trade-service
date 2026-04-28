package org.pgsg.trade.application.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.domain.model.TradeHistory;
import org.pgsg.trade.domain.model.TradeParticipants;
import org.pgsg.trade.domain.model.TradeStatus;
import org.pgsg.trade.domain.model.TradedItem;
import org.pgsg.trade.infrastructure.adapter.persistence.TradeHistoryPersistenceAdapter;
import org.pgsg.trade.infrastructure.adapter.persistence.TradePersistenceAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@Import({
        PersistencePortIntegrationTest.JpaAuditingTestConfig.class,
        TradePersistenceAdapter.class,
        TradeHistoryPersistenceAdapter.class
})
@DisplayName("PersistencePort 통합 테스트")
class PersistencePortIntegrationTest {

    @Autowired
    private TradePersistencePort tradePersistencePort;

    @Autowired
    private TradeHistoryPersistencePort tradeHistoryPersistencePort;

    @Test
    @DisplayName("TradePersistencePort를 실행하면 거래가 DB에 저장되고 조회된다.")
    void tradePersistencePort_SaveAndFind() {
        // given
        Trade trade = createTrade();

        // when
        Trade savedTrade = tradePersistencePort.save(trade);

        // then
        Optional<Trade> foundById = tradePersistencePort.findById(savedTrade.getId());
        Optional<Trade> foundByReservationId = tradePersistencePort.findByReservationId(savedTrade.getReservationId());
        boolean existsByReservationId = tradePersistencePort.existsByReservationId(savedTrade.getReservationId());

        assertAll(
                () -> assertThat(foundById).isPresent(),
                () -> assertThat(foundById.get().getId()).isEqualTo(savedTrade.getId()),
                () -> assertThat(foundByReservationId).isPresent(),
                () -> assertThat(foundByReservationId.get().getReservationId()).isEqualTo(savedTrade.getReservationId()),
                () -> assertThat(existsByReservationId).isTrue()
        );
    }

    @Test
    @DisplayName("TradeHistoryPersistencePort를 실행하면 거래 이력이 DB에 저장되고 조회된다.")
    void tradeHistoryPersistencePort_SaveAndFindAllByTradeId() {
        // given
        UUID tradeId = UUID.randomUUID();
        TradeHistory tradeHistory = TradeHistory.create(
                tradeId,
                null,
                TradeStatus.TRADING,
                null,
                null,
                null,
                null
        );

        // when
        TradeHistory savedTradeHistory = tradeHistoryPersistencePort.save(tradeHistory);

        // then
        List<TradeHistory> tradeHistories = tradeHistoryPersistencePort.findAllByTradeId(tradeId);

        assertThat(tradeHistories)
                .extracting(TradeHistory::getId)
                .containsExactly(savedTradeHistory.getId());
    }

    private Trade createTrade() {
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        return Trade.create(
                UUID.randomUUID(),
                TradeParticipants.of(buyerId, "구매자", sellerId, "판매자"),
                TradedItem.of(UUID.randomUUID(), "상품", 10000L)
        );
    }

    @TestConfiguration
    @EnableJpaAuditing
    static class JpaAuditingTestConfig {
    }
}
