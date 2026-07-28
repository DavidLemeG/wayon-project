package com.wayon.transferscheduling.domain.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FeeCalculator {

    Fee calculate(LocalDate schedulingDate, LocalDate transferDate, BigDecimal amount);

}
