public class TestUtils {

    private static final String HOST = System.getenv("TEST_SURREAL_HOST");
    private static final int PORT = Integer.parseInt(System.getenv("TEST_SURREAL_PORT"));
    private static final String USERNAME = System.getenv("TEST_SURREAL_USERNAME");
    private static final String PASSWORD = System.getenv("TEST_SURREAL_PASSWORD");

    public static String getHost(){
        return HOST;
    }

    public static int getPort(){
        return PORT;
    }

    public static String getUsername(){
        return USERNAME;
    }

    public static String getPassword(){
        return PASSWORD;
    }

}
