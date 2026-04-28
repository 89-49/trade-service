package org.pgsg.trade.application.port.out;

import org.pgsg.trade.domain.model.Trade;

import java.util.Optional;
import java.util.UUID;

public interface TradePersistencePort {

    Trade save(Trade trade);

    Optional<Trade> findById(UUID id);

    Optional<Trade> findByReservationId(UUID reservationId);

    boolean existsByReservationId(UUID reservationId);
}
