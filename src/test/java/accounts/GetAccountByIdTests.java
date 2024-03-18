package accounts;

import io.restassured.RestAssured;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.restassured.http.Method;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import common.BaseTest;
import util.APIUtil;
import util.AccountFileUtil;

public class GetAccountByIdTests extends BaseTest {
    String basePath = "accounts/";
    int accountId = 16;
    private final String defaultEndpoint = basePath + accountId;

    @Test
    void testReturnsValidResponse() throws IOException, JSONException {
        String expectedJsonFilePath = String.format("src/test/resources/json/accounts/%d.json", accountId);
        String expectedJson = AccountFileUtil.readJsonFromFile(expectedJsonFilePath);
        String actualJson = fetchAccountJson(accountId);

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    void testNoAuthReturns401() {
        String validTokenWithNoAuthToDefaultAccountId = "notForYou";
        APIUtil.assertStatusCodeForRequest(401, Method.GET, defaultEndpoint, validTokenWithNoAuthToDefaultAccountId);
    }

    @Test
    void testNonIntegerAccountIdReturns400() {
        String nonIntegerEndpoint = basePath + "qwerty789";
        RestAssured.given()
                .when()
                .get(nonIntegerEndpoint)
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    void testPutReturns405() {
        APIUtil.assertStatusCodeForRequest(405, Method.PUT, defaultEndpoint, null);
    }

//    void testPostReturns405() {}
//    void testDeleteReturns405() {}
//    void testAnyOtherVerbsWeChooseToTestReturnAProperCode() {}

    @Test
    void testInvalidTokenReturns403() {
        String badToken = "notFluent987";
        APIUtil.assertStatusCodeForRequest(403, Method.GET, defaultEndpoint, badToken);
    }

    @Test
    void testNegativeAccountIdReturns404() {
        String localEndpoint = basePath + "-16";
        APIUtil.assertStatusCodeForRequest(404, Method.GET, localEndpoint, null);
    }

    @Test
    void testPerformance() {
        // This test could easily be expanded to verify results, resulting in a load test of sorts, but I wanted to
        // keep this specific one a true performance test.
        int numberOfRequests = 100;
        long maxAllowedTimeInMillis = 1000;

        long startTime = System.currentTimeMillis();

        for(int i = 0; i < numberOfRequests; i++) {
            RestAssured.given()
                    .when()
                    .get(defaultEndpoint)
                    .then()
                    .and()
                    .contentType("application/json");
        }

        long totalTimeTaken = System.currentTimeMillis() - startTime;
        long averageTimeTaken = totalTimeTaken / numberOfRequests;

        assertTrue(averageTimeTaken <= maxAllowedTimeInMillis);
    }

    @Test
    void testConcurrency() throws IOException {
        String accountDirectoryPath = "src/test/resources/json/accounts";
        List<Integer> accountIds = AccountFileUtil.fetchAccountIdsFromFilenames(accountDirectoryPath);

        int numberOfThreads = accountIds.size();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger();

        for(Integer accountId : accountIds) {
            String expectedJsonFilePath = String.format("src/test/resources/json/accounts/%d.json", accountId);
            String expectedJson = AccountFileUtil.readJsonFromFile(expectedJsonFilePath);

            executorService.submit(() -> {
                String actualJson = fetchAccountJson(accountId);

                try {
                    JSONAssert.assertEquals(expectedJson, actualJson, false);
                    successCount.incrementAndGet();
                } catch (AssertionError | JSONException e) {
                    assertEquals(actualJson, expectedJson,
                            "Response JSON for accountId " + accountId + " did not match the expected JSON.");
                }
            });
        }
    }

    private String fetchAccountJson(int accountId) {
        String dynamicEndpoint = basePath + accountId;
        return RestAssured.given()
                .when()
                .get(dynamicEndpoint)
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .contentType("application/json")
                .extract()
                .asString();
    }
}
