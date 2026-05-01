package org.pgsg.trade.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pgsg.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum TradeErrorCode implements ErrorCode {

    // Traded Item
    TRADED_ITEM_ID_REQUIRED("trade.validation.traded-item-id.required"),
    TRADED_ITEM_NAME_REQUIRED("trade.validation.traded-item-name.required"),
    TRADED_ITEM_NAME_LENGTH_EXCEEDED("trade.validation.traded-item-name.length-exceeded"),
    TRADED_ITEM_PRICE_REQUIRED("trade.validation.traded-item-price.required"),
    TRADED_ITEM_PRICE_INVALID_RANGE("trade.validation.traded-item-price.invalid-range"),

    // Buyer
    BUYER_ID_REQUIRED("trade.validation.buyer-id.required"),
    BUYER_NAME_REQUIRED("trade.validation.buyer-name.required"),
    BUYER_NAME_LENGTH_EXCEEDED("trade.validation.buyer-name.length-exceeded"),

    // Seller
    SELLER_ID_REQUIRED("trade.validation.seller-id.required"),
    SELLER_NAME_REQUIRED("trade.validation.seller-name.required"),
    SELLER_NAME_LENGTH_EXCEEDED("trade.validation.seller-name.length-exceeded"),

    // Participants
    PARTICIPANTS_REQUIRED("trade.validation.participants.required"),
    PARTICIPANTS_SAME_PERSON("trade.validation.participants.same-person"),

    // Trade
    TRADE_ID_REQUIRED("trade.validation.id.required"),
    TRADE_NEW_STATUS_REQUIRED("trade.validation.new-status.required"),
    TRADED_ITEM_REQUIRED("trade.validation.traded-item.required"),
    RESERVATION_ID_REQUIRED("trade.validation.reservation-id.required"),
    TRADE_NOT_FOUND("trade.not-found"),
    TRADE_ALREADY_CLOSED("trade.already-closed"),
    TRADE_PARTICIPANT_NOT_FOUND("trade.participant.not-found"),
    TRADE_CONCURRENT_UPDATE_FAILED("trade.concurrent-update-failed"),

    // Cancel
    CANCEL_CANCELLER_ID_REQUIRED("trade.validation.cancel.canceller-id.required"),
    CANCEL_REASON_DETAIL_REQUIRED("trade.validation.cancel.reason-detail.required"),
    CANCEL_REASON_NOT_ALLOWED("trade.validation.cancel.reason-not-allowed"),
    CANCEL_REASON_INVALID("trade.validation.cancel.reason-invalid"),

    // Messaging
    RESERVATION_EVENT_DESERIALIZATION_FAILED("trade.messaging.reservation-event.deserialization-failed"),
    RESERVATION_EVENT_PROCESS_FAILED("trade.messaging.reservation-event.process-failed"),
    TRADE_EVENT_PUBLISH_FAILED("trade.messaging.trade-event.publish-failed"),

    ;

    private final String errorKey;
}
