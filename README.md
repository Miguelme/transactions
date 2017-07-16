# Concurrent Transactions Handler

This repo has a spring application to create a concurrent transaction handler to store real time data of the last 60 seconds while offering two endpoints that have a complexity time of O(1) in memory and time

# Endpoints
- ## GET /statistics

This is the endpoint to retrieve the statistics of the last 60 seconds

- ## POST /statistics

This is the endpoint to add a new transaction to the handler.

### Running
In order to run the application please run the following command: 

```
$ mvn spring-boot:run
```
### Testing
In order to test the app you can run the following command: 

```
$ mvn verify
```

### Tools
In order to develop this Spring Boot Application the following tools have been used: 

- Maven 3.5.0
- Java 8
- Lombok
- Spring Boot 1.4.0