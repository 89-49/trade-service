package org.pgsg.trade.infrastructure.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.pgsg.trade.application.port.out.persistence.TradePersistencePort;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.infrastructure.persistence.repository.TradeJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TradePersistenceAdapter implements TradePersistencePort {

    private final TradeJpaRepository tradeJpaRepository;

    @Override
    public Trade save(Trade trade) {
        return tradeJpaRepository.save(trade);
    }

    @Override
    public List<Trade> findAll() {
        return tradeJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<Trade> findById(UUID id) {
        return tradeJpaRepository.findById(id);
    }

    @Override
    public List<Trade> findByReservationId(UUID reservationId) {
        return tradeJpaRepository.findByReservationId(reservationId);
    }

    @Override
    public boolean existsByReservationId(UUID reservationId) {
        return tradeJpaRepository.existsByReservationId(reservationId);
    }
}
