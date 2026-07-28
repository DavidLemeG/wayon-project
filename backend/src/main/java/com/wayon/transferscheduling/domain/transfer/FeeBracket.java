package com.wayon.transferscheduling.domain.transfer;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum FeeBracket {

    // Escalas declaradas explicitamente (2 casas para dinheiro, 4 para a aliquota)
    // para casar com as colunas NUMERIC(19,2)/NUMERIC(19,4) da entidade: assim o
    // JSON de um agendamento recem-criado e o de um lido do banco sao identicos.
    SAME_DAY(0, 0, new BigDecimal("3.00"), new BigDecimal("0.0250")),
    UP_TO_10_DAYS(1, 10, new BigDecimal("12.00"), new BigDecimal("0.0000")),
    UP_TO_20_DAYS(11, 20, new BigDecimal("0.00"), new BigDecimal("0.0820")),
    UP_TO_30_DAYS(21, 30, new BigDecimal("0.00"), new BigDecimal("0.0690")),
    UP_TO_40_DAYS(31, 40, new BigDecimal("0.00"), new BigDecimal("0.0470")),
    UP_TO_50_DAYS(41, 50, new BigDecimal("0.00"), new BigDecimal("0.0170"));

    private final int minDays;
    private final int maxDays;
    private final BigDecimal fixedFee;
    private final BigDecimal percentageRate;

    FeeBracket(int minDays, int maxDays, BigDecimal fixedFee, BigDecimal percentageRate) {
        this.minDays = minDays;
        this.maxDays = maxDays;
        this.fixedFee = fixedFee;
        this.percentageRate = percentageRate;
    }

    public static Optional<FeeBracket> forDays(long days) {
        return Arrays.stream(values())
                .filter(bracket -> days >= bracket.minDays && days <= bracket.maxDays)
                .findFirst();
    }

}
