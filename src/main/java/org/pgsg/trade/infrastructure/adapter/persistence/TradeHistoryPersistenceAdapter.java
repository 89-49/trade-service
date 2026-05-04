package org.pgsg.trade.infrastructure.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.pgsg.trade.application.port.out.persistence.TradeHistoryPersistencePort;
import org.pgsg.trade.domain.model.TradeHistory;
import org.pgsg.trade.infrastructure.persistence.repository.TradeHistoryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TradeHistoryPersistenceAdapter implements TradeHistoryPersistencePort {

    private final TradeHistoryJpaRepository tradeHistoryJpaRepository;

    @Override
    public TradeHistory save(TradeHistory tradeHistory) {
        return tradeHistoryJpaRepository.save(tradeHistory);
    }

    @Override
    public List<TradeHistory> findAllByTradeId(UUID tradeId) {
        return tradeHistoryJpaRepository.findAllByTradeIdOrderByCreatedAtAsc(tradeId);
    }
}
