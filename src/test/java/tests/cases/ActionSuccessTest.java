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
 * Диагностический тест для проверки поведения при позитивном сценарии взаимодействия с /doAction
 * Тест не падает при ошибочном запросе.
 * Предварительная аутентификация по условию теста проходит успешно.
 * Diagnostic test to verify behavior in positive scenario when interacting with /doAction
 */

@Epic("Internal service (SUT)")
@Feature("POST /endpoint")
@Story("ACTION")
@Owner("candidate")
@Severity(SeverityLevel.CRITICAL)
public class ActionSuccessTest extends WireMockBaseTest {

    @Test
    @Tag("smoke")
    @Description("""
            After successful authentication, the user's request to the application should be processed successfully (call to the external service must return status ОК (200).
            """ + """
            После аутентификации обращение пользователя к приложению должно быть успешным (запрос должен вернуть статус ОК (200)
            """
    )
    void testActionIfExternalRespondsOK() {
        String token = ApiClient.randomToken32(); //сгенерируем токен

        AllureHelper.step("Предполагаем, что аутентификация пройдет успешно (/auth = 200)", "Allure step: Initial condition - authentication successful (/auth = 200)");
        externalAuthOk(); //мок настроен на ответ 200

        AllureHelper.step("Предварительно залогинимся в системе", "First login to system");
        var login = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "LOGIN");
        login.then().statusCode(200).body("result", equalTo("OK")); //залогинимся и проверим, что статус 200
        verify(1, postRequestedFor(urlEqualTo("/auth"))); //проверим, что запрос действительно отправлен

        externalActionOk(); //заставляем мок вернуть 200 для проверки позитивного сценария

        AllureHelper.step("Пробуем совершить действие", "Try do action");
        var action = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "ACTION"); //выполняем тестируемый запрос

        AllureHelper.step("Ожидаем, что приложение вернет статус ответа ОК (200). В теле ответа Result равен ОК", "Wait that application will respond ОК (200). Result is ОК in the response body.");
        action.then().statusCode(200).body("result", equalTo("OK")); //проверяем, что в ответе будет ОК и статус ответа будет 200

        AllureHelper.step("Убедимся, что приложение действительно выполнило запрос на /doAction", "Make sure that SUT actually called /doAction");
        verify(1, postRequestedFor(urlEqualTo("/doAction")));
    }
}
