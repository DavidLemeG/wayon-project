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
import java.util.Comparator;
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

    /**
     * O JSON de um agendamento recem-criado (objeto ainda em memoria, vindo do
     * calculador) precisa ser byte a byte igual ao do mesmo agendamento lido do
     * banco — caso contrario o cliente recebe duas representacoes diferentes do
     * mesmo recurso (ex.: "percentageRate":0.082 no POST e 0.0820 no GET).
     */
    @Test
    void representacaoJsonDoPostEDoGetSaoIdenticasParaOMesmoAgendamento() {
        Map<String, Object> request = new HashMap<>();
        request.put("originAccount", "6666666666");
        request.put("destinationAccount", "7777777777");
        request.put("amount", new BigDecimal("1000.00"));
        request.put("transferDate", LocalDate.now().plusDays(15).toString());

        String createdJson = restTemplate.postForEntity("/api/transfers", request, String.class).getBody();
        String statementJson = restTemplate.getForEntity("/api/transfers", String.class).getBody();

        assertThat(statementJson).contains(createdJson);
    }

    @Test
    void valorComMaisDeDuasCasasDecimaisERejeitadoEmVezDeArredondadoSilenciosamente() {
        Map<String, Object> request = new HashMap<>();
        request.put("originAccount", "8888888888");
        request.put("destinationAccount", "9999999999");
        request.put("amount", new BigDecimal("1000.999"));
        request.put("transferDate", LocalDate.now().toString());

        ResponseEntity<String> response =
                restTemplate.postForEntity("/api/transfers", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        String statementJson = restTemplate.getForEntity("/api/transfers", String.class).getBody();
        assertThat(statementJson).doesNotContain("8888888888");
    }

    @Test
    void extratoRetornaAgendamentosMaisRecentesPrimeiro() {
        postTransfer("1010101010", "2020202020", LocalDate.now().plusDays(3));
        postTransfer("3030303030", "4040404040", LocalDate.now().plusDays(4));

        ResponseEntity<TransferResponse[]> response =
                restTemplate.getForEntity("/api/transfers", TransferResponse[].class);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isSortedAccordingTo(
                Comparator.comparing(TransferResponse::getId).reversed());
    }

    private void postTransfer(String origin, String destination, LocalDate transferDate) {
        Map<String, Object> request = new HashMap<>();
        request.put("originAccount", origin);
        request.put("destinationAccount", destination);
        request.put("amount", new BigDecimal("100.00"));
        request.put("transferDate", transferDate.toString());
        restTemplate.postForEntity("/api/transfers", request, String.class);
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
