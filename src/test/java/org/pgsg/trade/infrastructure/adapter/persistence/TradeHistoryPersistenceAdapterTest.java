package org.pgsg.trade.infrastructure.adapter.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.trade.domain.model.TradeHistory;
import org.pgsg.trade.domain.model.TradeStatus;
import org.pgsg.trade.infrastructure.persistence.repository.TradeHistoryJpaRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeHistoryPersistenceAdapter 단위 테스트")
class TradeHistoryPersistenceAdapterTest {

    @Mock
    private TradeHistoryJpaRepository tradeHistoryJpaRepository;

    @InjectMocks
    private TradeHistoryPersistenceAdapter adapter;

    @Test
    @DisplayName("save 호출 시 TradeHistoryJpaRepository에 저장을 위임한다.")
    void save_DelegatesToJpaRepository() {
        // given
        TradeHistory tradeHistory = createTradeHistory();
        when(tradeHistoryJpaRepository.save(tradeHistory)).thenReturn(tradeHistory);

        // when
        TradeHistory savedTradeHistory = adapter.save(tradeHistory);

        // then
        assertAll(
                () -> assertThat(savedTradeHistory).isSameAs(tradeHistory),
                () -> verify(tradeHistoryJpaRepository).save(tradeHistory)
        );
    }

    @Test
    @DisplayName("findAllByTradeId 호출 시 생성일 오름차순 JPA 조회 메서드에 위임한다.")
    void findAllByTradeId_DelegatesToJpaRepository() {
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
        when(tradeHistoryJpaRepository.findAllByTradeIdOrderByCreatedAtAsc(tradeId))
                .thenReturn(List.of(tradeHistory));

        // when
        List<TradeHistory> tradeHistories = adapter.findAllByTradeId(tradeId);

        // then
        assertAll(
                () -> assertThat(tradeHistories).containsExactly(tradeHistory),
                () -> verify(tradeHistoryJpaRepository).findAllByTradeIdOrderByCreatedAtAsc(tradeId)
        );
    }

    private TradeHistory createTradeHistory() {
        return TradeHistory.create(
                UUID.randomUUID(),
                null,
                TradeStatus.TRADING,
                null,
                null,
                null,
                null
        );
    }
}
