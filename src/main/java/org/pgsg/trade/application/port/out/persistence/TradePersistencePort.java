package org.pgsg.trade.application.port.out.persistence;

import org.pgsg.trade.domain.model.Trade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradePersistencePort {

    Trade save(Trade trade);

    List<Trade> findAll();

    Optional<Trade> findById(UUID id);

    List<Trade> findByReservationId(UUID reservationId);

    boolean existsByReservationId(UUID reservationId);
}
