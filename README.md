# springboot-jwt-demo
This example shows a RESTFUL API using spring boot, spring security and Json Web Token(JWT). 

![Java CI with Maven](https://github.com/sergiovlvitorino/springboot-jwt-demo/workflows/Java%20CI%20with%20Maven/badge.svg)

[![codecov](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo/branch/master/graph/badge.svg)](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo)

## Getting Started

### Prerequisites
* JDK 18
* Maven 3.6+

### Endpoints
Method|Url|Body|Description|Returns|Authenticated?
------|---|----|-----------|-------|--------------
POST|http://localhost:8080/login|{username:,password:}|SignIn|JWT|No
POST|http://localhost:8080/rest/user|{name:,email:,password:,roleId:}|Create user|User json|Yes
PUT|http://localhost:8080/rest/user|{id:,name:}|Update user|User json|Yes
DELETE|http://localhost:8080/rest/user/{id}|null|Disable user|User json|Yes
GET|http://localhost:8080/rest/user?pageNumber=0&pageSize=1&orderBy=name&asc=true&user.enabled=true|null|User list|-|Yes
GET|http://localhost:8080/rest/user/count?user.enabled=true|null|Number of users|-|Yes
GET|http://localhost:8080/rest/role?pageNumber=0&pageSize=1&orderBy=name&asc=true&role.name=GUEST|null|Role list|-|Yes
GET|http://localhost:8080/rest/role/count?role.name=GUEST|null|Number of roles|-|Yes


### Running
Open the terminal. Put the commands below to download and start the project:

`git clone https://github.com/sergiovlvitorino/springboot-jwt-demo`

`cd springboot-jwt-demo`

`mvn spring-boot:run`


### Running tests
Open the terminal. Put the commands below to test:

`cd springboot-jwt-demo`

`mvn test`


### Variables
username:

`abc@def.com`

password:

`123456`


## Authors

* **Sergio Vitorino** - (https://github.com/sergiovlvitorino)

See also the list of [contributors](https://github.com/sergiovlvitorino/springboot-jwt-demo/contributors) who participated in this project.

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE.md](LICENSE.md) file for details
