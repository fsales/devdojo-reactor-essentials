package br.com.fosales.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class MonoTest {

    @Test
    void monoSubscribe() {
        String nome = "Fábio";

        var mono = Mono.just(nome).log();
        mono.subscribe();

        log.info("-------------------");

        StepVerifier.create(mono)
                .expectNext("Fábio")
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumer() {
        String nome = "Fábio";

        var mono = Mono.just(nome).log();
        mono.subscribe(s -> log.info("valor: {}", s));

        log.info("-------------------");

        StepVerifier.create(mono)
                .expectNext("Fábio")
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumerError() {
        String nome = "Fábio";

        var mono = Mono.just(nome).map(s -> {
            throw new RuntimeException("Testing mono with error");
        });

        mono.subscribe(s -> log.info("Name: {}", s), s -> log.error("Something bad happened"));

        mono.subscribe(s -> log.info("Name: {}", s), Throwable::printStackTrace);

        log.info("-------------------");

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void monoSubscriberConsumerComplete() {
        String nome = "Fábio";

        var mono = Mono.just(nome)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(
                s -> log.info("valor: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED")
        );

        log.info("-------------------");

        StepVerifier.create(mono)
                .expectNext(nome.toUpperCase())
                .verifyComplete();
    }

    @Test
    void monoSubscriberConsumerSubscription() {
        String nome = "Fábio";

        var mono = Mono.just(nome)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(
                s -> log.info("valor: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED"),
                subscription -> subscription.request(5)
        );

        log.info("-------------------");

        StepVerifier.create(mono)
                .expectNext(nome.toUpperCase())
                .verifyComplete();
    }

    @Test
    void monoDoOnMethods() {
        String nome = "Fábio";

        var mono = Mono.just(nome)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("subscribed"))
                .doOnRequest(longNumber -> log.info("Request received, starting doing something..."))
                .doOnNext(s -> log.info("Value is here. executing doOnNext {}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. executing doOnNext {}", s)) // will not be executed
                .doOnSuccess(s -> log.info("doOnSuccess executed"));

        mono.subscribe(
                s -> log.info("valor: {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED")
        );

    }

    @Test
    void monoDoOnError() {
       var error = Mono.error(new IllegalArgumentException(""))
                .doOnError(e-> log.error("Error message: {}", e.getMessage()))
               .doOnNext(s-> log.info("Executing this doOnNext"))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();

    }

    @Test
    void monoDoOnErrorResume() {
        var name = "Fábio";
        var error = Mono.error(new IllegalArgumentException(""))
                .doOnError(e-> log.error("Error message: {}", e.getMessage()))
                .onErrorResume(s-> {
                    log.info("Executing this doOnNext");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    void monoDoOnErrorReturn() {
        var name = "Fábio";
        var error = Mono.error(new IllegalArgumentException(""))
                .doOnError(e-> log.error("Error message: {}", e.getMessage()))
                .onErrorReturn("EMPTY")
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();

    }
}
