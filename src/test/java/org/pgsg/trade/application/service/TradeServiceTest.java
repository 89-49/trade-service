package org.pgsg.trade.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.application.port.out.TradeHistoryPersistencePort;
import org.pgsg.trade.application.port.out.TradePersistencePort;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeService 단위 테스트")
class TradeServiceTest {

    @Mock
    private TradePersistencePort tradePersistencePort;

    @Mock
    private TradeHistoryPersistencePort tradeHistoryPersistencePort;

    @Test
    @DisplayName("TradeService는 입력 포트인 TradeUseCase를 구현한다.")
    void tradeService_ImplementsTradeUseCase() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort);

        // when & then
        assertThat(tradeService).isInstanceOf(TradeUseCase.class);
    }
}
