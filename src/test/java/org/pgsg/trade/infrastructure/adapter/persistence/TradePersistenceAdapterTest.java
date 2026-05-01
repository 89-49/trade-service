package org.pgsg.trade.infrastructure.adapter.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.domain.model.TradeParticipants;
import org.pgsg.trade.domain.model.TradedItem;
import org.pgsg.trade.infrastructure.persistence.repository.TradeJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradePersistenceAdapter 단위 테스트")
class TradePersistenceAdapterTest {

    @Mock
    private TradeJpaRepository tradeJpaRepository;

    @InjectMocks
    private TradePersistenceAdapter adapter;

    @Test
    @DisplayName("성공: save 호출 시 TradeJpaRepository에 저장을 위임한다.")
    void save_DelegatesToJpaRepository() {
        // given
        Trade trade = createTrade();
        when(tradeJpaRepository.save(trade)).thenReturn(trade);

        // when
        Trade savedTrade = adapter.save(trade);

        // then
        assertAll(
                () -> assertThat(savedTrade).isSameAs(trade),
                () -> verify(tradeJpaRepository).save(trade)
        );
    }

    @Test
    @DisplayName("성공: findById 호출 시 TradeJpaRepository에 조회를 위임한다.")
    void findById_DelegatesToJpaRepository() {
        // given
        Trade trade = createTrade();
        UUID tradeId = trade.getId();
        when(tradeJpaRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        // when
        Optional<Trade> foundTrade = adapter.findById(tradeId);

        // then
        assertAll(
                () -> assertThat(foundTrade).containsSame(trade),
                () -> verify(tradeJpaRepository).findById(tradeId)
        );
    }

    @Test
    @DisplayName("성공: findByReservationId 호출 시 TradeJpaRepository에 조회를 위임한다.")
    void findByReservationId_DelegatesToJpaRepository() {
        // given
        Trade trade = createTrade();
        UUID reservationId = trade.getReservationId();
        when(tradeJpaRepository.findByReservationId(reservationId)).thenReturn(List.of(trade));

        // when
        List<Trade> foundTrade = adapter.findByReservationId(reservationId);

        // then
        assertAll(
                () -> assertThat(foundTrade.getFirst()).isEqualTo(trade),
                () -> verify(tradeJpaRepository).findByReservationId(reservationId)
        );
    }

    @Test
    @DisplayName("성공: existsByReservationId 호출 시 TradeJpaRepository에 존재 여부 확인을 위임한다.")
    void existsByReservationId_DelegatesToJpaRepository() {
        // given
        UUID reservationId = UUID.randomUUID();
        when(tradeJpaRepository.existsByReservationId(reservationId)).thenReturn(true);

        // when
        boolean exists = adapter.existsByReservationId(reservationId);

        // then
        assertAll(
                () -> assertThat(exists).isTrue(),
                () -> verify(tradeJpaRepository).existsByReservationId(reservationId)
        );
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
}
