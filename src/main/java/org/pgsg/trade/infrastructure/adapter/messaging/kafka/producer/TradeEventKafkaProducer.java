package org.pgsg.trade.infrastructure.adapter.messaging.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.common.event.Events;
import org.pgsg.common.event.OutboxEvent;
import org.pgsg.trade.application.port.out.event.TradeEventPublishPort;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.pgsg.trade.domain.exception.TradeServiceException;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.infrastructure.adapter.messaging.kafka.event.TradeCompletedEvent;
import org.pgsg.trade.infrastructure.adapter.messaging.kafka.event.TradeCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;


@Component
@Slf4j
@RequiredArgsConstructor
public class TradeEventKafkaProducer implements TradeEventPublishPort {

    @Value("${topics.trade.created}")
    private String tradeCreatedTopic;
    @Value("${topics.trade.completed}")
    private String tradeCompletedTopic;

    @Override
    public void publishTradeCreated(Trade trade) {
        TradeCreatedEvent event = TradeCreatedEvent.create(trade);

        try {
            log.info("거래 생성 Outbox 이벤트 등록 요청 - tradeId: {}, topic: {}", trade.getId(), tradeCreatedTopic);

            Events.trigger(new OutboxEvent(
                    trade.getId(),
                    "TRADE",
                    trade.getId(),
                    tradeCreatedTopic,
                    event
            ));
            log.info("거래 생성 Outbox 이벤트 등록 요청 완료 - tradeId: {}, topic: {}", trade.getId(), tradeCreatedTopic);
        } catch (RuntimeException e) {
            log.error("거래 생성 Outbox 이벤트 등록 요청 실패 - tradeId: {}, topic: {}",
                    trade.getId(), tradeCreatedTopic, e);
            throw new TradeServiceException(TradeErrorCode.TRADE_EVENT_PUBLISH_FAILED);
        }
    }

    @Override
    public void publishTradeCompleted(Trade trade) {
        TradeCompletedEvent event = TradeCompletedEvent.create(trade);
        UUID correlationId = UUID.nameUUIDFromBytes(("trade-completed:" + trade.getId()).getBytes(StandardCharsets.UTF_8));

        try {
            log.info("거래 완료 Outbox 이벤트 등록 요청 - tradeId: {}, correlationId: {}, topic: {}",
                    trade.getId(), correlationId, tradeCompletedTopic);

            Events.trigger(new OutboxEvent(
                    correlationId,
                    "TRADE",
                    trade.getId(),
                    tradeCompletedTopic,
                    event
            ));
            log.info("거래 완료 Outbox 이벤트 등록 요청 완료 - tradeId: {}, correlationId: {}, topic: {}",
                    trade.getId(), correlationId, tradeCompletedTopic);
        } catch (RuntimeException e) {
            log.error("거래 완료 Outbox 이벤트 등록 요청 실패 - tradeId: {}, correlationId: {}, topic: {}",
                    trade.getId(), correlationId, tradeCompletedTopic, e);
            throw new TradeServiceException(TradeErrorCode.TRADE_EVENT_PUBLISH_FAILED);
        }
    }

}
