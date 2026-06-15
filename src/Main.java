import java.io.IOException;
import server.CampusServer;

public class Main {
public static void main(String[] args) {
    try {
        CampusServer server = new CampusServer();
        server.start();
        System.out.println("Campus Connect Platform is running!");
        System.out.println("Access the application at: http://localhost:8080");
    } catch (IOException e) {
        System.err.println("Failed to start server: " + e.getMessage());
        e.printStackTrace();
    }
}

}
