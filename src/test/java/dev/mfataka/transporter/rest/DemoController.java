package dev.mfataka.transporter.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 18.12.2023 14:28
 */
@Slf4j
@RestController
@RequestMapping(path = "/demo")
public class DemoController {

    @GetMapping(path = "/handshake")
    public Mono<String> handshake() {
        return Mono.just("OK");
    }


    @GetMapping(path = "/greeting/{name}")
    public Mono<String> greet(@PathVariable(name = "name") final String name) {
        return Mono.just("hello " + name);
    }
}
