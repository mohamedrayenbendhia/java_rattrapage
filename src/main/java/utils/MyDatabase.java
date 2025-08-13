package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    final String URl="jdbc:mysql://localhost:3306/java_ratrappage";
    final String USERNAME="root";
    final String PASSWORD="";
    Connection connection;

    static MyDatabase instance;

    private MyDatabase(){
        try {
            connection= DriverManager.getConnection(URl,USERNAME,PASSWORD);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }
    public  static MyDatabase getInstance(){
        if(instance==null)
            instance=new MyDatabase();
        return instance;

    }

    public Connection getConnection() {
        return connection;
    }
}
