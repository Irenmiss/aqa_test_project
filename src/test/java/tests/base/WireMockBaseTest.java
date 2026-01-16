package tests.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.*;
import tests.api.Endpoints;
import tests.helpers.AllureContextHolder;
import tests.helpers.AllureHelper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Базовый класс — поднимает WireMock и задаёт дефолтные стабы.
 * Base test class — starts WireMock and sets default stubs.
 *
 * Внешние вызовы приложения (SUT calls):
 *  - POST /auth     when action=LOGIN
 *  - POST /doAction when action=ACTION
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class WireMockBaseTest {

    protected WireMockServer wireMockServer; //поле для управления WireMock сервером

    @BeforeAll
    void startWireMock() {
        //запускаем WireMock перед каждым тестом
        wireMockServer = new WireMockServer(options().port(8888)); //создаем мок на порту 8888
        wireMockServer.start(); //запускаем
        configureFor("localhost", 8888); //подключаем к localhost:8888

        AllureHelper.note(
                "WireMock запущен на :8888 (внешняя зависимость замокана)",
                "WireMock started on :8888 (external dependency mocked)"
        );
    }

    @BeforeEach
    void resetMocksAndDefaults() {
        //перед каждым тестом сбрасываем настройки, удаляем историю вызовов, сбрасываем заглушки
        wireMockServer.resetAll();
        // настраиваем поведение по умолчанию (всегда OK)
        externalAuthOk();
        externalActionOk();
    }

    @AfterAll
    void stopWireMock() {
        //после кажого теста останавливаем WireMock
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @AfterEach
    void logFinalResponse(TestInfo testInfo) {
        if (AllureContextHolder.lastResponse == null) {
            return;
        }

        var response = AllureContextHolder.lastResponse;

        String result = response.jsonPath().getString("result");
        String message = response.jsonPath().getString("message");

        Allure.step(
                "Результаты теста: " +
                        "Код ответа: " + response.statusCode() +
                        ", Значение result: " + result +
                        ", Значение message: " + message
        );
    }

    //Вспомогательные методы для настройки заглушек

    //заглушка для успешной аутентификации
    protected void externalAuthOk() {
        stubFor(post(urlEqualTo(Endpoints.MOCK_AUTH))
                .atPriority(1)
                .willReturn(aResponse().withStatus(200))); //возвращаем статус ок (200)
    }

    //заглушка для неуспешной аутентификации
    protected void externalAuthFail409() {
        stubFor(post(urlEqualTo(Endpoints.MOCK_AUTH))
                .atPriority(1)
                .willReturn(aResponse().withStatus(409))); //возвращаем статус 409
    }

    //заглушка для успешного действия (200)
    protected void externalActionOk() {
        stubFor(post(urlEqualTo(Endpoints.MOCK_DO_ACTION))
                .atPriority(1)
                .willReturn(aResponse().withStatus(200)));
    }

    //заглушка для неспешного действия (409)
    protected void externalActionFail409() {
        stubFor(post(urlEqualTo(Endpoints.MOCK_DO_ACTION))
                .atPriority(1)
                .willReturn(aResponse().withStatus(409)));
    }
}