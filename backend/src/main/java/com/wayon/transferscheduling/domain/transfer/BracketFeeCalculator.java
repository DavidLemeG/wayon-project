package com.wayon.transferscheduling.domain.transfer;

import com.wayon.transferscheduling.domain.transfer.exception.InvalidTransferDateException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class BracketFeeCalculator implements FeeCalculator {

    @Override
    public Fee calculate(LocalDate schedulingDate, LocalDate transferDate, BigDecimal amount) {
        long daysBetween = ChronoUnit.DAYS.between(schedulingDate, transferDate);

        FeeBracket bracket = FeeBracket.forDays(daysBetween)
                .orElseThrow(() -> new InvalidTransferDateException(daysBetween));

        BigDecimal fixedFee = bracket.getFixedFee();
        BigDecimal rawPercentageFee = amount.multiply(bracket.getPercentageRate());

        // Arredonda uma unica vez, no total, para nao acumular erro de arredondamento
        // entre a parcela fixa e a percentual (ver ADR 0004).
        BigDecimal totalFee = fixedFee.add(rawPercentageFee).setScale(2, RoundingMode.HALF_UP);
        BigDecimal percentageFee = totalFee.subtract(fixedFee);

        return new Fee(daysBetween, fixedFee, bracket.getPercentageRate(), percentageFee, totalFee);
    }

}
