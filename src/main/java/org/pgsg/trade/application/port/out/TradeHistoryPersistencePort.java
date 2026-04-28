package org.pgsg.trade.application.port.out;

import org.pgsg.trade.domain.model.TradeHistory;

import java.util.List;
import java.util.UUID;

public interface TradeHistoryPersistencePort {

    TradeHistory save(TradeHistory tradeHistory);

    List<TradeHistory> findAllByTradeId(UUID tradeId);
}
