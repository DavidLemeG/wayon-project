package com.wayon.transferscheduling.domain.transfer;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum FeeBracket {

    SAME_DAY(0, 0, new BigDecimal("3.00"), new BigDecimal("0.025")),
    UP_TO_10_DAYS(1, 10, new BigDecimal("12.00"), BigDecimal.ZERO),
    UP_TO_20_DAYS(11, 20, BigDecimal.ZERO, new BigDecimal("0.082")),
    UP_TO_30_DAYS(21, 30, BigDecimal.ZERO, new BigDecimal("0.069")),
    UP_TO_40_DAYS(31, 40, BigDecimal.ZERO, new BigDecimal("0.047")),
    UP_TO_50_DAYS(41, 50, BigDecimal.ZERO, new BigDecimal("0.017"));

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
