package com.wayon.transferscheduling.domain.transfer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transfer_schedule")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TransferSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String originAccount;

    @Column(nullable = false, length = 10)
    private String destinationAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fixedFee;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal percentageRate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal percentageFee;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalFee;

    @Column(nullable = false)
    private LocalDate transferDate;

    @Column(nullable = false)
    private LocalDate schedulingDate;

}
