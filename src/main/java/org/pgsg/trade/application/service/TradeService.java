package org.pgsg.trade.application.service;

import lombok.RequiredArgsConstructor;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.application.port.out.TradeHistoryPersistencePort;
import org.pgsg.trade.application.port.out.TradePersistencePort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService implements TradeUseCase {

    private final TradePersistencePort tradePersistencePort;
    private final TradeHistoryPersistencePort tradeHistoryPersistencePort;


}
