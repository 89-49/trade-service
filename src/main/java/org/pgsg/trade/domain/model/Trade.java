package org.pgsg.trade.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_trade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reservation_id", unique = true, nullable = false, updatable = false)
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @Embedded
    private TradeParticipants participants;

    @Embedded
    private TradedItem tradedItem;

    @Column(name = "buyer_status", length = 20)
    private String buyerStatus;

    @Column(name = "seller_status", length = 20)
    private String sellerStatus;

    @Version
    @Column(nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Trade(UUID reservationId, TradeStatus status, TradeParticipants participants, TradedItem tradedItem, String buyerStatus, String sellerStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateReservationId(reservationId);
        validateParticipants(participants);
        validateTradedItem(tradedItem);

        this.reservationId = reservationId;
        this.status = status;
        this.participants = participants;
        this.tradedItem = tradedItem;
        this.buyerStatus = buyerStatus;
        this.sellerStatus = sellerStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Trade create(UUID reservationId, TradeParticipants participants, TradedItem tradedItem) {
        return Trade.builder()
                .reservationId(reservationId)
                .participants(participants)
                .tradedItem(tradedItem)
                .status(TradeStatus.TRADING)
                .build();
    }

    // TODO: 리팩토링 - 검증 로직을 별도의 Validator 클래스로 분리
    private static void validateReservationId(UUID reservationId) {
        if (reservationId == null) {
            throw new TradeDomainValidationException(TradeErrorCode.RESERVATION_ID_REQUIRED);
        }
    }

    private static void validateParticipants(TradeParticipants participants) {
        if (participants == null) {
            throw new TradeDomainValidationException(TradeErrorCode.PARTICIPANTS_REQUIRED);
        }
    }

    private static void validateTradedItem(TradedItem tradedItem) {
        if (tradedItem == null) {
            throw new TradeDomainValidationException(TradeErrorCode.TRADED_ITEM_REQUIRED);
        }
    }
}
