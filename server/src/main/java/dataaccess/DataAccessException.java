package dataaccess;

import java.sql.SQLException;

public class DataAccessException extends Throwable {
    public DataAccessException(String failedToCreateDatabase, SQLException ex) {
    }
}
