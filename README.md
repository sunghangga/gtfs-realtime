# GTFS-REALTIME

This app collects GTFS-Realtime data (trip updates, vehicle positions and alerts) from available feed url and save it to the database.

## Installation

#### 1. Clone repository:

```
git clone https://dewa_maestronic@bitbucket.org/dewa_maestronic/gtfs-realtime.git
```

#### 2. Get postgres database from [here](https://maestronicoperationsltd-my.sharepoint.com/:f:/g/personal/angga_putra_maes-electronic_co_id/Eng41HxYYAVFrI5Giz13bCwB24bDxE5CnJNqlDOZUazTDQ?e=RhL27U)
#### 3. Open project using IntelliJ or other java IDE
#### 4. Set database connection in the application.properties file:

```
spring.datasource.url=jdbc:postgresql://hostname:port/database_name
spring.datasource.username=user_name
spring.datasource.password=password
```

#### 5. Set up the settings for GTFS-realtime feed. 
The settings are provided in the application.properties file. 
Currently, there are 2 feed url available from 2 datasource, Ovapi (Netherlands) and VIA (US). 
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

#### 1. Run project from the IDE or build the app into jar file using maven command:

```
mvn clean package
```
```
java -jar target/gtfs-realtime-0.0.1-SNAPSHOT.jar
```

#### 2. Test the API using postman or directly from the browser:

```
hostname:port/api/gtfs/stopmonitoring?agency_id=xxx&format=json
```

Full API documentation are available [here](https://maestronicoperationsltd-my.sharepoint.com/:w:/g/personal/angga_putra_maes-electronic_co_id/EQprBGgAZF1Bnm8ou-L2YmUBXblS9Nmulo4kf9QagtwD7w)