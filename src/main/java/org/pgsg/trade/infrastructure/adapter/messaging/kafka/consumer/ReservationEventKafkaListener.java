package org.pgsg.trade.infrastructure.adapter.messaging.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.pgsg.common.messaging.annotation.IdempotentConsumer;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.pgsg.trade.domain.exception.TradeServiceException;
import org.pgsg.trade.infrastructure.adapter.messaging.kafka.event.ReservationCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservationEventKafkaListener {

    private final ObjectMapper objectMapper;
    private final TradeUseCase tradeUseCase;

    @IdempotentConsumer("trade-service:reservation-completed")
    @KafkaListener(
            topics = "${topics.reservation.completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(ConsumerRecord<String, String> record) {
        log.info("예약 완료 이벤트 수신 - topic: {}, partition: {}, offset: {}, key: {}",
                record.topic(), record.partition(), record.offset(), record.key());

        try {
            ReservationCompletedEvent event =
                objectMapper.readValue(record.value(), ReservationCompletedEvent.class);

            log.debug("예약 완료 이벤트 역직렬화 완료 - reservationId: {}, productId: {}",
                    event.reservationId(), event.productId());

            tradeUseCase.createTrade(event.toCommand());
            log.info("예약 완료 이벤트 처리 완료 - reservationId: {}", event.reservationId());
        } catch (JsonProcessingException e) {
            log.error("예약 완료 이벤트 역직렬화 실패 - topic: {}, partition: {}, offset: {}",
                    record.topic(), record.partition(), record.offset(), e);
            throw new TradeServiceException(TradeErrorCode.RESERVATION_EVENT_DESERIALIZATION_FAILED);
        } catch (TradeServiceException e) {
            log.error("예약 완료 이벤트 처리 실패 - topic: {}, partition: {}, offset: {}, errorKey: {}",
                    record.topic(), record.partition(), record.offset(), e.getErrorCode().getErrorKey(), e);
            throw e;
        } catch (Exception e) {
            log.error("예약 완료 이벤트 처리 중 알 수 없는 예외 발생 - topic: {}, partition: {}, offset: {}",
                    record.topic(), record.partition(), record.offset(), e);
            throw new TradeServiceException(TradeErrorCode.RESERVATION_EVENT_PROCESS_FAILED);
        }
    }
}
