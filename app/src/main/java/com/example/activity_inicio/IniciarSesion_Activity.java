package com.example.activity_inicio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.dam.sql.MySQLConnection;

public class IniciarSesion_Activity extends AppCompatActivity {

    private Context context;
    private EditText etCorreo, etContrasenia;
    private int intentosRestantes;
    private SharedPreferences sharedPreferences;
    private TextView btnIniciarSesion, btnLimpiarCampos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iniciar_sesion);

        context = this;
        sharedPreferences = getSharedPreferences("contador", Context.MODE_PRIVATE);

        intentosRestantes = sharedPreferences.getInt("intentos_fallidos", 0); // Establecer intentos máximos
        etCorreo = findViewById(R.id.etCorreoElectronico);
        etContrasenia = findViewById(R.id.etContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnLimpiarCampos = findViewById(R.id.btnLimpiarCampos);

        // Verificar si la aplicación está bloqueada
        if (intentosRestantes >= 2 || sharedPreferences.getBoolean("bloqueado", false)) {
            // La aplicación está bloqueada, redirigir a la pantalla de bloqueo
            Intent intent = new Intent(context, PantallaBloqueo.class);
            startActivity(intent);
            finish(); // Cerrar la actividad actual
        }

        btnIniciarSesion.setOnClickListener(v -> {
            if (!hayCamposVacios()) {
                iniciarSesion();
            } else {
                Toast.makeText(context, getString(R.string.campos_vacios), Toast.LENGTH_SHORT).show();
            }
        });

        btnLimpiarCampos.setOnClickListener(v -> {
            limpiarCampos();
        });
    }

    private void iniciarSesion() {
        // Obtener el correo electrónico y contraseña ingresados por el usuario
        String correoUsuario = etCorreo.getText().toString();
        String contrasenia = etContrasenia.getText().toString();
        String contraseniaHasheada = hashearContrasena(contrasenia);
        try {
            Connection connection = MySQLConnection.getConnection();
            String sql = "SELECT * FROM Usuarios WHERE CorreoElectronico = ? AND Contrasenia = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, correoUsuario);
            statement.setString(2, contraseniaHasheada);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // El usuario y la contraseña son válidos
                // Redirigir al usuario a la pantalla principal
                Intent intent = new Intent(context, MenuPrincipal_Activity.class);
                startActivity(intent);
                finish(); // Cerrar la actividad actual
                Toast.makeText(context, getString(R.string.sesion_iniciada), Toast.LENGTH_SHORT).show();
            } else {
                // El usuario y/o la contraseña son incorrectos
                // Mostrar un Toast con los intentos restantes
                intentosRestantes--;
                if (intentosRestantes <= -1) {
                    intentosRestantes = 1;
                }
                Toast.makeText(context, getString(R.string.usuario_contraseña_incorrectos) + intentosRestantes, Toast.LENGTH_SHORT).show();

                // Guardar el número de intentos fallidos en SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("intentos_fallidos", intentosRestantes);
                editor.apply();

                if (intentosRestantes == 0) {
                    // Bloquear el inicio de sesión después de 3 intentos fallidos
                    etCorreo.setEnabled(false);
                    etContrasenia.setEnabled(false);
                    btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
                    btnIniciarSesion.setEnabled(false);
                    Intent intent = new Intent(context, PantallaBloqueo.class);
                    startActivity(intent);
                    finish(); // Cerrar la actividad actual
                }
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(context, getString(R.string.error_iniciar_sesion) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void limpiarCampos() {
        etCorreo.setText("");
        etContrasenia.setText("");
    }

    private boolean hayCamposVacios() {
        return etCorreo.getText().toString().isEmpty() || etContrasenia.getText().toString().isEmpty();
    }

    public static String hashearContrasena(String contrasena) {
        try {
            // Crear un objeto MessageDigest con el algoritmo SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Calcular el hash de la contraseña
            byte[] hashedBytes = md.digest(contrasena.getBytes());

            // Convertir el arreglo de bytes a una representación hexadecimal
            BigInteger bigInt = new BigInteger(1, hashedBytes);
            StringBuilder hashedPassword = new StringBuilder(bigInt.toString(16));

            // Asegurarse de que el hash tenga 64 caracteres
            while (hashedPassword.length() < 64) {
                hashedPassword.insert(0, "0");
            }

            return hashedPassword.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error: Algoritmo SHA-256 no disponible");
            return null;
        }
    }

}
