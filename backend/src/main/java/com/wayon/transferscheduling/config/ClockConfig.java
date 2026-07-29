package com.wayon.transferscheduling.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Expor o relogio como bean, em vez de chamar LocalDate.now() direto no
 * servico, permite fixar "hoje" em teste (Clock.fixed) e trocar o fuso por
 * configuracao — a data de agendamento e a base do calculo da taxa, entao
 * precisa ser determinavel e testavel.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${app.scheduling.time-zone}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }

}
