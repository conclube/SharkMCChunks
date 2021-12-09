package se.xfunserver.xplaychunks.utils;

public class PluginSettings {

    private static String CHUNK_WORLD = "xPlayMark";
    private static Integer CHUNK_PRICE = 5000;

    private static String DATABASE_HOSTNAME = "localhost";
    private static String DATABASE_NAME = "xfun_chunks";
    private static String DATABASE_USERNAME = "root";
    private static String DATABASE_PASSWORD = "?hqr(K>SD845";
    private static Integer DATABASE_PORT = 3306;
    private static Boolean USE_SSL = true;
    private static Boolean ALLOW_PUBLIC_KEY_RETRIEVAL = true;

    public static Integer getChunkPrice() {
        return CHUNK_PRICE;
    }

    public static String getChunkWorld() {
        return CHUNK_WORLD;
    }

    public static String getDatabaseHostname() {
        return DATABASE_HOSTNAME;
    }

    public static String getDatabaseName() {
        return DATABASE_NAME;
    }

    public static Boolean getAllowPublicKeyRetrieval() {
        return ALLOW_PUBLIC_KEY_RETRIEVAL;
    }

    public static Boolean getUseSsl() {
        return USE_SSL;
    }

    public static String getDatabasePassword() {
        return DATABASE_PASSWORD;
    }

    public static int getDatabasePort() {
        return DATABASE_PORT;
    }

    public static String getDatabaseUsername() {
        return DATABASE_USERNAME;
    }
}
