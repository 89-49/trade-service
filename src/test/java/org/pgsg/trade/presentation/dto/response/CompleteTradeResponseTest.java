package org.pgsg.trade.presentation.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.domain.model.ParticipantStatus;
import org.pgsg.trade.domain.model.TradeStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("CompleteTradeResponse 단위 테스트")
class CompleteTradeResponseTest {

    @Test
    @DisplayName("성공: 한 명만 완료한 경우 대기 응답 메시지를 생성한다.")
    void from_NotCompleted_CreatesWaitingMessage() {
        // given
        UUID tradeId = UUID.randomUUID();
        CompleteTradeResult result = new CompleteTradeResult(
                tradeId,
                TradeStatus.TRADING,
                ParticipantStatus.COMPLETED,
                null,
                false,
                false
        );

        // when
        CompleteTradeResponse response = CompleteTradeResponse.from(result);

        // then
        assertAll(
                () -> assertThat(response.tradeId()).isEqualTo(tradeId),
                () -> assertThat(response.tradeStatus()).isEqualTo(TradeStatus.TRADING),
                () -> assertThat(response.tradeCompleted()).isFalse(),
                () -> assertThat(response.eventPublished()).isFalse(),
                () -> assertThat(response.message()).isEqualTo("현재 사용자의 거래 완료 처리가 저장되었습니다. 상대방 완료 처리를 기다립니다.")
        );
    }

    @Test
    @DisplayName("성공: 거래가 완전히 완료된 경우 이벤트 발행 응답 메시지를 생성한다.")
    void from_Completed_CreatesCompletedMessage() {
        // given
        UUID tradeId = UUID.randomUUID();
        CompleteTradeResult result = new CompleteTradeResult(
                tradeId,
                TradeStatus.COMPLETED,
                ParticipantStatus.COMPLETED,
                ParticipantStatus.COMPLETED,
                true,
                true
        );

        // when
        CompleteTradeResponse response = CompleteTradeResponse.from(result);

        // then
        assertAll(
                () -> assertThat(response.tradeId()).isEqualTo(tradeId),
                () -> assertThat(response.tradeStatus()).isEqualTo(TradeStatus.COMPLETED),
                () -> assertThat(response.buyerStatus()).isEqualTo(ParticipantStatus.COMPLETED),
                () -> assertThat(response.sellerStatus()).isEqualTo(ParticipantStatus.COMPLETED),
                () -> assertThat(response.tradeCompleted()).isTrue(),
                () -> assertThat(response.eventPublished()).isTrue(),
                () -> assertThat(response.message()).isEqualTo("구매자와 판매자가 모두 거래 완료 처리되어 거래 완료 이벤트 발행이 요청되었습니다.")
        );
    }
}
