package com.verapi.abyss.echo.server;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.launcher.VertxCommandLauncher;
import io.vertx.core.impl.launcher.VertxLifecycleHooks;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxInfluxDbOptions;
import io.vertx.micrometer.VertxJmxMetricsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Launcher extends VertxCommandLauncher implements VertxLifecycleHooks {

    private Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {

        //enforce SLF4J logging set
        if (null == System.getProperty("vertx.logger-delegate-factory-class-name"))
            System.setProperty("vertx.logger-delegate-factory-class-name", io.vertx.core.logging.SLF4JLogDelegateFactory.class.getCanonicalName());

        new Launcher().dispatch(args);
    }

    @Override
    public void afterConfigParsed(JsonObject config) {

    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {

        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        ConfigStoreOptions configStoreOptions = new ConfigStoreOptions()
                .setType("env")
                .setConfig(new JsonObject().put("raw-data", true));

        ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
                .addStore(configStoreOptions);

        Vertx vertx = Vertx.vertx();
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions);

        configRetriever.getConfig(event -> {

            if (event.failed()) {
                vertx.close();
                logger.error(event.cause().getLocalizedMessage());
                future.completeExceptionally(event.cause());
                throw new RuntimeException(event.cause());
            } else {
                String configFilePath = event.result().getString("ABYSS_ECHOSERVER_CONFIG");
                logger.info("loading config file [{}]", configFilePath);

                ConfigStoreOptions fileConfigStoreOptions = new ConfigStoreOptions()
                        .setType("file")
                        .setFormat("properties")
                        .setConfig(new JsonObject().put("path", configFilePath));
                ConfigRetrieverOptions fileConfigRetrieverOptions = new ConfigRetrieverOptions()
                        .addStore(fileConfigStoreOptions);
                ConfigRetriever fileConfigRetriever = ConfigRetriever.create(vertx, fileConfigRetrieverOptions);
                fileConfigRetriever.getConfig(fileConfigEvent -> {
                    if (fileConfigEvent.failed()) {
                        vertx.close();
                        logger.info("unable to load config file [{}]", configFilePath);
                        logger.error(fileConfigEvent.cause().getLocalizedMessage());
                        future.completeExceptionally(fileConfigEvent.cause());
                        System.exit(1);
                    } else {
                        logger.info("successfully loaded config file [{}]", configFilePath);
                        logger.info(fileConfigEvent.result().encodePrettily());
                        options.setMetricsOptions(new MicrometerMetricsOptions()
                                .setJmxMetricsOptions(new VertxJmxMetricsOptions()
                                        .setStep(10)
                                        .setDomain("abyss.echo.server")
                                        .setEnabled(true))
                                .setInfluxDbOptions(
                                        new VertxInfluxDbOptions()
                                                .setUri(fileConfigEvent.result().getString("influxdb.uri"))
                                                .setDb(fileConfigEvent.result().getString("influxdb.dbname"))
                                                .setUserName(fileConfigEvent.result().getString("influxdb.dbuser.name"))
                                                .setPassword(fileConfigEvent.result().getString("influxdb.dbuser.password"))
                                                .setEnabled(fileConfigEvent.result().getBoolean("influxdb.logger.enabled"))
                                )
                                .setEnabled(true));
                        vertx.close();
                        future.complete(fileConfigEvent.result());
                    }
                });
            }
        });
        //wait till asynch code block completed to set metrics framework settings
        try {
            future.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {

    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {

    }

    @Override
    public void beforeStoppingVertx(Vertx vertx) {

    }

    @Override
    public void afterStoppingVertx() {

    }

    @Override
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        // Default behaviour is to close Vert.x if the deploy failed
        logger.error(cause.getLocalizedMessage());
        vertx.close();
    }
}
