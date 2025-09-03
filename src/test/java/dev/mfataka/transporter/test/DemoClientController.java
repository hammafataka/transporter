package dev.mfataka.transporter.test;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import reactor.core.publisher.Mono;

/**
 * @author HAMMA FATAKA
 */

public interface DemoClientController {

    @GetExchange(url = "/handshake")
    Mono<String> handshake();


    @GetExchange(url = "/greeting/{name}")
    Mono<String> greet(@PathVariable(name = "name") final String name);

}
