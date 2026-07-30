package com.wayon.transferscheduling.service;

import com.wayon.transferscheduling.domain.transfer.Fee;
import com.wayon.transferscheduling.domain.transfer.FeeCalculator;
import com.wayon.transferscheduling.domain.transfer.TransferSchedule;
import com.wayon.transferscheduling.domain.transfer.exception.InvalidTransferDateException;
import com.wayon.transferscheduling.domain.transfer.exception.SameAccountTransferException;
import com.wayon.transferscheduling.repository.TransferScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferSchedulingServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate HOJE = LocalDate.of(2026, 7, 28);

    @Mock
    private FeeCalculator feeCalculator;

    @Mock
    private TransferScheduleRepository repository;

    private TransferSchedulingService service;

    @BeforeEach
    void setUp() {
        // Relogio fixo: "hoje" e sempre 2026-07-28, entao o teste nao depende da
        // data em que for executado.
        Clock clock = Clock.fixed(HOJE.atStartOfDay(ZONE).toInstant(), ZONE);
        service = new TransferSchedulingService(feeCalculator, repository, clock);
    }

    @Test
    void agendaTransferenciaEPersisteAposCalculoDeTaxaComSucesso() {
        Fee fee = new Fee(0L, new BigDecimal("3.00"), new BigDecimal("0.0250"),
                new BigDecimal("25.00"), new BigDecimal("28.00"));
        when(feeCalculator.calculate(any(), any(), any())).thenReturn(fee);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransferSchedule result = service.schedule("1111111111", "2222222222",
                new BigDecimal("1000.00"), HOJE);

        assertThat(result.getTotalFee()).isEqualByComparingTo("28.00");

        ArgumentCaptor<TransferSchedule> captor = ArgumentCaptor.forClass(TransferSchedule.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOriginAccount()).isEqualTo("1111111111");
        assertThat(captor.getValue().getDestinationAccount()).isEqualTo("2222222222");
    }

    @Test
    void dataDeAgendamentoVemDoRelogioInjetado() {
        Fee fee = new Fee(0L, new BigDecimal("3.00"), new BigDecimal("0.0250"),
                new BigDecimal("25.00"), new BigDecimal("28.00"));
        when(feeCalculator.calculate(any(), any(), any())).thenReturn(fee);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransferSchedule result = service.schedule("1111111111", "2222222222",
                new BigDecimal("1000.00"), HOJE.plusDays(5));

        assertThat(result.getSchedulingDate()).isEqualTo(HOJE);
        // A taxa e calculada a partir dessa mesma data de agendamento.
        verify(feeCalculator).calculate(eq(HOJE), eq(HOJE.plusDays(5)), any());
    }

    @Test
    void naoPersisteQuandoContaOrigemIgualContaDestino() {
        assertThatThrownBy(() -> service.schedule("1111111111", "1111111111",
                new BigDecimal("1000.00"), HOJE))
                .isInstanceOf(SameAccountTransferException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void naoPersisteQuandoCalculoDeTaxaRejeitaData() {
        when(feeCalculator.calculate(any(), any(), any()))
                .thenThrow(new InvalidTransferDateException(51));

        assertThatThrownBy(() -> service.schedule("1111111111", "2222222222",
                new BigDecimal("1000.00"), HOJE.plusDays(51)))
                .isInstanceOf(InvalidTransferDateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void listAllRetornaTodosOsAgendamentosPersistidos() {
        TransferSchedule schedule = TransferSchedule.builder()
                .originAccount("1111111111")
                .destinationAccount("2222222222")
                .amount(new BigDecimal("1000.00"))
                .fixedFee(new BigDecimal("3.00"))
                .percentageRate(new BigDecimal("0.0250"))
                .percentageFee(new BigDecimal("25.00"))
                .totalFee(new BigDecimal("28.00"))
                .transferDate(HOJE)
                .schedulingDate(HOJE)
                .build();
        when(repository.findAllByOrderBySchedulingDateDescIdDesc()).thenReturn(List.of(schedule));

        List<TransferSchedule> result = service.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOriginAccount()).isEqualTo("1111111111");
    }

}
