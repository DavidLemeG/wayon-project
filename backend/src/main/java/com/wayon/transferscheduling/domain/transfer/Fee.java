package com.wayon.transferscheduling.domain.transfer;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Fee {

    /**
     * Dias entre agendamento e transferencia que determinaram a faixa aplicada.
     * Fica no resultado para quem chama nao precisar recalcular (o que
     * duplicaria a regra de contagem de dias fora do calculador).
     */
    long daysBetween;

    BigDecimal fixedFee;
    BigDecimal percentageRate;
    BigDecimal percentageFee;
    BigDecimal totalFee;

}
