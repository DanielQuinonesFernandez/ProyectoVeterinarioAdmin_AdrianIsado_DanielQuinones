package es.dam.sql;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class MySQLConnection {
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            // Crea la conexión
            String url = "jdbc:mysql://bmg7ajfj0oobgaitlsyn-mysql.services.clever-cloud.com:3306/bmg7ajfj0oobgaitlsyn?verifyServerCertificate=false";
            String usuario = "uvagmi97iaesxvss";
            String contrasenia = "OCKu7fO5sRtaNi6F5yA1";
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            connection = DriverManager.getConnection(url, usuario, contrasenia);

        } catch (ClassNotFoundException | SQLException e) {
            Log.d("Error", Objects.requireNonNull(e.getMessage()));
        }
        return connection;
    }
}

