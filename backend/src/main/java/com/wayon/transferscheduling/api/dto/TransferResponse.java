package com.wayon.transferscheduling.api.dto;

import com.wayon.transferscheduling.domain.transfer.TransferSchedule;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class TransferResponse {

    private Long id;
    private String originAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private BigDecimal fixedFee;
    private BigDecimal percentageRate;
    private BigDecimal percentageFee;
    private BigDecimal totalFee;
    private LocalDate transferDate;
    private LocalDate schedulingDate;

    public static TransferResponse from(TransferSchedule entity) {
        return TransferResponse.builder()
                .id(entity.getId())
                .originAccount(entity.getOriginAccount())
                .destinationAccount(entity.getDestinationAccount())
                .amount(entity.getAmount())
                .fixedFee(entity.getFixedFee())
                .percentageRate(entity.getPercentageRate())
                .percentageFee(entity.getPercentageFee())
                .totalFee(entity.getTotalFee())
                .transferDate(entity.getTransferDate())
                .schedulingDate(entity.getSchedulingDate())
                .build();
    }

}
