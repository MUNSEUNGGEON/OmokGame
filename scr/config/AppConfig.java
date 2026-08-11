package config;

/** Runtime configuration loaded from environment variables. */
public final class AppConfig {
    private AppConfig() {}

    public static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is missing: " + name);
        }
        return value;
    }

    public static String optional(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
