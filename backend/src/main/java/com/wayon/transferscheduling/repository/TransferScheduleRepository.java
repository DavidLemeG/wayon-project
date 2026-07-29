package com.wayon.transferscheduling.repository;

import com.wayon.transferscheduling.domain.transfer.TransferSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferScheduleRepository extends JpaRepository<TransferSchedule, Long> {

    /**
     * Extrato com ordem explicita: agendamentos mais recentes primeiro.
     * findAll() puro nao garante ordem nenhuma por contrato — hoje o H2
     * devolve por id, mas isso e coincidencia da implementacao, nao promessa.
     */
    List<TransferSchedule> findAllByOrderBySchedulingDateDescIdDesc();

}
