package util;

import io.restassured.RestAssured;
import io.restassured.http.Method;

public class APIUtil {

    public static void assertStatusCodeForRequest(int expectedStatusCode, Method method, String endpoint, String tokenOverride) {
        if (tokenOverride == null || tokenOverride.isEmpty()) {
            RestAssured.given()
                    .when()
                    .request(method, endpoint)
                    .then()
                    .assertThat()
                    .statusCode(expectedStatusCode);
        } else {
            RestAssured.given()
                    .auth().oauth2(tokenOverride)
                    .when()
                    .request(method, endpoint)
                    .then()
                    .assertThat()
                    .statusCode(expectedStatusCode);
        }
    }
}

