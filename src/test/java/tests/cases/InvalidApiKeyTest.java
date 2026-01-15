package tests.cases;

import io.qameta.allure.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.api.ApiClient;
import tests.api.TestData;
import tests.base.WireMockBaseTest;
import tests.helpers.AllureHelper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Диагностический тест для проверки поведения при использовании некорректного апи-ключа
 * Тест не падает при ошибочном запросе.
 * Diagnostic test to verify behavior when using invalid Api Key
 */

@Epic("Internal service (SUT)")
@Feature("POST /endpoint")
@Story("Security (X-Api-Key)")
@Owner("candidate")
@Severity(SeverityLevel.CRITICAL)
public class InvalidApiKeyTest extends WireMockBaseTest {

    @Test
    @Tag("smoke")
    @Description("""
            Invalid X-Api-Key should be rejected by the application, response status should be like 4xx.
            """ + """
            Сервис отклоняет попытки авторизорваться с неверным апи-ключом и возвращает ошибку типа 4xxx.
            """
    )
    void testLoginWithInvalidApiKey() {
        String token = ApiClient.randomToken32(); //генерируем токен

        AllureHelper.step("Если пытаемся залогиниться с неверным X-Api-Key", "If try login with invalid X-Api-Key");
        var response = ApiClient.callEndpoint(TestData.INVALID_API_KEY, token, "LOGIN");

        AllureHelper.step("Тогда получаем ошибку типа 4xx", "Then response is like 4xx");
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 500,
                "Expected 4xx, got: " + response.statusCode());

        //можно проверить, что приложение выполнило запрос
        //но по идее при неверном ключе приложение не должно ходить во внешний сервис
        AllureHelper.step("Проверка, что приложение действительно выполнило запрос /auth", "Verify that SUT actually called /auth");
        verify(0, postRequestedFor(urlEqualTo("/auth")));
    }
}
