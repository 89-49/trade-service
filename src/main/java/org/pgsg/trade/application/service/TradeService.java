package org.pgsg.trade.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.command.CreateTradeCommand;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.application.port.out.event.TradeEventPublishPort;
import org.pgsg.trade.application.port.out.persistence.TradeHistoryPersistencePort;
import org.pgsg.trade.application.port.out.persistence.TradePersistencePort;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.pgsg.trade.domain.exception.TradeServiceException;
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
        if (command == null) {
            throw new IllegalArgumentException("CreateTradeCommand must not be null");
        }

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

    @Override
    @Transactional
    public CompleteTradeResult completeTrade(CompleteTradeCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CompleteTradeCommand must not be null");
        }

        log.info("거래 완료 요청 시작 - tradeId: {}, participantId: {}", command.tradeId(), command.participantId());

        if (command.tradeId() == null) {
            throw new TradeServiceException(TradeErrorCode.TRADE_ID_REQUIRED);
        }

        Trade trade = tradePersistencePort.findById(command.tradeId())
                .orElseThrow(() -> new TradeServiceException(TradeErrorCode.TRADE_NOT_FOUND));

        TradeStatus previousStatus = trade.getStatus();
        boolean completed = trade.completeBy(command.participantId());
        Trade savedTrade = tradePersistencePort.save(trade);

        log.info("거래 참여자 완료 처리 완료 - tradeId: {}, participantId: {}, buyerStatus: {}, sellerStatus: {}",
                savedTrade.getId(), command.participantId(), savedTrade.getBuyerStatus(), savedTrade.getSellerStatus());

        if (!completed) {
            log.debug("거래 완료 이벤트 발행 대기 - tradeId: {}", savedTrade.getId());
            return CompleteTradeResult.from(savedTrade, false);
        }

        TradeHistory tradeHistory = TradeHistory.create(
                savedTrade.getId(), previousStatus, TradeStatus.COMPLETED,
                null, null, null, null
        );
        tradeHistoryPersistencePort.save(tradeHistory);
        log.info("거래 완료 이력 저장 완료 - tradeId: {}", savedTrade.getId());

        tradeEventPublishPort.publishTradeCompleted(savedTrade);
        log.info("거래 완료 이벤트 발행 요청 완료 - tradeId: {}", savedTrade.getId());

        return CompleteTradeResult.from(savedTrade, true);
    }
}
