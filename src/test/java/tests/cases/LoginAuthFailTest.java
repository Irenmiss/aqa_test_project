package tests.cases;

import io.qameta.allure.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.api.ApiClient;
import tests.api.TestData;
import tests.base.WireMockBaseTest;
import tests.helpers.AllureHelper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Диагностический тест для проверки поведения при негативном сценарии аутентификации
 * Тест testLoginWhenAuthFails_observed не падает при ошибочном запросе
 * Тест testLoginWhenAuthFails_strict упадет, но отчет в allure будет создан
 * Diagnostic test to verify behavior in negative auth scenario
 */


@Epic("Internal service (SUT)")
@Feature("POST /endpoint")
@Story("LOGIN")
@Owner("candidate")
@Severity(SeverityLevel.NORMAL)
public class LoginAuthFailTest extends WireMockBaseTest {

    @Test
    @Tag("diagnostic")
    @Description("""
            Observed behavior: when external /auth returns 409, app may respond 409 or 500.
            """ + """
            Наблюдаемое поведение при ошибках авторизации (когда сервер возвращает ошибку 500 или 4хх).
            """
    )
    void testLoginWhenAuthFails_observed() {
        String token = ApiClient.randomToken32(); //генерируем токен

        externalAuthFail409(); //заставляем мок вернуть 409 (допустим, уже авторизован)

        AllureHelper.step("Если пробуем залогиниться ", "When try to login");
        var response = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "LOGIN"); //отправляем запрос на /auth

        AllureHelper.note(
                "Ожидаем, что сервис вернет ошибку типа 4хх или 500.",
                "When login, wait that application returns an error like 4xx or 500"
        );

        AllureHelper.step("Тогда в теле ответа параметр result будет равен ERROR", "Then result in the response body is ERROR");
        response.then()
                .statusCode(anyOf(is(409), is(500)))
                .body("result", equalTo("ERROR"));

        AllureHelper.step("Проверим, что приложение действительно выполнило запрос /auth", "Verify that SUT actually called /auth");
        verify(1, postRequestedFor(urlEqualTo("/auth")));
    }

@Epic("Internal service (SUT)")
@Feature("POST /endpoint")
@Story("LOGIN strict contract")
@Owner("candidate")
@Severity(SeverityLevel.CRITICAL)

    @Test
    @Tag("smoke")
    @Description("Strict contract: if /auth returns 409" +
    "Проверяем строгий сценарий аутентификации, когда статус ответа может быть только 409")
    void testLoginWhenAuthFails_strict() {
        String token = ApiClient.randomToken32(); //генерируем токен

        externalAuthFail409();

        AllureHelper.step("Если отправляем запрос с логином, апи ключом и токеном ", "When send a response that includes login, api-key and token");
        var response = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "LOGIN");

        AllureHelper.step(
                "Приложение должно вернуть при попытке залогиниться ошибку 409.",
                "Application must return 409"
        );
        response.then()
                .statusCode(409)
                .body("result", equalTo("ERROR"));

        AllureHelper.step("Проверка, что приложение действительно выполнило запрос /auth", "Verify that SUT actually called /auth");
        verify(1, postRequestedFor(urlEqualTo("/auth")));

        Allure.addAttachment("WireMock verify", "text/plain", "Verified: POST /auth called exactly 1 time");
    }
}
