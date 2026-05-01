package org.pgsg.trade.application.port.in;

import org.pgsg.trade.application.dto.command.CreateTradeCommand;

public interface TradeUseCase {

    void createTrade(CreateTradeCommand command);

}
