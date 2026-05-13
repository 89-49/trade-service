package org.pgsg.trade.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.trade.application.dto.command.CancelTradeCommand;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.command.CreateTradeCommand;
import org.pgsg.trade.application.dto.result.CancelTradeResult;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.application.dto.result.TradeResult;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.application.port.out.event.TradeEventPublishPort;
import org.pgsg.trade.application.port.out.persistence.TradeHistoryPersistencePort;
import org.pgsg.trade.application.port.out.persistence.TradePersistencePort;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.pgsg.trade.domain.exception.TradeServiceException;
import org.pgsg.trade.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService implements TradeUseCase {

    private static final int COMPLETE_TRADE_MAX_RETRY_COUNT = 3;

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
    @Transactional(readOnly = true)
    public Page<TradeResult> getTrades(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }

        return tradePersistencePort.findAll(pageable)
                .map(TradeResult::from);
    }

    @Override
    @Transactional(readOnly = true)
    public TradeResult getTrade(UUID tradeId) {
        if (tradeId == null) {
            throw new TradeServiceException(TradeErrorCode.TRADE_ID_REQUIRED);
        }

        return tradePersistencePort.findById(tradeId)
                .map(TradeResult::from)
                .orElseThrow(() -> new TradeServiceException(TradeErrorCode.TRADE_NOT_FOUND));
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

        // TODO: 실패 시 재시도 로직은 트랜잭션 밖에서 수행하도록 리팩토링 필요
        for (int attempt = 1; attempt <= COMPLETE_TRADE_MAX_RETRY_COUNT; attempt++) {
            try {
                return completeTradeWithOptimisticLock(command);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("거래 완료 처리 중 낙관적 락 충돌 발생 - tradeId: {}, participantId: {}, attempt: {}/{}",
                        command.tradeId(), command.participantId(), attempt, COMPLETE_TRADE_MAX_RETRY_COUNT, e);
            }
        }

        throw new TradeServiceException(TradeErrorCode.TRADE_CONCURRENT_UPDATE_FAILED);
    }

    private CompleteTradeResult completeTradeWithOptimisticLock(CompleteTradeCommand command) {
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

    @Override
    @Transactional
    public CancelTradeResult cancelTrade(CancelTradeCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CancelTradeCommand must not be null");
        }

        log.info("거래 취소 요청 시작 - tradeId: {}, participantId: {}", command.tradeId(), command.participantId());

        if (command.tradeId() == null) {
            throw new TradeServiceException(TradeErrorCode.TRADE_ID_REQUIRED);
        }

        // TODO: 재시도 로직을 트랜잭션 밖에서 수행하도록 리팩토링 필요
        for (int attempt = 1; attempt <= COMPLETE_TRADE_MAX_RETRY_COUNT; attempt++) {
            try {
                return cancelTradeWithOptimisticLock(command);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("거래 취소 처리 중 낙관적 락 충돌 발생 - tradeId: {}, participantId: {}, attempt: {}/{}",
                        command.tradeId(), command.participantId(), attempt, COMPLETE_TRADE_MAX_RETRY_COUNT, e);
            }
        }

        throw new TradeServiceException(TradeErrorCode.TRADE_CONCURRENT_UPDATE_FAILED);
    }

    private CancelTradeResult cancelTradeWithOptimisticLock(CancelTradeCommand command) {
        Trade trade = tradePersistencePort.findById(command.tradeId())
                .orElseThrow(() -> new TradeServiceException(TradeErrorCode.TRADE_NOT_FOUND));

        TradeStatus previousStatus = trade.getStatus();
        boolean cancelled = trade.cancelBy(command.participantId());
        Trade savedTrade = tradePersistencePort.save(trade);

        log.info("거래 참여자 취소 처리 완료 - tradeId: {}, participantId: {}, buyerStatus: {}, sellerStatus: {}",
                savedTrade.getId(), command.participantId(), savedTrade.getBuyerStatus(), savedTrade.getSellerStatus());

        TradeHistory tradeHistory = TradeHistory.create(
                savedTrade.getId(), previousStatus, TradeStatus.CANCELLED,
                command.cancelledBy(), command.participantId(), command.cancelReasonType(), command.cancelReasonDetail()
        );
        TradeHistory savedTradeHistory = tradeHistoryPersistencePort.save(tradeHistory);
        log.info("거래 취소 이력 저장 완료 - tradeId: {}", savedTrade.getId());


        // 취소 이벤트 발행 대기 or 발행
        if (!cancelled) {
            log.debug("거래 취소 이벤트 발행 대기 - tradeId: {}", savedTrade.getId());
            return CancelTradeResult.from(savedTrade, savedTradeHistory, false);
        }
        tradeEventPublishPort.publishTradeCancelled(trade, savedTradeHistory);
        log.info("거래 취소 이벤트 발행 요청 완료 - tradeId: {}", savedTrade.getId());
        return CancelTradeResult.from(savedTrade, savedTradeHistory, true);
    }
}
