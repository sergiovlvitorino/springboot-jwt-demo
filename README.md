# springboot-jwt-demo
This example shows how to implement spring boot, spring security and JWT.

[![Build Status](https://travis-ci.org/sergiovlvitorino/springboot-jwt-demo.svg?branch=master)](https://travis-ci.org/sergiovlvitorino/springboot-jwt-demo)

[![codecov](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo/branch/master/graph/badge.svg)](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo)

## Getting Started

### Prerequisites
* JDK 1.8
* Maven 3

### Endpoints
Method|Url|Body|Description|Returns|Authenticated?
------|---|----|-----------|-------|--------------
POST|http://localhost:8080/login|{username:,password:}|SignIn|JWT|No
POST|http://localhost:8080/user|{name:,email:,password:,roleId:}|Create user|User json|Yes
PUT|http://localhost:8080/user|{id:,name:}|Update user|User json|Yes
DELETE|http://localhost:8080/user/{id}|null|Disable user|User json|Yes
GET|http://localhost:8080/user?pageNumber=0&pageSize=1&orderBy=name&asc=true&user.enabled=true|null|Number of users|-|Yes
GET|http://localhost:8080/user/count?user.enabled=true|null|Number of users|-|Yes

### Running
Open the terminal. Put the commands below to download and start the project:

`git clone https://github.com/sergiovlvitorino/springboot-jwt-demo`

`cd springboot-jwt-demo`

`mvn spring-boot:run`


### Running tests
Open the terminal. Put the commands below to test:

`cd springboot-jwt-demo`

`mvn test`

## Authors

* **Sergio Vitorino** - (https://github.com/sergiovlvitorino)

See also the list of [contributors](https://github.com/sergiovlvitorino/springboot-jwt-demo/contributors) who participated in this project.

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE.md](LICENSE.md) file for details
