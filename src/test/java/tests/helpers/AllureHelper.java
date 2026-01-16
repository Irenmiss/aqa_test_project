package tests.helpers;

import io.qameta.allure.Allure;
import io.restassured.response.Response;

/**
 * Утилиты для Allure (шаги, приложения, заметки)
 * Allure utilities (steps, attachments, notes)
 */
public final class AllureHelper {

    private AllureHelper() {}


    //для удобства для разных пользователей сделаем отчеты двуязычными
    public static void step(String ru, String en) {
        Allure.step("RU: " + ru);
        Allure.step("EN: " + en);
        Allure.step("____________________________________________________________________");
    }

    //метод для сохранения в отчете allure вложения с деталями запроса
    public static void attachRequest(String title, String apiKey, String token, String action) {
        String request = """
                X-Api-Key: %s
                Content-Type: application/x-www-form-urlencoded
                token=%s
                action=%s
                """.formatted(apiKey, token, action);

        Allure.addAttachment(title + " | Request", "text/plain", request);
    }

    //метод для сохранения отчетов о запросе (ответ + тело ответа)
    public static void attachResponse(String title, Response response) {
        Allure.addAttachment(title + " | HTTP status", String.valueOf(response.statusCode()));
        Allure.addAttachment(title + " | Response body", "application/json", response.getBody().asString());
    }

    //метод добаления заметок в отчеты
    public static void note(String ru, String en) {
        Allure.addAttachment("NOTE", "text/plain", ru + "\n" + en);
    }
}