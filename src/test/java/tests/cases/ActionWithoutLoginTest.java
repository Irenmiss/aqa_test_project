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
 * Диагностический тест для проверки поведения запроса /doAction в случае неуспешной аутентификации
 * Тест не падает при ошибочном запросе.
 * Предварительная аутентификация по условию теста не пройдена.
 * Diagnostic test to verify calling /doAction when auth failed
 */

@Epic("Internal service (SUT)")
@Feature("POST /endpoint")
@Story("ACTION rules")
@Owner("candidate")
@Severity(SeverityLevel.CRITICAL)
public class ActionWithoutLoginTest extends WireMockBaseTest {

    @Test
    @Tag("smoke")
    @Description("""
            ACTION without LOGIN should be rejected (4xx, usually 403).
            """ + """
            Попытка совершить действие после неуспешной аутентификации должна быть отклонена приложением (обычно это ошибка 4хх, часто 403)
            """
    )
    void testActionWithoutLogin() {
        String token = ApiClient.randomToken32(); //сгенерируем токен

        //выполняем тестируемый запрос
        //не логинимся перед этим
        AllureHelper.step("Пробуем совершить действие", "Try do action");
        var response = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "ACTION");

        AllureHelper.step("Ожидаем получение ошибки типа 4xx", "Wait that the response is like 4xx");
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 500,
                "Expected 4xx, got: " + response.statusCode());

        //убедимся, что приложение действительно не обработало запрос (реализация может отличаться)
        AllureHelper.step("Убедимся, что приложение НЕ выполнило запрос на /doAction", "Make sure that app NOT actually called /doAction");
        verify(0, postRequestedFor(urlEqualTo("/doAction")));
    }
}
