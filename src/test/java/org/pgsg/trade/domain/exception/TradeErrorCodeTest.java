package org.pgsg.trade.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.common.exception.ErrorConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TradeErrorCodeTest {

    @Autowired
    private ErrorConfigProperties errorConfigProperties;

    @Test
    @DisplayName("성공: TradeErrorCode의 모든 에러 키가 application-trade-error.yaml에 정의되어 있어야 한다.")
    void verifyAllErrorKeysExistInYaml() {
        var configs = errorConfigProperties.getConfigs();

        assertThat(configs).isNotNull();

        for (TradeErrorCode errorCode : TradeErrorCode.values()) {
            String errorKey = errorCode.getErrorKey();
            assertThat(configs)
                    .withFailMessage("YAML 파일에 정의되지 않은 에러 키가 존재합니다. 누락된 키: " + errorKey)
                    .containsKey(errorKey);
        }
    }
}
