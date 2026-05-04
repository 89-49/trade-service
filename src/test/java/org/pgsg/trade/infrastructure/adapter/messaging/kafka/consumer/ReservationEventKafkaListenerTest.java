package org.pgsg.trade.infrastructure.adapter.messaging.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.trade.application.dto.command.CreateTradeCommand;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.pgsg.trade.domain.exception.TradeServiceException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationEventKafkaListener 단위 테스트")
class ReservationEventKafkaListenerTest {

    private static final UUID RESERVATION_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TradeUseCase tradeUseCase;

    @Test
    @DisplayName("성공: 예약 완료 JSON 객체 메시지를 거래 생성 command로 변환한다.")
    void listen_ObjectJson_CreatesTrade() {
        ReservationEventKafkaListener listener = new ReservationEventKafkaListener(objectMapper, tradeUseCase);

        listener.listen(record(reservationCompletedJson()));

        assertCreateTradeCommand();
    }

    @Test
    @DisplayName("실패: 빈 메시지는 예약 이벤트 역직렬화 실패 예외가 발생한다.")
    void listen_BlankPayload_ThrowsDeserializationException() {
        ReservationEventKafkaListener listener = new ReservationEventKafkaListener(objectMapper, tradeUseCase);

        assertThatThrownBy(() -> listener.listen(record(" ")))
                .isInstanceOf(TradeServiceException.class)
                .extracting(e -> ((TradeServiceException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.RESERVATION_EVENT_DESERIALIZATION_FAILED);
    }

    @Test
    @DisplayName("실패: JSON 문자열로 감싸진 예약 완료 메시지는 역직렬화 실패 예외가 발생한다.")
    void listen_StringWrappedJson_ThrowsDeserializationException() throws Exception {
        ReservationEventKafkaListener listener = new ReservationEventKafkaListener(objectMapper, tradeUseCase);
        String stringWrappedJson = objectMapper.writeValueAsString(reservationCompletedJson());

        assertThatThrownBy(() -> listener.listen(record(stringWrappedJson)))
                .isInstanceOf(TradeServiceException.class)
                .extracting(e -> ((TradeServiceException) e).getErrorCode())
                .isEqualTo(TradeErrorCode.RESERVATION_EVENT_DESERIALIZATION_FAILED);
    }

    private void assertCreateTradeCommand() {
        ArgumentCaptor<CreateTradeCommand> commandCaptor = ArgumentCaptor.forClass(CreateTradeCommand.class);
        verify(tradeUseCase).createTrade(commandCaptor.capture());

        CreateTradeCommand command = commandCaptor.getValue();
        assertThat(command.reservationId()).isEqualTo(RESERVATION_ID);
        assertThat(command.productId()).isEqualTo(PRODUCT_ID);
        assertThat(command.productName()).isEqualTo("타임딜 특가 상품");
        assertThat(command.productPrice()).isEqualTo(50000L);
        assertThat(command.sellerId()).isEqualTo(SELLER_ID);
        assertThat(command.sellerNickName()).isEqualTo("임시 판매자");
        assertThat(command.buyerId()).isEqualTo(BUYER_ID);
        assertThat(command.buyerNickName()).isEqualTo("minseong");
    }

    private ConsumerRecord<String, String> record(String value) {
        return new ConsumerRecord<>("prod-reservation-completed", 0, 18L, RESERVATION_ID.toString(), value);
    }

    private String reservationCompletedJson() {
        return """
                {
                  "reservationId": "%s",
                  "productId": "%s",
                  "productName": "타임딜 특가 상품",
                  "productPrice": 50000,
                  "sellerId": "%s",
                  "sellerNickName": "임시 판매자",
                  "buyerId": "%s",
                  "buyerNickName": "minseong"
                }
                """.formatted(RESERVATION_ID, PRODUCT_ID, SELLER_ID, BUYER_ID);
    }
}
