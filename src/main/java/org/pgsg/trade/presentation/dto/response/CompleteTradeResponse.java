package org.pgsg.trade.presentation.dto.response;

import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.domain.model.ParticipantStatus;
import org.pgsg.trade.domain.model.TradeStatus;

import java.util.UUID;

public record CompleteTradeResponse(
        UUID tradeId,
        TradeStatus tradeStatus,
        ParticipantStatus buyerStatus,
        ParticipantStatus sellerStatus,
        boolean tradeCompleted,
        boolean eventPublished,
        String message
) {

    public static CompleteTradeResponse from(CompleteTradeResult result) {
        String message = result.tradeCompleted()
                ? "구매자와 판매자가 모두 거래 완료 처리되어 거래 완료 이벤트 발행이 요청되었습니다."
                : "현재 사용자의 거래 완료 처리가 저장되었습니다. 상대방 완료 처리를 기다립니다.";

        return new CompleteTradeResponse(
                result.tradeId(),
                result.tradeStatus(),
                result.buyerStatus(),
                result.sellerStatus(),
                result.tradeCompleted(),
                result.eventPublished(),
                message
        );
    }
}
