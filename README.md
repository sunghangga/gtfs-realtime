# GTFS-Realtime

This app collects GTFS-Realtime data (trip updates, vehicle positions and alerts) from available feed url and save it to the database.

## Installation

#### 1. Clone repository:

```
git clone https://dewa_maestronic@bitbucket.org/dewa_maestronic/gtfs-realtime.git
```

#### 2. Open project using IntelliJ or other java IDE
#### 3. Get postgres database from [here](https://maestronicoperationsltd-my.sharepoint.com/:f:/g/personal/angga_putra_maes-electronic_co_id/Eng41HxYYAVFrI5Giz13bCwB24bDxE5CnJNqlDOZUazTDQ?e=RhL27U)
#### 4. Set database connection in the _application.properties_ file:

```
spring.datasource.url=jdbc:postgresql://hostname:port/database_name
spring.datasource.username=username
spring.datasource.password=password
```

#### 5. Set up the settings for GTFS-realtime feed
The settings are provided in the _application.properties_ file. 
Currently, there are 2 feed url available, Ovapi (Netherlands) and VIA (US). 
Choose only one by commenting other settings:

```
# USER CONFIG
## NL
url.trip-update=http://gtfs.ovapi.nl/nl/tripUpdates.pb
url.vehicle-position=http://gtfs.ovapi.nl/nl/vehiclePositions.pb
url.alert=http://gtfs.ovapi.nl/nl/alerts.pb
timezone=Europe/Amsterdam
agency-filter=CXX

## VIA
url.trip-update=http://gtfs.viainfo.net/tripupdate/tripupdates.pb
url.vehicle-position=http://gtfs.viainfo.net/vehicle/vehiclepositions.pb
url.alert=http://gtfs.viainfo.net/alert/alerts.pb
timezone=America/Chicago
agency-filter=
```

## Test run

#### 1. Build the app into jar file using maven command:

```
mvn clean package
```

#### 2. Execute jar file using command:

```
java -jar target/gtfs-realtime-0.0.1-SNAPSHOT.jar
```

The app will automatically collect all GTFS-Realtime data and save it into the database.

#### 3. Test the API using postman or directly from the browser using url:

```
hostname:port/api/gtfs/stopmonitoring?agency_id=xxx&format=json
```

The API will return stop monitoring data from xxx agency (user/operator) in json format.

> In order to get the data from the API, make sure GTFS-Static data already imported to the database.
> Otherwise, no data will be available for response.
> GTFS-Realtime data need to be joined with the GTFS-Static data in the query.

## Docker

For testing using docker, there are _Dockerfile_ and _docker-compose_ file available in the project directory.

#### 1. Set the environment value in the _docker-compose_ file:

The default values are the same as the values in the _application.properties_ file. Change the values as needed.

```
environment:
    - SPRING_DATASOURCE_URL=jdbc:postgresql://gtfs-db-test:port/database_name
    - SPRING_DATASOURCE_USERNAME=username
    - SPRING_DATASOURCE_PASSWORD=password
    - SERVER_PORT=8080
    - FIXEDDELAY_IN_MILLISECONDS=20000
    - DELETE_REALTIME_START_DAY=1
    - CRON_EXPRESSION_DELETE_REALTIME=@hourly
```

```
environment:
    - POSTGRES_USER=username
    - POSTGRES_PASSWORD=password
    - POSTGRES_DB=database_name
```

#### 2. Set directory for sql file to be imported to the postgres container in the _docker-compose_ file:

```
volumes:
    - target_dir/sql_file.sql:/docker-entrypoint-initdb.d/sql_file.sql
```

#### 3. Run docker by executing command:

```
docker-compose up --build / docker-compose up --build -d
```

Docker will download postgres image if not exist and build image for the app and creates container for both image.

App will collect data after the docker-compose process complete.

#### 4. Test the API

--- 
> Full API documentation are available [here](https://maestronicoperationsltd-my.sharepoint.com/:w:/g/personal/angga_putra_maes-electronic_co_id/EQprBGgAZF1Bnm8ou-L2YmUBXblS9Nmulo4kf9QagtwD7w)