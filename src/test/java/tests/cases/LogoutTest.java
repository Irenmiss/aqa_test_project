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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Строгая проверка LOGOUT по ТЗ:
 * 1) LOGIN сохраняет токен во внутреннем хранилище приложения
 * 2) LOGOUT удаляет токен из внутреннего хранилища
 * 3) После LOGOUT действие ACTION должно быть недоступно (ожидаем 4xx)
 *
 * Strict LOGOUT contract test:
 * 1) LOGIN stores token in application's internal storage
 * 2) LOGOUT removes token from internal storage
 * 3) After LOGOUT, ACTION must be rejected (expect 4xx)
 */
@Epic("Internal service (SUT)")
@Feature("POST /endpoint")
@Story("LOGOUT removes token session")
@Owner("candidate")
@Severity(SeverityLevel.CRITICAL)
public class LogoutTest extends WireMockBaseTest {

    @Test
    @Tag("smoke")
    @Description("""
            Strict contract:
            - LOGIN returns 200 OK and result is OK
            - LOGOUT returns 200 OK and result is OK and removes the token from internal storage
            - ACTION after LOGOUT must be rejected (4xx)
            """ +
            """
            Строгое поведение:
            - Попытка залогиниться возвращает статус 200 (OK), result равен OK
            - Завершение сессии возвращает статус 200 (OK), result равен OK, токен удаляется из внутреннего хранилища
            - Попытка выполнить действие после разлогинивания должна быть отклонена с ошибкой 4xx
            """
    )
    void testLogoutRemoveTokenAndBlockAction() {
        String token = ApiClient.randomToken32();// генерируем токен как ключ пользовательской "сессии" в приложении

        AllureHelper.step(
                "Ожидаем, что авторизация проходит успешно и сервис возвращает ответ 200",
                "We wait that auth is successful and service returns 200"
        );
        externalAuthOk();

        AllureHelper.step(
                "После авторизации токен сохраняется во внутреннем хранилище приложения.",
                "After auth token is stored in app internal storage)"
        );
        var login = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "LOGIN");

        login.then()
                .statusCode(200)
                .body("result", equalTo("OK"));

        // проверяем, что приложение действительно ходило во внешний сервис /auth
        AllureHelper.step("Проверим, что приложение действительно выполнило запрос", "Verify that SUT actually called");
        verify(1, postRequestedFor(urlEqualTo("/auth")));

        AllureHelper.step(
                "Если сессия юзера завершается, то токен удаляется из внутреннего хранилища.", "When user's session is complete, token is removed from internal storage.");
        var logout = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "LOGOUT");

        AllureHelper.step(
                "Ответ сервиса при этом также будет ОК (200)'", "The response status is also OK (200)");
        logout.then()
                .statusCode(200)
                .body("result", equalTo("OK"));

        AllureHelper.step(
                "Если после завершения сессии пробуем соверщить действия с сервисом.", "When perform ACTION after LOGOUT");
        var actionAfterLogout = ApiClient.callEndpoint(TestData.VALID_API_KEY, token, "ACTION");

        AllureHelper.step(
                "То действие должно быть запрещено (4xx), т.к. токен удален",
                "Then action must be rejected (4xx), because token was removed"
        );

        // потому что реализация может отличаться (403/401/400), не привязываемся к коду
        assertTrue(actionAfterLogout.statusCode() >= 400 && actionAfterLogout.statusCode() < 500,
                "Expected 4xx after LOGOUT, got: " + actionAfterLogout.statusCode());

        //после logout приложение не должно ходить во внешний /doAction,
        // потому что оно должно отклонить запрос раньше (нет сессии/токена).
        AllureHelper.step(
                "Убедимся, что приложение НЕ вызывало /doAction после LOGOUT.", "Make sure SUT did NOT call /doAction after LOGOUT");
        verify(0, postRequestedFor(urlEqualTo("/doAction")));

        Allure.addAttachment(
                "Result summary",
                "text/plain",
                "LOGIN: 200 OK -> token stored\nLOGOUT: 200 OK -> token removed\nACTION after LOGOUT: 4xx -> rejected"
        );
    }
}
