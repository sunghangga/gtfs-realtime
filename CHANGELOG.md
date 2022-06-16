# Change Log
All notable changes to this project will be documented in this file.

### Version [1.4.0] - 2022-06-03

#### Added:
- Add API for next-bus
- Add API for bus-schedule


---


### Version [1.3.4] - 2022-05-06

#### Change:
- Disable feature for collecting vehicle id. Vehicle id data will be given by the user.
- Change back spring.task.scheduling.pool.size value to 4


---


### Version [1.3.3] - 2022-04-20

#### Added:
- Add new scheduled function for collecting vehicle id

#### Change:
- Change spring.task.scheduling.pool.size value to 5 for additional schedule


---


### Version [1.3.2] - 2022-04-18

#### Changed:
- Add parameter "approx" for API SM


---


### Version [1.3.1] - 2022-04-13

#### Added:
- Add CHANGELOG file.
- Add **CANCELED** trip and **SKIPPED** stop in the Stop Monitoring API response.

#### Changed:
- Change id type from integer to long for all GTFS realtime entity to increase maximum value of primary key.

#### Fixed:
- Fix bug of duplicate trip data when inserting realtime data by removing @Async annotation to scheduler method and increase pool size value for task scheduler to make all scheduler run at the same time..
- Fix vehicle label data in Stop Monitoring API for null value.


---


### Version [1.3.0] - 2022-03-31

#### Added:
- Add README file.
- Add _arrival delay_ and _departure delay_ to the Stop Monitoring API response.

#### Changed:
- Change hour format in Stop Monitoring API from 12-hour to 24-hour format.
- Modify code and query to support GTFS Realtime data from VIA and Translink.
- Change code for consuming realtime data from using dedicated thread for each url to using scheduler.

#### Removed:
- Remove timezone filter in Stop Monitoring query.


---


### Version [1.2.0] - 2022-02-28

#### Changed:
- Change the number of parameter for deleting data in the database when updating realtime data.

#### Fixed:
- Optimize code for out of memory and java heap size issue.


---


### Version [1.1.0] - 2022-01-31

#### Added:
- Add docker installation.
- Add dummy data for Stop Monitoring API.
- Add feature for deleting old realtime data periodically in a given time range.

#### Changed:
- Change logic by deleting previous realtime data while inserting the latest realtime data to prevent duplicate realtime data in the database.

#### Fixed:
- Realtime data stop consuming when thread is die of error. Fixed by changing code logic to prevent consumer thread to die when error occurred.


---


### Version [1.0.0] - 2021-12-31

Initial release:

- Provide feature for consuming GTFS-Realtime data (trip-updates, vehicle-position, alert) from any GTFS-RT feeder.
- Provide API for Stop-Monitoring.