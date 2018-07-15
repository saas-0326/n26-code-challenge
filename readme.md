# N26 Java Code Challenge #

This project contains the solution for the Java Code Challenge at N26.

## Project Basics ##

### Basic Information ###

* Statistics API implementation for Java Code Challenge at N26.
* Version: **0.0.1-SNAPSHOT**
* This API consists basically on 2 operations: Register Transaction & Query Statistics
* Built using Spring Boot and Maven

### Relevant Dependencies ###

* Spring-Boot - 1.4.3.RELEASE (Web Container & IOC)
* GigaSpaces - 10.2.1-14000-RELEASE (In Memory Data Base)
* TestNG - 6.13.1 (Testing purposes)
* EasyMock - 3.4 (Testing purposes)

### Project Purpose ###

Restful API to calculate real time statistics from the last 60 seconds of the processed transactions. The API has 2 end points:
* POST /transactions - Registers a new transaction each time it happens with the time stamp and the amount.
* GET /statistics - Returns the statistics (such as average, sum, maximum, minimum, count) based on the amount of the transactions from the last 60 seconds.

### Download, Build & Run ###

* Clone the project from [the repository](https://github.com/saas-0326/n26-code-challenge).
* Build project with maven: `mvn clean package`.
* Run the api. It could be run in 2 different ways:
  - Maven: `mvn spring-boot:run`
  - CLI: `java -jar target/statistics-api-{version}.jar`
* By default, the API will run on port 8080, but it could be changed via command-line parameter. For example for port 9000: `-Dserver.port=9000`.
* By default, the API will create a (required) new Space for each process, but it could be changed to use an already created one via command-line parameter: `-Dstatistics.space.create=false`.

## Project Structure ##

### Persistency Layer ###

At the persistency level uses [Giga Spaces](https://www.gigaspaces.com/) as an In Memory Data Base, to store the required information from the transactions. Each time a transaction is written at the space, it is given a specific time to live (60 seconds starting from the transaction's time stamp) and when the expiration time comes, the transaction is removed from the space to avoid being included into the statistics.

This project doesn't have a DataBase (except for the in memory) but it would be easy to connect it to a Data Base using any hihgly-accepted ORM.
Historical information (more than 60 seconds) is not stored anywhere to avoid memory leaks, but if it was necessary, it would be easy to keep the transactions in the space forever or connect to a Data Base and store all the information both synchronously or asynchronously.

### Service Layer ###

At the service level, there is only 1 service (StatisticsService) in charge of validations and executing operations over the space. Since the business logic is not so complex, it doesn't make sense to have an isolated DAO layer.
Since the space only keeps the 'alive' transactions in memory, the statistics are taken from all the transactions in the space when the query is made, and all the transactions' amounts are aggregated to calculate the required information.

If historical information was to be stored for ever, then the query should be modified to look for only transactions from the last 60 seconds, and it would probably be a good idea to add a Space Index over the transaction's time stamp to make the query even faster.

### Web Layer ###

The web layer consists of a single controller, that exposes both end points (create transaction & query statistics) with only field's formatting validations plus response status code and information.

There is an isolated transaction model for the Web Layer with only the required information to make the requests, and at the persistency level the objects have more information.

At the web layer, there are also 2 more end points to query for the service information and health. Its endpoints are:
* GET /actuator/info - Service information
* GET /actuator/health - Service Health

### Testing ###

The project tests are divided in 2 big groups, Unit Tests & Integration Tests.

#### Unit Tests ###

- The Unit Tests are grouped by class and functionality.
- Unit tests are coded in classes with the 'MockTest' postfix.
- Unit tests are included into maven lyfe cycle via de [Sure Fire plugin](https://maven.apache.org/surefire/maven-surefire-plugin/).
- Unit tests run in maven as part of the 'test' phase of the build life cycle.
- Unit tests use mocks to simulate specific components behavior.
- Unit tests purpose is to test specific methods' functionality and inputs-outputs.

#### Integration Tests ###

- The Integration Tests are way more general than Unit Tests and cover the complete project integration.
- Integration tests are coded in classes with the 'IntegrationTest' postfix.
- Integration tests are included into maven lyfe cycle via de [Fail Safe plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/).
- Integration tests run in maven as part of the 'integration-test' & 'verify' phases of the build life cycle.
- Integration tests start the environment and context of the project, to have the complete scope for the tests.
- Integration tests purpose is to test the whole project functionality.
- There is a main integration test suite, in the 'StatisticsApiIntegrationTest' class, where the web end points are fully tested and covered.

## Project Decisions ##

### Giga Spaces ###

The use of an In Memory Data Grid was made on purpose, in order to be able to run multiple instances of the same service, with the same code base and still be able to keep transactions' and statistical information shared among instances. It's possible to run multiple instances (different ports on the same server) and share information, if running the second (and later) service with the 'create-space' flag with false.

Example:
* Run the first instance (might be with default values or specifying a port).
* Run the second (or later) instance with a different port and specifying not to create a new space (will connect to the one created on the first instance):

    $ java -Dserver.port=8080 -jar target/statistics-api-0.0.1-SNAPSHOT.jar

    $ java -Dserver.port=8090 -Dstatistics.space.create=false -jar target/statistics-api-0.0.1-SNAPSHOT.jar

### Local Maven Repository ###

Since Giga Spaces dependencies are not publicly available in a maven repository (like maven central) and it has a huge importance in this project's solution, it was necessary to install the required dependencies in a local repository and thus, add it to the source code repository.

If there is a problem building or deploying the project due to Giga Spaces dependencies not being found, please copy the libraries in "repo" folder to the maven repository or run the following 2 commands and it will install both dependencies on the local repository:

    mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file \
    -Dfile=repo/com/gigaspaces/gs-openspaces/10.2.1-14000-RELEASE/gs-openspaces-10.2.1-14000-RELEASE.jar \
    -DgroupId=com.gigaspaces -DartifactId=gs-openspaces -Dpackaging=jar -Dversion=10.2.1-14000-RELEASE -DlocalRepositoryPath=repo

    mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file \
    -Dfile=repo/com/gigaspaces/gs-runtime/10.2.1-14000-RELEASE/gs-runtime-10.2.1-14000-RELEASE.jar \
    -DgroupId=com.gigaspaces -DartifactId=gs-runtime -Dpackaging=jar -Dversion=10.2.1-14000-RELEASE -DlocalRepositoryPath=repo

### Spring Boot ###

The project is built using spring-boot, allowing the project to be self-contained and deployed isolated from any other web container (like Jboss, Tomcat, etc.). It also have some useful dependencies like Spring for IOC, Spring Test to ease building and running unit & integration tests.


