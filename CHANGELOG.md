# Change Log
All notable changes to this project will be documented in this file.

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
- Change the number of parameter for deleting data when updating realtime data.

#### Fixed:
- Optimize code for out of memory and java heap size issue.


---


### Version [1.1.0] - 2022-01-31

#### Added:
- Add docker installation.
- Add dummy data for Stop Monitoring API.
- Add feature for deleting old realtime data in a given time range.

#### Changed:
- Change logic while inserting realtime data by deleting previous realtime data to prevent duplicate realtime data.

#### Fixed:
- Fix bug of realtime data stop consuming by changing code logic to prevent consumer thread to die when error occurred.


---


### Version [1.0.0] - 2021-12-31

Initial release.