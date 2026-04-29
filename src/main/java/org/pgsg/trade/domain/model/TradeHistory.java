package org.pgsg.trade.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;
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

        this.id = id;
        this.tradeId = tradeId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.cancelledBy = cancelledBy;
        this.cancellerId = cancellerId;
        this.cancelReasonType = cancelReasonType;
        this.cancelReasonDetail = cancelReasonDetail;
    }

    // TODO: 리팩토링 필요 - 검증 로직을 별도의 Validator 클래스로 분리, 에러 메시지 상수화
    public static TradeHistory create(UUID tradeId, TradeStatus previousStatus, TradeStatus newStatus,
                                      CancellerType cancelledBy, UUID cancellerId,
                                      CancelReasonType cancelReasonType, String cancelReasonDetail) {
        return TradeHistory.builder()
                .id(UUID.randomUUID())
                .tradeId(tradeId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .cancelledBy(cancelledBy)
                .cancellerId(cancellerId)
                .cancelReasonType(cancelReasonType)
                .cancelReasonDetail(cancelReasonDetail)
                .build();
    }

    private void validateRequired(UUID id, UUID tradeId, TradeStatus newStatus) {
        if (id == null) {
            throw new TradeDomainValidationException("id는 필수입니다.");
        }

        if (tradeId == null) {
            throw new TradeDomainValidationException("tradeId는 필수입니다.");
        }

        if (newStatus == null) {
            throw new TradeDomainValidationException("newStatus는 필수입니다.");
        }
    }

    private void validateCancel(CancellerType cancelledBy, UUID cancellerId,
                                CancelReasonType type, String detail) {
        if (cancelledBy != null && cancellerId == null) {
            throw new TradeDomainValidationException("취소 주체 ID는 필수입니다.");
        }

        if (type == CancelReasonType.ETC && (detail == null || detail.isBlank())) {
            throw new TradeDomainValidationException("기타 사유인 경우 상세 사유 입력은 필수입니다.");
        }

        if (cancelledBy != null && type != null && !type.allowedFor(cancelledBy)) {
            throw new TradeDomainValidationException("해당 주체는 이 취소 사유를 사용할 수 없습니다.");
        }
    }
}
