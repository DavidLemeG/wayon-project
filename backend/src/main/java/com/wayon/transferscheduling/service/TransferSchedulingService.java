package com.wayon.transferscheduling.service;

import com.wayon.transferscheduling.domain.transfer.Fee;
import com.wayon.transferscheduling.domain.transfer.FeeCalculator;
import com.wayon.transferscheduling.domain.transfer.TransferSchedule;
import com.wayon.transferscheduling.common.AccountMasker;
import com.wayon.transferscheduling.domain.transfer.exception.SameAccountTransferException;
import com.wayon.transferscheduling.repository.TransferScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TransferSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(TransferSchedulingService.class);

    private final FeeCalculator feeCalculator;
    private final TransferScheduleRepository repository;
    private final Clock clock;

    public TransferSchedulingService(FeeCalculator feeCalculator,
                                      TransferScheduleRepository repository,
                                      Clock clock) {
        this.feeCalculator = feeCalculator;
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public TransferSchedule schedule(String originAccount, String destinationAccount,
                                      BigDecimal amount, LocalDate transferDate) {
        if (originAccount.equals(destinationAccount)) {
            log.warn("Agendamento recusado: conta de origem e destino sao a mesma (conta={})",
                    AccountMasker.mask(originAccount));
            throw new SameAccountTransferException();
        }

        LocalDate schedulingDate = LocalDate.now(clock);

        // Normaliza para a mesma escala da coluna NUMERIC(19,2) ANTES de calcular a
        // taxa, garantindo que a taxa persistida seja sempre coerente com o valor
        // persistido. Nao arredonda: @Digits(fraction = 2) no TransferRequest ja
        // rejeita valores com mais casas decimais, entao setScale aqui e sem perda
        // (e lanca ArithmeticException, alto e claro, se essa garantia for violada).
        BigDecimal normalizedAmount = amount.setScale(2);

        // Cálculo de taxa acontece antes de qualquer persistência: se a data estiver
        // fora da janela válida, a exceção interrompe o fluxo e nada é salvo.
        Fee fee = feeCalculator.calculate(schedulingDate, transferDate, normalizedAmount);

        log.info("Taxa calculada: {} dia(s) entre agendamento e transferencia -> "
                        + "taxa fixa R$ {} + aliquota {}% (R$ {}) = total R$ {}",
                ChronoUnit.DAYS.between(schedulingDate, transferDate),
                fee.getFixedFee(),
                fee.getPercentageRate().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString(),
                fee.getPercentageFee(),
                fee.getTotalFee());

        TransferSchedule transferSchedule = TransferSchedule.builder()
                .originAccount(originAccount)
                .destinationAccount(destinationAccount)
                .amount(normalizedAmount)
                .fixedFee(fee.getFixedFee())
                .percentageRate(fee.getPercentageRate())
                .percentageFee(fee.getPercentageFee())
                .totalFee(fee.getTotalFee())
                .transferDate(transferDate)
                .schedulingDate(schedulingDate)
                .build();

        TransferSchedule saved = repository.save(transferSchedule);

        log.info("Agendamento id={} criado: origem={}, destino={}, valor=R$ {}, taxa total=R$ {}, "
                        + "transferencia em {}, agendado em {}",
                saved.getId(),
                AccountMasker.mask(saved.getOriginAccount()),
                AccountMasker.mask(saved.getDestinationAccount()),
                saved.getAmount(),
                saved.getTotalFee(),
                saved.getTransferDate(),
                saved.getSchedulingDate());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<TransferSchedule> listAll() {
        return repository.findAllByOrderBySchedulingDateDescIdDesc();
    }

}
