package com.wayon.transferscheduling.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayon.transferscheduling.api.dto.TransferRequest;
import com.wayon.transferscheduling.domain.transfer.TransferSchedule;
import com.wayon.transferscheduling.domain.transfer.exception.InvalidTransferDateException;
import com.wayon.transferscheduling.domain.transfer.exception.SameAccountTransferException;
import com.wayon.transferscheduling.service.TransferSchedulingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferSchedulingService transferSchedulingService;

    @Test
    void criaAgendamentoComSucessoRetorna201() throws Exception {
        TransferSchedule schedule = TransferSchedule.builder()
                .id(1L)
                .originAccount("1111111111")
                .destinationAccount("2222222222")
                .amount(new BigDecimal("1000.00"))
                .fixedFee(new BigDecimal("3.00"))
                .percentageRate(new BigDecimal("0.025"))
                .percentageFee(new BigDecimal("25.00"))
                .totalFee(new BigDecimal("28.00"))
                .transferDate(LocalDate.now())
                .schedulingDate(LocalDate.now())
                .build();
        when(transferSchedulingService.schedule(anyString(), anyString(), any(), any()))
                .thenReturn(schedule);

        String body = objectMapper.writeValueAsString(transferRequest(
                "1111111111", "2222222222", new BigDecimal("1000.00"), LocalDate.now()));

        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalFee").value(28.00));
    }

    @Test
    void contaComFormatoInvalidoRetorna400() throws Exception {
        String body = objectMapper.writeValueAsString(transferRequest(
                "123", "2222222222", new BigDecimal("1000.00"), LocalDate.now()));

        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void autoTransferenciaRetorna422() throws Exception {
        when(transferSchedulingService.schedule(anyString(), anyString(), any(), any()))
                .thenThrow(new SameAccountTransferException());

        String body = objectMapper.writeValueAsString(transferRequest(
                "1111111111", "1111111111", new BigDecimal("1000.00"), LocalDate.now()));

        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void dataForaDaJanelaRetorna422() throws Exception {
        when(transferSchedulingService.schedule(anyString(), anyString(), any(), any()))
                .thenThrow(new InvalidTransferDateException(51));

        String body = objectMapper.writeValueAsString(transferRequest(
                "1111111111", "2222222222", new BigDecimal("1000.00"), LocalDate.now().plusDays(51)));

        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void valorComMaisDeDuasCasasDecimaisRetorna400() throws Exception {
        String body = objectMapper.writeValueAsString(transferRequest(
                "1111111111", "2222222222", new BigDecimal("1000.999"), LocalDate.now()));

        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0]").value(
                        "amount: Valor da transferência deve ter no máximo 2 casas decimais"));
    }

    @Test
    void jsonMalformadoRetorna400NoFormatoApiError() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content("{\"originAccount\":\"1111111111\",}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/transfers"));
    }

    @Test
    void dataEmFormatoInvalidoRetorna400NoFormatoApiError() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType("application/json")
                        .content("{\"originAccount\":\"1111111111\",\"destinationAccount\":\"2222222222\","
                                + "\"amount\":1000.00,\"transferDate\":\"28/07/2026\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void listaExtratoRetorna200() throws Exception {
        when(transferSchedulingService.listAll()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk());
    }

    private TransferRequest transferRequest(String originAccount, String destinationAccount,
                                              BigDecimal amount, LocalDate transferDate) {
        TransferRequest request = new TransferRequest();
        request.setOriginAccount(originAccount);
        request.setDestinationAccount(destinationAccount);
        request.setAmount(amount);
        request.setTransferDate(transferDate);
        return request;
    }

}
