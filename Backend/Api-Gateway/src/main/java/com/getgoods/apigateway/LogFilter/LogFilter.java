package com.getgoods.apigateway.LogFilter;

import org.springframework.web.server.ServerWebExchange;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;


public class LogFilter implements GlobalFilter{
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("Pre Procesing Logic "+exchange.getRequest().getURI());
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            System.out.println("Post Processing Logic "+exchange.getResponse().toString());
            System.out.println("Post Processing Logic "+exchange.getResponse().getStatusCode());
        }));
    }
}