package org.pgsg.trade.infrastructure.persistence.repository;

import org.pgsg.trade.domain.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeJpaRepository extends JpaRepository<Trade, UUID> {

    List<Trade> findAllByOrderByCreatedAtDesc();

    List<Trade> findByReservationId(UUID reservationId);

    boolean existsByReservationId(UUID reservationId);
}
