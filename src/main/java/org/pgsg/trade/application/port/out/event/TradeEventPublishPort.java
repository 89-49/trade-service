package org.pgsg.trade.application.port.out.event;

import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.domain.model.TradeHistory;

public interface TradeEventPublishPort {

    void publishTradeCreated(Trade trade);

    void publishTradeCompleted(Trade trade);

    void publishTradeCancelled(Trade trade, TradeHistory tradeHistory);
}
