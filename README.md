# Twitter Topics

Java console app for streaming topics from Twitter and writing them into a file.

## Installation

#### Clone the Github repository
```sh
$ git clone https://github.com/ianong/twitter-topic.git
```

#### Twitter App and Configuration
1. Login to https://apps.twitter.com
2. Create a New App and note down the *Consumer Key, Consumer Secret, Access Token and Access Token Secret*. 
3. Edit the `/src/main/resources/application.properties` and add above noted keys.

#### Package the application
This creates `twitter-topic-1.0-jar-with-dependencies.jar` file inside the target folder.
```sh
$ mvn clean install
```

#### Run the application
```sh
$ java -jar twitter-topic-1.0-jar-with-dependencies.jar
```
You can override the configuration specified in `/src/main/resources/application.properties` by adding a file named `application.properties` beside the jar.
```$xslt
 |__ twitter-topic-1.0-jar-with-dependencies.jar
 |__ application.properties
```
