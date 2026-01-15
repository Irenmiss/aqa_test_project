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
 * Диагностический тест для проверки поведения при негативном сценарии взаимодействия с /doAction
 * Тест не падает при ошибочном запросе.
 * Предварительная аутентификация по условию теста проходит успешно.
 * Diagnostic test to verify behavior in negative scenario when interacting with /doAction
 */

@Epic("Internal service of SUT")
@Feature("POST /endpoint")
@Story("ACTION")
@Owner("candidate")
@Severity(SeverityLevel.NORMAL)
public class ActionFailTest extends WireMockBaseTest {

    @Test
    @Tag("diagnostic")
    @Description("""
            Observed behavior: when external service returns 409 or 500.
            """ + """
            Наблюдаем поведение сервиса в случае ошибки 409 или 500
            """
    )
    void testActionIfExternalFails() {
        String token = ApiClient.randomToken32(); //сгенерируем токен

        AllureHelper.step("Предполагаем, что аутентификация пройдет успешно (/auth = 200)",
                "Allure step: Initial condition - authentication successful (/auth = 200)");
        externalAuthOk(); //мок настроен на ответ 200

        AllureHelper.step("Предварительно залогинимся в системе", "First login to system");
        var login = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "LOGIN"); //вызовем эндпоинт (с ключом и логином)
        login.then().statusCode(200).body("result", equalTo("OK")); //залогинимся и проверим, что статус 200
        verify(1, postRequestedFor(urlEqualTo("/auth"))); //проверим, что мок принял /auth

        externalActionFail409();//заставляем мок вернуть 409 для проверки негативного кейса

        AllureHelper.step("Пробуем совершить действие", "Try do action");
        var action = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "ACTION"); //выполняем тестируемый запрос

        AllureHelper.note(
                "Ожидаем, что приложение вернет ошибку 409 или 500. В теле ответа Result будет равен ERROR.",
                "See if application will respond 409 or 500. Result id ERROR in the response body."
        );
        action.then()
                .statusCode(anyOf(is(409), is(500))) //ожидаем ошибку 409 или 500
                .body("result", equalTo("ERROR")); //проверяем, есть ли в ответе ERROR

        AllureHelper.step("Убедимся, что приложение действительно выполнило запрос на /doAction", "Make sure that SUT actually called /doAction");
        verify(1, postRequestedFor(urlEqualTo("/doAction")));//проверим, что мок получил запрос
    }
}
