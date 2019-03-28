# Vert.x Example Application

This application creates a fat jar which includes a basic endpoint that returns a short string.

Source: https://vertx.io/blog/my-first-vert-x-3-application/

## Build and Run
In order to create a fat jar package, install jdk >= 8 and Maven; afterwards, run this command:

```bash
mvn clean package
```

For configuring the app:

Create a properties file using sample vertx-echoserver-config.properties file
Create an environment variable named VERTX_ECHOSERVER_CONFIG pointing to your properties file

For running the app:

```bash
java -jar target/my-first-app-1.0-SNAPSHOT-fat.jar -instances {numberOfInstance}
```

