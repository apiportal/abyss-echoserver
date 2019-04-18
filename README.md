# Abyss Echo Server Application

This application creates a fat jar which includes a basic endpoint that returns 
``` xml
<h1>Hello</h1>.
```
listening on port 8080

This application collects metrics and reports to InfluxDB


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

vertx-echoserver-config.properties file should be configured properly to enable InfluxDB reporting 

```properties
#InfluxDB settings
influxdb.logger.enabled=true
influxdb.uri=http://influxdburi:8086
influxdb.dbname=dbname
influxdb.dbuser.name=dbusername
influxdb.dbuser.password=password
```