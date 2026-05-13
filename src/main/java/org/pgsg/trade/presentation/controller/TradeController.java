package org.pgsg.trade.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.pgsg.common.util.SecurityUtil;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.presentation.dto.response.CompleteTradeResponse;
import org.pgsg.trade.presentation.dto.response.TradeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TradeController {

    private final TradeUseCase tradeUseCase;

    @GetMapping("/api/v1/trades")
    public List<TradeResponse> getTrades() {
        return tradeUseCase.getTrades().stream()
                .map(TradeResponse::from)
                .toList();
    }

    @GetMapping("/api/v1/trades/{tradeId}")
    public TradeResponse getTrade(@PathVariable UUID tradeId) {
        return TradeResponse.from(tradeUseCase.getTrade(tradeId));
    }

    @PatchMapping("/api/v1/trades/{tradeId}/complete")
    public CompleteTradeResponse completeTrade(@PathVariable UUID tradeId) {
        UUID currentUserId = SecurityUtil.getCurrentUserIdOrThrow();

        CompleteTradeCommand command = new CompleteTradeCommand(tradeId, currentUserId);
        CompleteTradeResult result = tradeUseCase.completeTrade(command);

        return CompleteTradeResponse.from(result);
    }
}
