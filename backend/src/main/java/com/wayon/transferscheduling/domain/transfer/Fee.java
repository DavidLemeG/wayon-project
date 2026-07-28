package com.wayon.transferscheduling.domain.transfer;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Fee {

    BigDecimal fixedFee;
    BigDecimal percentageRate;
    BigDecimal percentageFee;
    BigDecimal totalFee;

}
