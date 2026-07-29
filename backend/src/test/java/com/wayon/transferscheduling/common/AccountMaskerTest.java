package com.wayon.transferscheduling.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMaskerTest {

    @ParameterizedTest(name = "conta {0} deve ser mascarada como {1}")
    @CsvSource({
            "1234567890, ******7890",
            "9999999999, ******9999",
            "12345, *2345",
    })
    void mantemApenasOsQuatroUltimosDigitos(String account, String esperado) {
        assertThat(AccountMasker.mask(account)).isEqualTo(esperado);
    }

    @Test
    void contaCompletaNuncaAparecerNoResultado() {
        String account = "1232132132";

        String masked = AccountMasker.mask(account);

        assertThat(masked).doesNotContain(account);
        assertThat(masked).hasSameSizeAs(account);
    }

    @ParameterizedTest(name = "conta curta demais ({0}) deve ser totalmente mascarada")
    @ValueSource(strings = {"1234", "123", "1"})
    void mascaraTotalmenteQuandoNaoHaDigitosSuficientesParaEsconder(String account) {
        assertThat(AccountMasker.mask(account)).isEqualTo("****");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void mascaraTotalmenteQuandoContaENulaOuVazia(String account) {
        assertThat(AccountMasker.mask(account)).isEqualTo("****");
    }

}
