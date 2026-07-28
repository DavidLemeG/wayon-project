package com.wayon.transferscheduling.api;

import com.wayon.transferscheduling.api.dto.TransferRequest;
import com.wayon.transferscheduling.api.dto.TransferResponse;
import com.wayon.transferscheduling.service.TransferSchedulingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferSchedulingService transferSchedulingService;

    public TransferController(TransferSchedulingService transferSchedulingService) {
        this.transferSchedulingService = transferSchedulingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse create(@Valid @RequestBody TransferRequest request) {
        return TransferResponse.from(transferSchedulingService.schedule(
                request.getOriginAccount(),
                request.getDestinationAccount(),
                request.getAmount(),
                request.getTransferDate()));
    }

    @GetMapping
    public List<TransferResponse> list() {
        return transferSchedulingService.listAll().stream()
                .map(TransferResponse::from)
                .collect(Collectors.toList());
    }

}
