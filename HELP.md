# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.avensys.rts.user-service' is invalid and this project uses 'com.avensys.rts.userservice' instead.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.1.2/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.1.2/maven-plugin/reference/html/#build-image)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#using.devtools)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#web)
* [Spring Session](https://docs.spring.io/spring-session/reference/)
* [Docker Compose Support](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#features.docker-compose)
* [Spring Security](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#web.security)
* [OAuth2 Client](https://docs.spring.io/spring-boot/docs/3.1.2/reference/htmlsingle/index.html#web.security.oauth2.client)
* [Eureka Discovery Client](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#service-discovery-eureka-clients)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Service Registration and Discovery with Eureka and Spring Cloud](https://spring.io/guides/gs/service-registration-and-discovery/)

### Docker Compose support
This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined:

* postgres: [`postgres:latest`](https://hub.docker.com/_/postgres)

Please review the tags of the used images and set them to the same as you're running in production.

