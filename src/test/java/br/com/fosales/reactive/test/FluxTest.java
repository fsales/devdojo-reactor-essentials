package br.com.fosales.reactive.test;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
public class FluxTest {

    @Test
    void fluxSubscriber(){
        var listaNomes = new String[]{"Fábio", "Rose", "Arthur"};

        var fluxString =Flux.just(listaNomes).log();


        StepVerifier
                .create(fluxString)
                .expectNext(listaNomes)
                .verifyComplete();


    }

    @Test
    void fluxSubscriberNumbers(){
        var listaNomes = new Integer[]{1,2,3,4,5};

        var flux =Flux.range(1,5).log();


        flux.subscribe(i-> log.info("Number: {}", i));

        StepVerifier
                .create(flux)
                .expectNext(listaNomes)
                .verifyComplete();


    }

    @Test
    void fluxSubscriberFromList(){
        var listaNomes = List.of(1,2,3,4,5);

        var flux =Flux.fromIterable(listaNomes).log();


        flux.subscribe(i-> log.info("Number: {}", i));

        StepVerifier
                .create(flux)
                .expectNext(1,2,3,4,5)
                .verifyComplete();


    }

    @Test
    void fluxSubscriberNumberError(){
        var listaNomes = new Integer[]{1,2,3,4,5};

        var flux = Flux.range(1,5)
                .log()
                .map( i -> {
                    if( i == 4){
                        throw new IndexOutOfBoundsException("index erro");
                    }

                    return i;
                });


        flux.subscribe(
                i-> log.info("Number: {}", i),
                Throwable::printStackTrace,
                ()-> log.info("Done"),
                subscription -> subscription.request(5)
        );

        StepVerifier
                .create(flux)
                .expectNext(1,2,3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();


    }

    @Test
    void fluxSubscriberNumberUglyBackpressure() {
        var flux = Flux.range(1, 10)
                .log()
                .map(i -> {
                    if (i == 4) {
                        throw new IndexOutOfBoundsException("index erro");
                    }

                    return i;
                });


        flux.subscribe(
                new Subscriber<Integer>() {

                    private final int requestCount = 2;
                    private int count;
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(2);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        count++;
                        if (count >= requestCount) {
                            count = 0;
                            subscription.request(requestCount);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }
        );

        StepVerifier
                .create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();


    }

    @Test
    void fluxSubscriberNumberNotSoUglyBackpressure(){
        var flux = Flux.range(1,10)
                .log()
                .map( i -> {
                    if( i == 4){
                        throw new IndexOutOfBoundsException("index erro");
                    }

                    return i;
                });


        flux.subscribe(
                new BaseSubscriber<Integer>(){

                    private int count;

                    private final int requestCount = 2;

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(requestCount);
                    }

                    @Override
                    protected void hookOnNext(Integer value) {
                        count++;
                        if (count >= requestCount){
                            count = 0;
                            request(requestCount);
                        }
                    }
                }
        );

        StepVerifier
                .create(flux)
                .expectNext(1,2,3,4,5,6,7,8,9,10)
                .verifyComplete();


    }
}
