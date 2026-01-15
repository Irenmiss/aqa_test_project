package tests.cases;

import io.qameta.allure.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.api.ApiClient;
import tests.api.TestData;
import tests.base.WireMockBaseTest;
import tests.helpers.AllureHelper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.equalTo;

/**
 * Диагностический тест для проверки поведения при позитивном сценарии аутентификации
 * Тест не падает при ошибочном запросе.
 * Diagnostic test to verify behavior in positive auth scenario
 */


@Epic("Internal service (SUT)")
@Feature("POST /endpoint")
@Story("LOGIN")
@Owner("candidate")
@Severity(SeverityLevel.CRITICAL)
public class LoginSuccessTest extends WireMockBaseTest {

    @Test
    @Tag("smoke")
    @Description("""
            OObserved behavior: LOGIN should return OK (200)
            """ + """
            Поведение приложения при успешной аутентификации, когда статус ответа ОК (200).
            """
    )
    void testLoginWhenAuthOK() {
        String token = ApiClient.randomToken32(); //генерируем токен

        AllureHelper.step("Ожидаем, что сервис должен вернуть статус OK (200)", "Application must return status OK (200)");
        externalAuthOk();

        AllureHelper.step("Если пробуем залогиниться", "When try to login");
        var response = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "LOGIN"); //отправляем запрос на /auth

        AllureHelper.step("Ожидаем, что сервис вернет статус ответа ОК (200)", "Wait that application must return status OK (200)");
        response.then()
                .statusCode(200)
                .body("result", equalTo("OK"));

        AllureHelper.step("Проверим, что приложение действительно выполнило запрос /auth", "Verify that SUT actually called /auth");
        verify(1, postRequestedFor(urlEqualTo("/auth")));
        Allure.addAttachment("WireMock verify", "text/plain", "Verified: POST /auth called 1 time");
    }
}
