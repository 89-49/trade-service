package org.pgsg.trade.application.port.out.event;

import org.pgsg.trade.domain.model.Trade;

public interface TradeEventPublishPort {

    void publishTradeCreated(Trade trade);

}
