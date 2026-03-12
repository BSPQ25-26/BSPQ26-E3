BSPQ26-E3 - Plant Hub app
=========================

This example relies on the SpringBoot to create a REST API and connecting to a MySQL database using JPA, with a sample web application and a command line client.

The basic structure of a SpringBoot project can be initialized using *Spring initializr* at https://start.spring.io/.

Launching the application
-------------------------

First, check all the required dependencies specified by the pom.xml file and the database configuration contained in *src/main/resources/application.properties* file.

Now, run the following command to check to download all dependencies and check that everything correctly compiles

      mvn compile

Make sure that the MySQL database was correctly configured before connecting the application to use it. 
Use the contents of the file *src/main/resources/dbsetup.sql* to create the database, an specific user 'spq' (pass: 'spq') for the application and grant privileges to that user. If you are using the command line client for MySQL you could run the following command

      mysql –uroot -p < src/main/resources/dbsetup.sql

otherwise you could use the contents of the file in any other MySQL client you are using.

Now, launch the server using the following command

    mvn spring-boot:run

If there are no errors, you should get a running web application serving its contents at http://localhost:8080/. You can press Ctrl+C to stop the running application.

REST API
--------

The application exposes a REST API, which is used by the web application, which is implemented in the PlantHubController class. To execute these methods, you may install [POSTman app](https://learning.postman.com/docs/getting-started/first-steps/get-postman/) or [CURL command line tool](https://curl.se/). For example, some methods are

Retrieves all the registered PlantHubs

    GET http://localhost:8080/api/PlantHubs

Adds a new PlantHub to the database

    POST http://localhost:8080/api/PlantHubs
    Content-Type: application/json

    {
    "title": "Spring Boot in Action",
    "author": "Craig Walls",
    "isbn": "9781617292545"
    }

Removes a previously registered PlantHub

    DELETE http://localhost:8080/api/PlantHubs/1

To see the full list of methods from the REST API, you can visit Swagger interface at: http://localhost:8080/swagger-ui.html. Check the annotations in the *PlantHubController* class, the required dependencies in the *pom.xml* file and the *application.properties* file for its configuration

Command line client
-------------------

There is a sample REST API client implementation using the SpringBoot REST client libraries in class *PlantHubManager.java*. You can launch the client using the following Maven command (check)

    mvn exec:java

See <build> section in *pom.xml* to see how this command was configured to work.

Packaging the application
-------------------------

Application can be packaged executing the following command

    mvn package

including all the SpringBoot required libraries inside the *target/rest-api-0.0.1-SNAPSHOT.jar*, which can be distributed.

Once packaged, the server can be launched with

    java -jar rest-api-0.0.1-SNAPSHOT.jar

and the sample client by running, as SpringBoot changes the way the default Java loader

    java -cp rest-api-0.0.1-SNAPSHOT.jar -Dloader.main=com.example.restapi.client.PlantHubManager org.springframework.boot.loader.launch.PropertiesLauncher localhost 8080

Therefore, in a real development, it would be advisable to create different Maven projects for server and client applications, easing distribution and manteinance of each application separately.

References
----------

* Very good explaination of the project: https://medium.com/@pratik.941/building-rest-api-using-spring-boot-a-comprehensive-guide-3e9b6d7a8951 
* Building REST services with Spring: https://spring.io/guides/tutorials/rest
* Good example documenting how to generate Swagger APIs in Spring Boot: https://bell-sw.com/blog/documenting-rest-api-with-swagger-in-spring-boot-3/#mcetoc_1heq9ft3o1v 
* Docker example with Spring: https://medium.com/@yunuseulucay/end-to-end-spring-boot-with-mysql-and-docker-2c42a6e036c0



