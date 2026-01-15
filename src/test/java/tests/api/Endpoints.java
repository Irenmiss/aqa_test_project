package tests.api;

/**
 * Эндпоинты тестируемого приложения и внешнего мок-сервиса.
 * Endpoints of application and mocked external dependency.
 */
public final class Endpoints {

    private Endpoints() {}

    // Endpoint / Эндпоинт
    public static final String APP_ENDPOINT = "/endpoint";

    // External mocked endpoints / Внешние эндпоинты (WireMock)
    public static final String MOCK_AUTH = "/auth";
    public static final String MOCK_DO_ACTION = "/doAction";
}
