package org.pgsg.trade.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Trade 도메인 - CancelReasonType Enum 단위 테스트")
class CancelReasonTypeTest {

    @ParameterizedTest
    @CsvSource({
            "CHANGE_OF_MIND, CHANGE_OF_MIND",
            "change_of_mind, CHANGE_OF_MIND",
            "Product_Issue, PRODUCT_ISSUE"
    })
    @DisplayName("성공: 유효한 문자열이 주어지면 대소문자 구분 없이 정확한 Enum을 반환한다.")
    void from_ValidString_ReturnsEnum(String input, CancelReasonType expected) {
        assertThat(CancelReasonType.from(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "INVALID_REASON"})
    @DisplayName("실패: 유효하지 않은 문자열이나 null이 주어지면 예외가 발생한다.")
    void from_InvalidString_ThrowsException(String input) {
        assertThatThrownBy(() -> CancelReasonType.from(input))
                .isInstanceOf(TradeDomainValidationException.class);
    }

    @Test
    @DisplayName("성공: 공통 사유(CHANGE_OF_MIND)는 판매자와 구매자 모두에게 허용된다.")
    void allowedFor_CommonReason_ReturnsTrue() {
        assertThat(CancelReasonType.CHANGE_OF_MIND.allowedFor(CancellerType.SELLER)).isTrue();
        assertThat(CancelReasonType.CHANGE_OF_MIND.allowedFor(CancellerType.BUYER)).isTrue();
        assertThat(CancelReasonType.CHANGE_OF_MIND.allowedFor(CancellerType.SYSTEM)).isFalse();
    }

    @Test
    @DisplayName("성공: 판매자 전용 사유(PRODUCT_ISSUE)는 판매자에게만 허용된다.")
    void allowedFor_SellerOnlyReason_ReturnsTrue() {
        assertThat(CancelReasonType.PRODUCT_ISSUE.allowedFor(CancellerType.SELLER)).isTrue();
        assertThat(CancelReasonType.PRODUCT_ISSUE.allowedFor(CancellerType.BUYER)).isFalse();
        assertThat(CancelReasonType.PRODUCT_ISSUE.allowedFor(CancellerType.SYSTEM)).isFalse();
    }

    @Test
    @DisplayName("성공: 구매자 전용 사유(REFUND_REQUEST)는 구매자에게만 허용된다.")
    void allowedFor_BuyerOnlyReason_ReturnsTrue() {
        assertThat(CancelReasonType.REFUND_REQUEST.allowedFor(CancellerType.BUYER)).isTrue();
        assertThat(CancelReasonType.REFUND_REQUEST.allowedFor(CancellerType.SELLER)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = CancellerType.class, names = {"SELLER", "BUYER"})
    @DisplayName("성공: 시스템 전용 사유(POLICY_VIOLATION)는 일반 사용자에게 허용되지 않는다.")
    void allowedFor_SystemReason_ReturnsFalseForUsers(CancellerType userType) {
        assertThat(CancelReasonType.POLICY_VIOLATION.allowedFor(userType)).isFalse();
        assertThat(CancelReasonType.POLICY_VIOLATION.allowedFor(CancellerType.SYSTEM)).isTrue();
    }
}
