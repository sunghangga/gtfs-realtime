# GTFS-REALTIME

Spring boot application that collects GTFS-realtime data (trip updates, vehicle positions and alerts) from available feed url and save it to the database.

## Installation

#### Clone repository:

````
git clone https://dewa_maestronic@bitbucket.org/dewa_maestronic/gtfs-realtime.git
````

#### Get postgres database from [here](https://maestronicoperationsltd-my.sharepoint.com/:f:/g/personal/angga_putra_maes-electronic_co_id/Eng41HxYYAVFrI5Giz13bCwB24bDxE5CnJNqlDOZUazTDQ?e=RhL27U)
#### Open project using IntelliJ or other java IDE
#### Set the database connection in the application.properties file:

````
spring.datasource.url=jdbc:postgresql://hostname:port/database_name
spring.datasource.username=user_name
spring.datasource.password=password
````

#### Run project from the IDE or build the app into jar file using maven command:

````
mvn clean package

java -jar target/gtfs-realtime-0.0.1-SNAPSHOT.jar
````

#### Test the API using postman or directly from the browser:

````
hostname:port/api/gtfs/stopmonitoring?agency_id=xxx&format=json
````

#### Full API documentation are available [here](https://maestronicoperationsltd-my.sharepoint.com/:w:/g/personal/angga_putra_maes-electronic_co_id/EQprBGgAZF1Bnm8ou-L2YmUBXblS9Nmulo4kf9QagtwD7w)