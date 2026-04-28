package org.pgsg.trade.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TradeController {

    private final TradeUseCase tradeUseCase;
}
