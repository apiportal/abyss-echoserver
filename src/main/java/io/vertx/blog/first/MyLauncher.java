package io.vertx.blog.first;

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

public class MyLauncher extends VertxCommandLauncher implements VertxLifecycleHooks {

    private Logger logger = LoggerFactory.getLogger(MyLauncher.class);

    public static void main(String[] args) {

        //enforce SLF4J logging set
        if (null == System.getProperty("vertx.logger-delegate-factory-class-name"))
            System.setProperty("vertx.logger-delegate-factory-class-name", io.vertx.core.logging.SLF4JLogDelegateFactory.class.getCanonicalName());

        new MyLauncher().dispatch(args);
    }

    @Override
    public void afterConfigParsed(JsonObject config) {

    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {

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
                throw new RuntimeException(event.cause());
            } else {
                String configFilePath = event.result().getString("VERTX_ECHOSERVER_CONFIG");
                logger.info(configFilePath);

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
                        logger.error(fileConfigEvent.cause().getLocalizedMessage());
                        System.exit(1);
                    } else {
                        logger.info(fileConfigEvent.result().encodePrettily());
                        options.setMetricsOptions(new MicrometerMetricsOptions()
                                .setJmxMetricsOptions(new VertxJmxMetricsOptions()
                                        .setStep(10)
                                        .setDomain("vertx-echo-server")
                                        .setEnabled(true))
                                .setInfluxDbOptions(
                                        new VertxInfluxDbOptions()
                                                .setUri(fileConfigEvent.result().getString("influxdb.uri"))
                                                .setDb(fileConfigEvent.result().getString("influxdb.dbname"))
                                                .setUserName(fileConfigEvent.result().getString("influxdb.dbuser.name"))
                                                .setPassword(fileConfigEvent.result().getString("influxdb.dbuser.password"))
                                                .setEnabled((fileConfigEvent.result().getBoolean("influxdb.logger.enabled")))
                                )
                                .setEnabled(true));
                        vertx.close();
                    }
                });
            }
        });


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
