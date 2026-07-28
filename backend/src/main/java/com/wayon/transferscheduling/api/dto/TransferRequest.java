package com.wayon.transferscheduling.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TransferRequest {

    @NotBlank(message = "Conta de origem é obrigatória")
    @Pattern(regexp = "\\d{10}", message = "Conta de origem deve conter exatamente 10 dígitos")
    private String originAccount;

    @NotBlank(message = "Conta de destino é obrigatória")
    @Pattern(regexp = "\\d{10}", message = "Conta de destino deve conter exatamente 10 dígitos")
    private String destinationAccount;

    @NotNull(message = "Valor da transferência é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor da transferência deve ser maior que zero")
    private BigDecimal amount;

    @NotNull(message = "Data da transferência é obrigatória")
    private LocalDate transferDate;

}
