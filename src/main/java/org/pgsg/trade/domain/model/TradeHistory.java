package org.pgsg.trade.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;
import org.pgsg.trade.domain.exception.TradeErrorCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trade_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trade_id", nullable = false, updatable = false)
    private UUID tradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20, updatable = false)
    private TradeStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20, updatable = false)
    private TradeStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by", length = 20, updatable = false)
    private CancellerType cancelledBy;

    @Column(name = "canceller_id", updatable = false)
    private UUID cancellerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_reason_type", length = 50, updatable = false)
    private CancelReasonType cancelReasonType;

    @Column(name = "cancel_reason_detail", columnDefinition = "TEXT", updatable = false)
    private String cancelReasonDetail;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private TradeHistory(UUID tradeId, TradeStatus previousStatus, TradeStatus newStatus,
                         CancellerType cancelledBy, UUID cancellerId, CancelReasonType cancelReasonType, String cancelReasonDetail) {
        validateRequired(tradeId, newStatus);
        validateCancel(cancelledBy, cancellerId, cancelReasonType, cancelReasonDetail);

        this.tradeId = tradeId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.cancelledBy = cancelledBy;
        this.cancellerId = cancellerId;
        this.cancelReasonType = cancelReasonType;
        this.cancelReasonDetail = cancelReasonDetail;
    }

    public static TradeHistory create(UUID tradeId, TradeStatus previousStatus, TradeStatus newStatus,
                                      CancellerType cancelledBy, UUID cancellerId,
                                      CancelReasonType cancelReasonType, String cancelReasonDetail) {
        return TradeHistory.builder()
                .tradeId(tradeId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .cancelledBy(cancelledBy)
                .cancellerId(cancellerId)
                .cancelReasonType(cancelReasonType)
                .cancelReasonDetail(cancelReasonDetail)
                .build();
    }

    // TODO: 리팩토링 - 검증 로직을 별도의 Validator 클래스로 분리
    private void validateRequired(UUID tradeId, TradeStatus newStatus) {
        if (tradeId == null) {
            throw new TradeDomainValidationException(TradeErrorCode.TRADE_ID_REQUIRED);
        }

        if (newStatus == null) {
            throw new TradeDomainValidationException(TradeErrorCode.TRADE_NEW_STATUS_REQUIRED);
        }
    }

    private void validateCancel(CancellerType cancelledBy, UUID cancellerId,
                                CancelReasonType type, String detail) {
        if (cancelledBy != null && cancellerId == null) {
            throw new TradeDomainValidationException(TradeErrorCode.CANCEL_CANCELLER_ID_REQUIRED);
        }

        if (type == CancelReasonType.ETC && (detail == null || detail.isBlank())) {
            throw new TradeDomainValidationException(TradeErrorCode.CANCEL_REASON_DETAIL_REQUIRED);
        }

        if (cancelledBy != null && type != null && !type.allowedFor(cancelledBy)) {
            throw new TradeDomainValidationException(TradeErrorCode.CANCEL_REASON_NOT_ALLOWED);
        }
    }
}
