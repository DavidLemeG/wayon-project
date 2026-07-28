package com.wayon.transferscheduling;

import com.wayon.transferscheduling.api.dto.TransferResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferSchedulingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void agendaTransferenciaEDepoisApareceNoExtrato() {
        Map<String, Object> request = new HashMap<>();
        request.put("originAccount", "1111111111");
        request.put("destinationAccount", "2222222222");
        request.put("amount", new BigDecimal("500.00"));
        request.put("transferDate", LocalDate.now().plusDays(5).toString());

        ResponseEntity<TransferResponse> createResponse =
                restTemplate.postForEntity("/api/transfers", request, TransferResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getTotalFee()).isEqualByComparingTo("12.00");

        ResponseEntity<TransferResponse[]> listResponse =
                restTemplate.getForEntity("/api/transfers", TransferResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody())
                .anySatisfy(transfer -> assertThat(transfer.getId()).isEqualTo(createResponse.getBody().getId()));
    }

    @Test
    void transferenciaComDataForaDaJanelaNaoEPersistida() {
        Map<String, Object> request = new HashMap<>();
        request.put("originAccount", "3333333333");
        request.put("destinationAccount", "4444444444");
        request.put("amount", new BigDecimal("500.00"));
        request.put("transferDate", LocalDate.now().plusDays(51).toString());

        ResponseEntity<String> response =
                restTemplate.postForEntity("/api/transfers", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void autoTransferenciaERejeitadaComErroDeNegocio() {
        Map<String, Object> request = new HashMap<>();
        request.put("originAccount", "5555555555");
        request.put("destinationAccount", "5555555555");
        request.put("amount", new BigDecimal("500.00"));
        request.put("transferDate", LocalDate.now().toString());

        ResponseEntity<String> response =
                restTemplate.postForEntity("/api/transfers", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
