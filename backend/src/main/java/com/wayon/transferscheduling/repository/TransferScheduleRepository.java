package com.wayon.transferscheduling.repository;

import com.wayon.transferscheduling.domain.transfer.TransferSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferScheduleRepository extends JpaRepository<TransferSchedule, Long> {
}
