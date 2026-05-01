package org.pgsg.trade.infrastructure.adapter.messaging.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.common.event.Events;
import org.pgsg.common.event.OutboxEvent;
import org.pgsg.trade.application.port.out.event.TradeEventPublishPort;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.pgsg.trade.domain.exception.TradeServiceException;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.infrastructure.adapter.messaging.kafka.event.TradeCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class TradeEventKafkaProducer implements TradeEventPublishPort {

    @Value("${topics.trade.created}")
    private String tradeCreatedTopic;

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

}
