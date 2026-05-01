package org.pgsg.trade.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.trade.application.dto.command.CreateTradeCommand;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.application.port.out.event.TradeEventPublishPort;
import org.pgsg.trade.application.port.out.persistence.TradeHistoryPersistencePort;
import org.pgsg.trade.application.port.out.persistence.TradePersistencePort;
import org.pgsg.trade.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService implements TradeUseCase {

    private final TradePersistencePort tradePersistencePort;
    private final TradeHistoryPersistencePort tradeHistoryPersistencePort;
    private final TradeEventPublishPort tradeEventPublishPort;

    @Override
    @Transactional
    public void createTrade(CreateTradeCommand command) {
        log.info("거래 생성 시작 - reservationId: {}, productId: {}, buyerId: {}, sellerId: {}",
                command.reservationId(), command.productId(), command.buyerId(), command.sellerId());

        Trade trade = Trade.create(
                command.reservationId(),
                TradeParticipants.of(
                        command.buyerId(), command.buyerNickName(),
                        command.sellerId(), command.sellerNickName()
                ),
                TradedItem.of(
                        command.productId(), command.productName(),
                        command.productPrice()
                )
        );

        Trade savedTrade = tradePersistencePort.save(trade);
        log.info("거래 저장 완료 - tradeId: {}, reservationId: {}", savedTrade.getId(), savedTrade.getReservationId());

        TradeHistory tradeHistory = TradeHistory.create(
                savedTrade.getId(), null, TradeStatus.TRADING,
                null, null, null, null
        );
        tradeHistoryPersistencePort.save(tradeHistory);
        log.debug("거래 이력 저장 완료 - tradeId: {}, status: {}", savedTrade.getId(), TradeStatus.TRADING);

        tradeEventPublishPort.publishTradeCreated(savedTrade);
        log.info("거래 생성 이벤트 발행 요청 완료 - tradeId: {}", savedTrade.getId());
    }
}
