package org.pgsg.trade.application.port.out.persistence;

import org.pgsg.trade.domain.model.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradePersistencePort {

    Trade save(Trade trade);

    Page<Trade> findAll(Pageable pageable);

    Optional<Trade> findById(UUID id);

    List<Trade> findByReservationId(UUID reservationId);

    boolean existsByReservationId(UUID reservationId);
}
