package com.wayon.transferscheduling.domain.transfer;

import com.wayon.transferscheduling.domain.transfer.exception.InvalidTransferDateException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BracketFeeCalculatorTest {

    private static final LocalDate SCHEDULING_DATE = LocalDate.of(2026, 7, 28);
    private static final BigDecimal AMOUNT = new BigDecimal("1000.00");

    private final BracketFeeCalculator calculator = new BracketFeeCalculator();

    @ParameterizedTest(name = "dia {0}: taxa fixa {1}, taxa % {2}, total {3}")
    @CsvSource({
            "0,  3.00, 0.0250, 28.00",
            "1,  12.00, 0.0000, 12.00",
            "10, 12.00, 0.0000, 12.00",
            "11, 0.00, 0.0820, 82.00",
            "20, 0.00, 0.0820, 82.00",
            "21, 0.00, 0.0690, 69.00",
            "30, 0.00, 0.0690, 69.00",
            "31, 0.00, 0.0470, 47.00",
            "40, 0.00, 0.0470, 47.00",
            "41, 0.00, 0.0170, 17.00",
            "50, 0.00, 0.0170, 17.00",
    })
    void calculaTaxaCorretaParaCadaFaixa(long dias, BigDecimal taxaFixaEsperada,
                                         BigDecimal taxaPercentualEsperada, BigDecimal totalEsperado) {
        LocalDate transferDate = SCHEDULING_DATE.plusDays(dias);

        Fee fee = calculator.calculate(SCHEDULING_DATE, transferDate, AMOUNT);

        assertThat(fee.getFixedFee()).isEqualByComparingTo(taxaFixaEsperada);
        assertThat(fee.getPercentageRate()).isEqualByComparingTo(taxaPercentualEsperada);
        assertThat(fee.getTotalFee()).isEqualByComparingTo(totalEsperado);
    }

    @ParameterizedTest(name = "dia {0} deve ser rejeitado por estar fora da janela 0-50")
    @CsvSource({"51", "60", "100"})
    void rejeitaTransferenciaAlemDe50Dias(long dias) {
        LocalDate transferDate = SCHEDULING_DATE.plusDays(dias);

        assertThatThrownBy(() -> calculator.calculate(SCHEDULING_DATE, transferDate, AMOUNT))
                .isInstanceOf(InvalidTransferDateException.class);
    }

    @ParameterizedTest(name = "data de transferencia no passado ({0} dia(s) antes) deve ser rejeitada")
    @CsvSource({"1", "5", "30"})
    void rejeitaDataDeTransferenciaNoPassado(long diasNoPassado) {
        LocalDate transferDate = SCHEDULING_DATE.minusDays(diasNoPassado);

        assertThatThrownBy(() -> calculator.calculate(SCHEDULING_DATE, transferDate, AMOUNT))
                .isInstanceOf(InvalidTransferDateException.class);
    }

}
