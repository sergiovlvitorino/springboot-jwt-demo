package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return a list of users"

    request {
        method GET()
        urlPath('/rest/user') {
            queryParameters {
                parameter "pageNumber": "0"
                parameter "pageSize": "10"
                parameter "orderBy": "name"
                parameter "asc": "true"
            }
        }
        headers {
            // The Authorization header is included for documentation purposes,
            // but the test setup will bypass actual security checks.
            header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9...")
        }
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
            content: [[
                id: "123e4567-e89b-12d3-a456-426614174000",
                name: "John Doe",
                email: "john@example.com"
            ]],
            totalElements: 1,
            totalPages: 1,
            last: true,
            first: true,
            empty: false
        )
    }
}
