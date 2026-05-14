package org.pgsg.trade.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.pgsg.common.util.SecurityUtil;
import org.pgsg.trade.application.dto.command.CancelTradeCommand;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.result.CancelTradeResult;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.presentation.dto.request.CancelTradeRequest;
import org.pgsg.trade.presentation.dto.response.CancelTradeResponse;
import org.pgsg.trade.presentation.dto.response.CompleteTradeResponse;
import org.pgsg.trade.presentation.dto.response.TradeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TradeController {

    private static final int DEFAULT_PAGE = 0;
    private static final String DEFAULT_SIZE_VALUE = "20";
    private static final int MAX_SIZE = 100;

    private final TradeUseCase tradeUseCase;

    @GetMapping("/api/v1/trades")
    public Page<TradeResponse> getTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_SIZE_VALUE) int size
    ) {
        validatePageRequest(page, size);

        return tradeUseCase.getTrades(PageRequest.of(page, size))
                .map(TradeResponse::from);
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

    @PatchMapping("/api/v1/trades/{tradeId}/cancel")
    public CancelTradeResponse cancelTrade(
            @PathVariable("tradeId") UUID tradeId,
            @RequestBody CancelTradeRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserIdOrThrow();

        CancelTradeCommand command = request.toCommand(tradeId, currentUserId);
        CancelTradeResult result = tradeUseCase.cancelTrade(command);

        return CancelTradeResponse.from(result);
    }

    private void validatePageRequest(int page, int size) {
        if (page < DEFAULT_PAGE) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }

        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
        }
    }
}
