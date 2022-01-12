FROM openjdk:17.0.1
COPY build/libs/helloKtor-1.0-SNAPSHOT-all.jar helloKtor-1.0-SNAPSHOT-all.jar
EXPOSE 8080
CMD ["java", "-jar", "-Xms16M", "-Xmx16M",  "helloKtor-1.0-SNAPSHOT-all.jar"]