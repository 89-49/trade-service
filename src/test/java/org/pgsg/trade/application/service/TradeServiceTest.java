package org.pgsg.trade.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.application.dto.result.TradeResult;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.application.port.out.event.TradeEventPublishPort;
import org.pgsg.trade.application.port.out.persistence.TradeHistoryPersistencePort;
import org.pgsg.trade.application.port.out.persistence.TradePersistencePort;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.pgsg.trade.domain.exception.TradeServiceException;
import org.pgsg.trade.domain.model.ParticipantStatus;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.domain.model.TradeHistory;
import org.pgsg.trade.domain.model.TradeParticipants;
import org.pgsg.trade.domain.model.TradeStatus;
import org.pgsg.trade.domain.model.TradedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeService 단위 테스트")
class TradeServiceTest {

    private static final UUID TRADE_ID = UUID.randomUUID();
    private static final UUID RESERVATION_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Mock
    private TradePersistencePort tradePersistencePort;

    @Mock
    private TradeHistoryPersistencePort tradeHistoryPersistencePort;

    @Mock
    private TradeEventPublishPort tradeEventPublishPort;

    @Test
    @DisplayName("성공: TradeService는 입력 포트인 TradeUseCase를 구현한다.")
    void tradeService_ImplementsTradeUseCase() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);

        // when & then
        assertThat(tradeService).isInstanceOf(TradeUseCase.class);
    }

    @Test
    @DisplayName("실패: 거래 생성 command가 null이면 명확한 예외가 발생한다.")
    void createTrade_NullCommand_ThrowsException() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);

        // when & then
        assertThatThrownBy(() -> tradeService.createTrade(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CreateTradeCommand must not be null");

        verify(tradePersistencePort, never()).save(any(Trade.class));
        verify(tradeHistoryPersistencePort, never()).save(any(TradeHistory.class));
        verify(tradeEventPublishPort, never()).publishTradeCreated(any(Trade.class));
    }

    @Test
    @DisplayName("성공: 거래 목록을 조회 결과 DTO로 변환한다.")
    void getTrades_ReturnsTradeResults() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);
        Trade trade = createTrade();
        Pageable pageable = PageRequest.of(0, 20);
        when(tradePersistencePort.findAll(pageable)).thenReturn(new PageImpl<>(List.of(trade), pageable, 1));

        // when
        Page<TradeResult> results = tradeService.getTrades(pageable);

        // then
        assertAll(
                () -> assertThat(results.getContent()).hasSize(1),
                () -> assertThat(results.getContent().get(0).tradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(results.getContent().get(0).reservationId()).isEqualTo(RESERVATION_ID),
                () -> assertThat(results.getContent().get(0).buyerId()).isEqualTo(BUYER_ID),
                () -> assertThat(results.getContent().get(0).sellerId()).isEqualTo(SELLER_ID),
                () -> assertThat(results.getContent().get(0).productId()).isEqualTo(PRODUCT_ID),
                () -> assertThat(results.getTotalElements()).isEqualTo(1)
        );
        verify(tradePersistencePort).findAll(pageable);
    }

    @Test
    @DisplayName("실패: 거래 목록 조회 pageable이 null이면 명확한 예외가 발생한다.")
    void getTrades_NullPageable_ThrowsException() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);

        // when & then
        assertThatThrownBy(() -> tradeService.getTrades(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pageable must not be null");
    }

    @Test
    @DisplayName("성공: 거래 단건을 조회 결과 DTO로 변환한다.")
    void getTrade_ReturnsTradeResult() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);
        Trade trade = createTrade();
        when(tradePersistencePort.findById(TRADE_ID)).thenReturn(Optional.of(trade));

        // when
        TradeResult result = tradeService.getTrade(TRADE_ID);

        // then
        assertAll(
                () -> assertThat(result.tradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(result.tradeStatus()).isEqualTo(TradeStatus.TRADING),
                () -> assertThat(result.productName()).isEqualTo("테스트 상품")
        );
        verify(tradePersistencePort).findById(TRADE_ID);
    }

    @Test
    @DisplayName("실패: 거래 단건 조회 ID가 null이면 거래 ID 필수 예외가 발생한다.")
    void getTrade_NullTradeId_ThrowsException() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);

        // when & then
        assertThatThrownBy(() -> tradeService.getTrade(null))
                .isInstanceOf(TradeServiceException.class)
                .extracting(e -> ((TradeServiceException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ID_REQUIRED);

        verify(tradePersistencePort, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("실패: 거래 단건 조회 대상이 없으면 거래 없음 예외가 발생한다.")
    void getTrade_NotFound_ThrowsException() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);
        when(tradePersistencePort.findById(TRADE_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tradeService.getTrade(TRADE_ID))
                .isInstanceOf(TradeServiceException.class)
                .extracting(e -> ((TradeServiceException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);
    }

    @Test
    @DisplayName("성공: 구매자만 완료하면 참여자 상태만 저장하고 거래 완료 이벤트는 발행하지 않는다.")
    void completeTrade_OnlyBuyerCompleted_DoesNotPublishCompletedEvent() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);
        Trade trade = createTrade();

        when(tradePersistencePort.findById(TRADE_ID)).thenReturn(Optional.of(trade));
        when(tradePersistencePort.save(trade)).thenReturn(trade);

        // when
        CompleteTradeResult result = tradeService.completeTrade(new CompleteTradeCommand(TRADE_ID, BUYER_ID));

        // then
        assertAll(
                () -> assertThat(result.tradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(result.tradeStatus()).isEqualTo(TradeStatus.TRADING),
                () -> assertThat(result.buyerStatus()).isEqualTo(ParticipantStatus.COMPLETED),
                () -> assertThat(result.sellerStatus()).isEqualTo(ParticipantStatus.TRADING),
                () -> assertThat(result.tradeCompleted()).isFalse(),
                () -> assertThat(result.eventPublished()).isFalse()
        );
        verify(tradePersistencePort).save(trade);
        verify(tradeHistoryPersistencePort, never()).save(any(TradeHistory.class));
        verify(tradeEventPublishPort, never()).publishTradeCompleted(any(Trade.class));
    }

    @Test
    @DisplayName("성공: 구매자와 판매자가 모두 완료하면 거래 이력을 저장하고 거래 완료 이벤트를 발행한다.")
    void completeTrade_AllParticipantsCompleted_PublishesCompletedEvent() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);
        Trade trade = createTrade();
        trade.completeBy(BUYER_ID);

        when(tradePersistencePort.findById(TRADE_ID)).thenReturn(Optional.of(trade));
        when(tradePersistencePort.save(trade)).thenReturn(trade);

        // when
        CompleteTradeResult result = tradeService.completeTrade(new CompleteTradeCommand(TRADE_ID, SELLER_ID));

        // then
        ArgumentCaptor<TradeHistory> tradeHistoryCaptor = ArgumentCaptor.forClass(TradeHistory.class);
        verify(tradeHistoryPersistencePort).save(tradeHistoryCaptor.capture());
        verify(tradeEventPublishPort).publishTradeCompleted(trade);

        TradeHistory savedHistory = tradeHistoryCaptor.getValue();
        assertAll(
                () -> assertThat(result.tradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(result.tradeStatus()).isEqualTo(TradeStatus.COMPLETED),
                () -> assertThat(result.buyerStatus()).isEqualTo(ParticipantStatus.COMPLETED),
                () -> assertThat(result.sellerStatus()).isEqualTo(ParticipantStatus.COMPLETED),
                () -> assertThat(result.tradeCompleted()).isTrue(),
                () -> assertThat(result.eventPublished()).isTrue(),
                () -> assertThat(savedHistory.getTradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(savedHistory.getPreviousStatus()).isEqualTo(TradeStatus.TRADING),
                () -> assertThat(savedHistory.getNewStatus()).isEqualTo(TradeStatus.COMPLETED)
        );
    }

    @Test
    @DisplayName("성공: 거래 완료 저장 중 낙관적 락 충돌이 발생하면 재조회 후 재시도한다.")
    void completeTrade_OptimisticLockingFailure_RetriesAndPublishesCompletedEvent() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);
        Trade staleTrade = createTrade();
        Trade retriedTrade = createTrade();
        retriedTrade.completeBy(BUYER_ID);

        when(tradePersistencePort.findById(TRADE_ID))
                .thenReturn(Optional.of(staleTrade))
                .thenReturn(Optional.of(retriedTrade));
        when(tradePersistencePort.save(staleTrade))
                .thenThrow(new ObjectOptimisticLockingFailureException(Trade.class, TRADE_ID));
        when(tradePersistencePort.save(retriedTrade)).thenReturn(retriedTrade);

        // when
        CompleteTradeResult result = tradeService.completeTrade(new CompleteTradeCommand(TRADE_ID, SELLER_ID));

        // then
        assertAll(
                () -> assertThat(result.tradeStatus()).isEqualTo(TradeStatus.COMPLETED),
                () -> assertThat(result.buyerStatus()).isEqualTo(ParticipantStatus.COMPLETED),
                () -> assertThat(result.sellerStatus()).isEqualTo(ParticipantStatus.COMPLETED),
                () -> assertThat(result.tradeCompleted()).isTrue(),
                () -> assertThat(result.eventPublished()).isTrue()
        );
        verify(tradePersistencePort, times(2)).findById(TRADE_ID);
        verify(tradePersistencePort).save(staleTrade);
        verify(tradePersistencePort).save(retriedTrade);
        verify(tradeEventPublishPort).publishTradeCompleted(retriedTrade);
    }

    @Test
    @DisplayName("실패: 낙관적 락 충돌 재시도를 모두 소진하면 동시 수정 실패 예외가 발생한다.")
    void completeTrade_OptimisticLockingFailureExhausted_ThrowsException() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);
        when(tradePersistencePort.findById(TRADE_ID))
                .thenReturn(Optional.of(createTrade()))
                .thenReturn(Optional.of(createTrade()))
                .thenReturn(Optional.of(createTrade()));
        when(tradePersistencePort.save(any(Trade.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Trade.class, TRADE_ID));

        // when & then
        assertThatThrownBy(() -> tradeService.completeTrade(new CompleteTradeCommand(TRADE_ID, BUYER_ID)))
                .isInstanceOf(TradeServiceException.class)
                .extracting(e -> ((TradeServiceException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_CONCURRENT_UPDATE_FAILED);

        verify(tradePersistencePort, times(3)).findById(TRADE_ID);
        verify(tradePersistencePort, times(3)).save(any(Trade.class));
        verify(tradeHistoryPersistencePort, never()).save(any(TradeHistory.class));
        verify(tradeEventPublishPort, never()).publishTradeCompleted(any(Trade.class));
    }

    @Test
    @DisplayName("실패: 거래 ID가 null이면 거래 ID 필수 예외가 발생한다.")
    void completeTrade_NullTradeId_ThrowsException() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);

        // when & then
        assertThatThrownBy(() -> tradeService.completeTrade(new CompleteTradeCommand(null, BUYER_ID)))
                .isInstanceOf(TradeServiceException.class)
                .extracting(e -> ((TradeServiceException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_ID_REQUIRED);

        verify(tradePersistencePort, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("실패: 거래 완료 command가 null이면 명확한 예외가 발생한다.")
    void completeTrade_NullCommand_ThrowsException() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);

        // when & then
        assertThatThrownBy(() -> tradeService.completeTrade(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CompleteTradeCommand must not be null");

        verify(tradePersistencePort, never()).findById(any(UUID.class));
        verify(tradePersistencePort, never()).save(any(Trade.class));
        verify(tradeHistoryPersistencePort, never()).save(any(TradeHistory.class));
        verify(tradeEventPublishPort, never()).publishTradeCompleted(any(Trade.class));
    }

    @Test
    @DisplayName("실패: 거래를 찾을 수 없으면 거래 없음 예외가 발생한다.")
    void completeTrade_TradeNotFound_ThrowsException() {
        // given
        TradeService tradeService = new TradeService(tradePersistencePort, tradeHistoryPersistencePort, tradeEventPublishPort);
        when(tradePersistencePort.findById(TRADE_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tradeService.completeTrade(new CompleteTradeCommand(TRADE_ID, BUYER_ID)))
                .isInstanceOf(TradeServiceException.class)
                .extracting(e -> ((TradeServiceException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.TRADE_NOT_FOUND);

        verify(tradePersistencePort, never()).save(any(Trade.class));
        verify(tradeEventPublishPort, never()).publishTradeCompleted(any(Trade.class));
    }

    private Trade createTrade() {
        Trade trade = Trade.create(
                RESERVATION_ID,
                TradeParticipants.of(BUYER_ID, "구매자", SELLER_ID, "판매자"),
                TradedItem.of(PRODUCT_ID, "테스트 상품", 10000L)
        );
        ReflectionTestUtils.setField(trade, "id", TRADE_ID);
        return trade;
    }
}
