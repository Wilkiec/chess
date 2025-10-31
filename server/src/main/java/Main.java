import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager.createDatabase();
            System.out.println("Database is created");

            Server server = new Server();
            server.run(8080);

            System.out.println("♕ 240 Chess Server");
        } catch (DataAccessException e) {
            System.err.println("failed to make the database. Server cannot start");
        }
    }
}