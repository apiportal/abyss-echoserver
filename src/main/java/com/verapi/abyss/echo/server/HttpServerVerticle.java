/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
