package com.wayon.transferscheduling.service;

import com.wayon.transferscheduling.domain.transfer.Fee;
import com.wayon.transferscheduling.domain.transfer.FeeCalculator;
import com.wayon.transferscheduling.domain.transfer.TransferSchedule;
import com.wayon.transferscheduling.domain.transfer.exception.SameAccountTransferException;
import com.wayon.transferscheduling.repository.TransferScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class TransferSchedulingService {

    private static final ZoneId SCHEDULING_ZONE = ZoneId.of("America/Sao_Paulo");

    private final FeeCalculator feeCalculator;
    private final TransferScheduleRepository repository;

    public TransferSchedulingService(FeeCalculator feeCalculator, TransferScheduleRepository repository) {
        this.feeCalculator = feeCalculator;
        this.repository = repository;
    }

    @Transactional
    public TransferSchedule schedule(String originAccount, String destinationAccount,
                                      BigDecimal amount, LocalDate transferDate) {
        if (originAccount.equals(destinationAccount)) {
            throw new SameAccountTransferException();
        }

        LocalDate schedulingDate = LocalDate.now(SCHEDULING_ZONE);

        // Cálculo de taxa acontece antes de qualquer persistência: se a data estiver
        // fora da janela válida, a exceção interrompe o fluxo e nada é salvo.
        Fee fee = feeCalculator.calculate(schedulingDate, transferDate, amount);

        TransferSchedule transferSchedule = TransferSchedule.builder()
                .originAccount(originAccount)
                .destinationAccount(destinationAccount)
                .amount(amount)
                .fixedFee(fee.getFixedFee())
                .percentageRate(fee.getPercentageRate())
                .percentageFee(fee.getPercentageFee())
                .totalFee(fee.getTotalFee())
                .transferDate(transferDate)
                .schedulingDate(schedulingDate)
                .build();

        return repository.save(transferSchedule);
    }

    @Transactional(readOnly = true)
    public List<TransferSchedule> listAll() {
        return repository.findAll();
    }

}
