package com.verapi.abyss.echo.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

    private static Logger logger = LoggerFactory.getLogger(Launcher.class);

    @Override
    public void start(Future<Void> fut) {
        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setCompressionSupported(false)
                .setAcceptBacklog(1000000)
                .setReuseAddress(true)
                .setTcpKeepAlive(true)
                .setUsePooledBuffers(true);
        vertx
                .createHttpServer(httpServerOptions)
                .exceptionHandler(event -> logger.error(event.getLocalizedMessage()))
                .requestHandler(r -> {
                    r.response().end("<h1>Hello</h1>");
                })
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }
}
