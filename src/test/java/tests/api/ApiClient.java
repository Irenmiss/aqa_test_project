package tests.api;

import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.UUID;

import tests.helpers.AllureHelper;

/**
 * REST-клиент для тестируемого приложения
 * REST client for the Application Under Test
 */

public final class ApiClient {

    static {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080; // port / порт приложения
    }

    private ApiClient() {}

    /**
     * Генерация токена (32 символа, uppercase)
     * Generate token (32 chars, uppercase)
     */
    public static String randomToken32() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 32)
                .toUpperCase();
    }

    /**
     * Универсальный вызов /endpoint.
     * Universal call to /endpoint.
     *
     * Автоматическое добавление:
     * - Allure step
     * - Request attachment
     * - Response attachment
     */
    public static Response callEndpoint(String apiKey, String token, String action) {
        Allure.parameter("action", action);
        Allure.parameter("token", token);
        Allure.parameter("apiKey", apiKey);

        AllureHelper.attachRequest("APP /endpoint " + action, apiKey, token, action);

        Response response = RestAssured
                .given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json")
                .header("X-Api-Key", apiKey)
                .formParam("token", token)
                .formParam("action", action)
                .when()
                .post(Endpoints.APP_ENDPOINT);

        AllureHelper.attachResponse("APP /endpoint " + action, response);
        return response;
    }
}
