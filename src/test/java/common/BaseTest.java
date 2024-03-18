package common;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;

public class BaseTest {
    private static final String TOKEN = System.getenv("BEARER_TOKEN");
    private static final String TEST_DOMAIN = System.getenv("TEST_DOMAIN") != null ? System.getenv("TEST_DOMAIN") : "http://fluency-dev.com";
    private static final String API_PREFIX = System.getenv("API_PREFIX") != null ? System.getenv("API_PREFIX") : "/api/v2/";
    @BeforeSuite
    public static void setup() {
        RestAssured.baseURI = TEST_DOMAIN + API_PREFIX;
        RestAssured.authentication = RestAssured.oauth2(TOKEN);
    }
}
