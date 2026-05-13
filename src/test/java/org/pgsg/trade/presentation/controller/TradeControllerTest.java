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
import org.pgsg.trade.application.dto.result.TradeResult;
import org.pgsg.trade.application.port.in.TradeUseCase;
import org.pgsg.trade.domain.model.ParticipantStatus;
import org.pgsg.trade.domain.model.TradeStatus;
import org.pgsg.trade.presentation.dto.response.CompleteTradeResponse;
import org.pgsg.trade.presentation.dto.response.TradeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeController 단위 테스트")
class TradeControllerTest {

    private static final UUID TRADE_ID = UUID.randomUUID();
    private static final UUID RESERVATION_ID = UUID.randomUUID();
    private static final UUID CURRENT_USER_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Mock
    private TradeUseCase tradeUseCase;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("성공: 거래 목록 조회 결과를 응답 DTO로 변환한다.")
    void getTrades_ReturnsTradeResponses() {
        // given
        TradeResult result = createTradeResult();
        Pageable pageable = PageRequest.of(0, 20);
        when(tradeUseCase.getTrades(pageable)).thenReturn(new PageImpl<>(List.of(result), pageable, 1));

        TradeController controller = new TradeController(tradeUseCase);

        // when
        Page<TradeResponse> responses = controller.getTrades(0, 20);

        // then
        assertAll(
                () -> assertThat(responses.getContent()).hasSize(1),
                () -> assertThat(responses.getContent().get(0).tradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(responses.getContent().get(0).reservationId()).isEqualTo(RESERVATION_ID),
                () -> assertThat(responses.getContent().get(0).productName()).isEqualTo("테스트 상품"),
                () -> assertThat(responses.getTotalElements()).isEqualTo(1)
        );
        verify(tradeUseCase).getTrades(pageable);
    }

    @Test
    @DisplayName("실패: 거래 목록 조회 page가 음수이면 예외가 발생한다.")
    void getTrades_NegativePage_ThrowsException() {
        // given
        TradeController controller = new TradeController(tradeUseCase);

        // when & then
        assertThatThrownBy(() -> controller.getTrades(-1, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("page must be greater than or equal to 0");
    }

    @Test
    @DisplayName("실패: 거래 목록 조회 size가 최대값을 초과하면 예외가 발생한다.")
    void getTrades_SizeOverMax_ThrowsException() {
        // given
        TradeController controller = new TradeController(tradeUseCase);

        // when & then
        assertThatThrownBy(() -> controller.getTrades(0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("size must be between 1 and 100");
    }

    @Test
    @DisplayName("성공: 거래 단건 조회 요청을 전달하고 응답 DTO로 변환한다.")
    void getTrade_ReturnsTradeResponse() {
        // given
        TradeResult result = createTradeResult();
        when(tradeUseCase.getTrade(TRADE_ID)).thenReturn(result);

        TradeController controller = new TradeController(tradeUseCase);

        // when
        TradeResponse response = controller.getTrade(TRADE_ID);

        // then
        assertAll(
                () -> assertThat(response.tradeId()).isEqualTo(TRADE_ID),
                () -> assertThat(response.buyerId()).isEqualTo(BUYER_ID),
                () -> assertThat(response.sellerId()).isEqualTo(SELLER_ID),
                () -> assertThat(response.productPrice()).isEqualTo(10000L)
        );
        verify(tradeUseCase).getTrade(TRADE_ID);
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

    private TradeResult createTradeResult() {
        return new TradeResult(
                TRADE_ID,
                RESERVATION_ID,
                TradeStatus.TRADING,
                BUYER_ID,
                "구매자",
                ParticipantStatus.TRADING,
                SELLER_ID,
                "판매자",
                ParticipantStatus.TRADING,
                PRODUCT_ID,
                "테스트 상품",
                10000L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
