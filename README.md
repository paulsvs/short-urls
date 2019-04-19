## Short URLs
Application is using in memory DB, schema is created during startup and all the data is lost on shutdown.
User activity tracking is done by using cookies. Cookie is set on link creation and short link opening.
As of writing this only cookie is used for identifying users and their actions.</p>

There are no time limits or any other rules protecting created URLs they can be overwritten by the next request.
Additionally there is tracking for invalid/non existing short URLs. 

## Application setup
This is an application for creating short URLs. Interaction happens through REST API. 
To create a short url
````
curl -H 'Content-Type: application/json' -vd '{"shortPath":"test", "url":"http://google.com"}' localhost:8080/url/create
````
Open the created link to be redirected 
````
curl -H 'Content-Type: application/json' -v localhost:8080/test
````
For tracking results
````
curl -H 'Content-Type: application/json' -v localhost:8080/tracker/data
````
Take a look at the test for more details.


## Running
Requires Java 11.

To create package run `./mvnw clean package`

To run the package in Docker 
````
docker build . -t short-urls-image --build-arg JAR_FILE=target/*.jar
docker run -d -p 8080:8080 --name short-urls-container short-urls-image
````

To shut down and clean up
````
docker ps -a
docker stop short-urls-container

docker rm short-urls-container
docker images
docker rmi short-urls-image
````

