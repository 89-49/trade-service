package org.pgsg.trade.infrastructure.persistence.repository;

import org.pgsg.trade.domain.model.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeHistoryJpaRepository extends JpaRepository<TradeHistory, UUID> {

    List<TradeHistory> findAllByTradeIdOrderByCreatedAtAsc(UUID tradeId);
}
