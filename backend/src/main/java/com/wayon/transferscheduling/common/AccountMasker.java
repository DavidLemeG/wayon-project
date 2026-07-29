package com.wayon.transferscheduling.common;

/**
 * Mascara numeros de conta para uso em log.
 *
 * <p>Log de sistema financeiro costuma ser agregado, retido por muito tempo e
 * lido por gente que nao teria acesso ao dado em si — numero de conta completo
 * em texto puro no log e um vazamento silencioso. Mantem apenas os 4 ultimos
 * digitos, o suficiente para correlacionar um agendamento com o registro no
 * banco durante uma investigacao.
 */
public final class AccountMasker {

    private static final int VISIBLE_DIGITS = 4;
    private static final String FULLY_MASKED = "****";

    private AccountMasker() {
    }

    public static String mask(String account) {
        if (account == null || account.length() <= VISIBLE_DIGITS) {
            return FULLY_MASKED;
        }

        int maskedLength = account.length() - VISIBLE_DIGITS;
        return "*".repeat(maskedLength) + account.substring(maskedLength);
    }

}
