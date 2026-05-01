package org.pgsg.trade.presentation.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.domain.model.ParticipantStatus;
import org.pgsg.trade.domain.model.TradeStatus;
import org.pgsg.trade.presentation.dto.response.CompleteTradeResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeController 단위 테스트")
class TradeControllerTest {

    private static final UUID TRADE_ID = UUID.randomUUID();
    private static final UUID CURRENT_USER_ID = UUID.randomUUID();

    @Mock
    private TradeUseCase tradeUseCase;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("성공: 현재 로그인한 사용자 ID로 거래 완료 요청을 전달한다.")
    void completeTrade_UsesCurrentUserId() {
        // given
        setAuthentication(CURRENT_USER_ID);
        CompleteTradeResult result = new CompleteTradeResult(
                TRADE_ID,
                TradeStatus.TRADING,
                ParticipantStatus.COMPLETED,
                ParticipantStatus.TRADING,
                false,
                false
        );
        when(tradeUseCase.completeTrade(new CompleteTradeCommand(TRADE_ID, CURRENT_USER_ID))).thenReturn(result);

        TradeController controller = new TradeController(tradeUseCase);

        // when
        CompleteTradeResponse response = controller.completeTrade(TRADE_ID);

        // then
        ArgumentCaptor<CompleteTradeCommand> commandCaptor = ArgumentCaptor.forClass(CompleteTradeCommand.class);
        verify(tradeUseCase).completeTrade(commandCaptor.capture());

        CompleteTradeCommand command = commandCaptor.getValue();
        assertAll(
                () -> assertThat(command.tradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(command.participantId()).isEqualTo(CURRENT_USER_ID),
                () -> assertThat(response.tradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(response.tradeCompleted()).isFalse(),
                () -> assertThat(response.eventPublished()).isFalse()
        );
    }

    private void setAuthentication(UUID userId) {
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .uuid(userId)
                .username("test-user")
                .password("")
                .userRole("ROLE_USER")
                .name("테스트 사용자")
                .nickname("테스터")
                .enabled(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }
}
