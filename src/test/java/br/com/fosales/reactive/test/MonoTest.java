package br.com.fosales.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@Slf4j
public class MonoTest {

    @Test
    void monoSubscribe(){
        String nome = "Fábio";

        Mono<String> mono = Mono.<String>just(nome).log();

        mono.subscribe();
        log.info("mono: {}", mono);
    }
}
