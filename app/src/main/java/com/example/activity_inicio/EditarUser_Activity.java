package com.example.activity_inicio;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import es.dam.model.objetos.Mensaje;
import es.dam.sql.MySQLConnection;

public class EditarUser_Activity extends AppCompatActivity {

    private EditText etNombre, etApellido, etEmail, etTelefono, etContenidoMensaje, etContenidoMensajeVeto, etContrasenia, etAsuntoMensaje,
    etAsuntoMensajeVeto;
    private CheckBox cbVetado;
    private LinearLayout zonaVeto;
    private int idUsuario;
    private Spinner spinnerSelecTipoMensaje, spinnerSelecTipoMensajeVeto;
    private TextView btnUserCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_user);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellidos);
        etEmail = findViewById(R.id.etCorreoElectronico);
        etTelefono = findViewById(R.id.etTelefono);
        cbVetado = findViewById(R.id.checkBoxVetado);
        zonaVeto = findViewById(R.id.layoutVeto);
        etContenidoMensaje = findViewById(R.id.etContenidoMensaje);
        etContenidoMensajeVeto = findViewById(R.id.etContenidoMensajeVeto);
        etContrasenia = findViewById(R.id.etContrasenia);
        btnUserCita = findViewById(R.id.btnUserCita);
        etAsuntoMensaje = findViewById(R.id.etAsuntoMensaje);
        etAsuntoMensajeVeto = findViewById(R.id.etAsuntoMensajeVeto);

        idUsuario = getIntent().getIntExtra("idUsuario", -1);

        // Obtener datos del usuario y rellenar los campos
        obtenerDatosUsuario();

        // Listener para el checkbox de veto
        cbVetado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                zonaVeto.setVisibility(View.VISIBLE);
            } else {
                zonaVeto.setVisibility(View.GONE);
            }
        });

        // Crear el adaptador para el spinner de tipos de veto
        ArrayAdapter<CharSequence> adapterSelecTipoMensajeVeto = ArrayAdapter.createFromResource(this,
                R.array.tipos_de_veto, android.R.layout.simple_spinner_item);
        adapterSelecTipoMensajeVeto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar el adaptador al spinner de tipos de veto
        spinnerSelecTipoMensajeVeto = findViewById(R.id.combobox_seleccionTipoRazonVeto);
        spinnerSelecTipoMensajeVeto.setAdapter(adapterSelecTipoMensajeVeto);

        // Manejar el cambio de selección en el spinner de tipo de mensaje
        spinnerSelecTipoMensajeVeto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] tiposDeMensaje = getResources().getStringArray(R.array.tipos_de_veto);
                String tipoMensaje = tiposDeMensaje[position];
                String contenidoPredeterminado;
                String razDefecto = getString(R.string.razon_defecto);
                String razInasistencia = getString(R.string.razon_inasistencia);
                String razMascotas = getString(R.string.razon_mascotas);
                String razComportamiento = getString(R.string.razon_comportamiento);

                if (tipoMensaje.equals(razDefecto)) {
                    contenidoPredeterminado = getString(R.string.contenido_razon_defecto);
                } else if (tipoMensaje.equals(razInasistencia)) {
                    contenidoPredeterminado = getString(R.string.contenido_razon_inasistencia);
                } else if (tipoMensaje.equals(razMascotas)) {
                    contenidoPredeterminado = getString(R.string.contenido_razon_mascotas);
                } else if (tipoMensaje.equals(razComportamiento)) {
                    contenidoPredeterminado = getString(R.string.contenido_razon_comportamiento);
                } else {
                    contenidoPredeterminado = "Defecto";
                }
                etContenidoMensajeVeto.setText(contenidoPredeterminado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Crear el adaptador para el spinner de tipos de veto
        ArrayAdapter<CharSequence> adapterSelecTipoMensaje = ArrayAdapter.createFromResource(this,
                R.array.tipos_de_mensaje_user, android.R.layout.simple_spinner_item);
        adapterSelecTipoMensaje.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar el adaptador al spinner de tipos de veto
        spinnerSelecTipoMensaje = findViewById(R.id.combobox_cambioUser);
        spinnerSelecTipoMensaje.setAdapter(adapterSelecTipoMensaje);

        // Manejar el cambio de selección en el spinner de tipo de mensaje
        spinnerSelecTipoMensaje.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] tiposDeMensaje = getResources().getStringArray(R.array.tipos_de_mensaje_user);
                String tipoMensaje = tiposDeMensaje[position];
                String contenidoPredeterminado;
                String modDefecto = getString(R.string.mod_user_defecto);
                String modEmail = getString(R.string.mod_user_cambio_email);
                String modTelefono = getString(R.string.mod_user_cambio_telefono);
                String modContra = getString(R.string.mod_user_cambio_contra);

                if (tipoMensaje.equals(modDefecto)) {
                    contenidoPredeterminado = getString(R.string.contenido_modificacion_por_defecto_user);
                } else if (tipoMensaje.equals(modEmail)) {
                    contenidoPredeterminado = getString(R.string.contenido_email_user);
                } else if (tipoMensaje.equals(modTelefono)) {
                    contenidoPredeterminado = getString(R.string.contenido_telefono_user);
                } else if (tipoMensaje.equals(modContra)) {
                    contenidoPredeterminado = getString(R.string.contenido_contra_user);
                } else {
                    contenidoPredeterminado = "Defecto";
                }
                etContenidoMensaje.setText(contenidoPredeterminado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnUserCita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connection connection = MySQLConnection.getConnection();
                // Verificar si hay campos vacíos
                if (etNombre.getText().toString().isEmpty() ||
                        etApellido.getText().toString().isEmpty() ||
                        etEmail.getText().toString().isEmpty() ||
                        etTelefono.getText().toString().isEmpty() ||
                        etAsuntoMensaje.getText().toString().isEmpty() ||
                        etContenidoMensaje.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.campos_vacios), Toast.LENGTH_SHORT).show();
                } else {
                    // Todos los campos están completos, actualizar el registro en la base de datos
                    try {
                        if (connection == null) {
                            connection = MySQLConnection.getConnection();
                        }

                        if (!etContrasenia.getText().toString().isEmpty()) {
                            String sql = "UPDATE Usuarios SET NombreUsuario = ?, ApellidosUsuario = ?, CorreoElectronico = ?, NumTelefono = ?, Contrasenia = ?, estaVetado = ?, razonVeto = ? WHERE IdUsuario = ?";
                            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                                statement.setString(1, etNombre.getText().toString());
                                statement.setString(2, etApellido.getText().toString());
                                statement.setString(3, etEmail.getText().toString());
                                statement.setString(4, etTelefono.getText().toString());
                                String etContraseniaStringHash = hashearContrasena(etContrasenia.getText().toString());
                                statement.setString(5, etContraseniaStringHash);
                                statement.setBoolean(6, cbVetado.isChecked());
                                statement.setString(7, etContenidoMensajeVeto.getText().toString());
                                statement.setInt(8, idUsuario);
                                int rowsAffected = statement.executeUpdate();
                                if (rowsAffected > 0) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.datos_actualizados_correctamente), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_actualizar_datos), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            String sql = "UPDATE Usuarios SET NombreUsuario = ?, ApellidosUsuario = ?, CorreoElectronico = ?, NumTelefono = ?, estaVetado = ?, razonVeto = ? WHERE IdUsuario = ?";
                            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                                statement.setString(1, etNombre.getText().toString());
                                statement.setString(2, etApellido.getText().toString());
                                statement.setString(3, etEmail.getText().toString());
                                statement.setString(4, etTelefono.getText().toString());
                                statement.setBoolean(5, cbVetado.isChecked());
                                statement.setString(6, etContenidoMensajeVeto.getText().toString());
                                statement.setInt(7, idUsuario);
                                int rowsAffected = statement.executeUpdate();
                                if (rowsAffected > 0) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.datos_actualizados_correctamente), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_actualizar_datos), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        if (!etAsuntoMensaje.getText().toString().isEmpty() ||
                                !etContenidoMensaje.getText().toString().isEmpty()) {
                            // Crear y enviar objeto Mensaje
                            Mensaje mensaje = new Mensaje(idUsuario, etAsuntoMensaje.getText().toString(), etContenidoMensaje.getText().toString(),
                                    spinnerSelecTipoMensaje.getSelectedItem().toString());
                            enviarMensajeABaseDeDatos(mensaje);
                        }

                        if (cbVetado.isChecked()) {
                            if (etAsuntoMensajeVeto.getText().toString().isEmpty() ||
                                    etContenidoMensajeVeto.getText().toString().isEmpty()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.complete_campos_veto), Toast.LENGTH_SHORT).show();
                            } else {
                                Mensaje mensajeVeto = new Mensaje(idUsuario, etAsuntoMensajeVeto.getText().toString(), etContenidoMensajeVeto.getText().toString(),
                                        spinnerSelecTipoMensaje.getSelectedItem().toString());
                                enviarMensajeABaseDeDatos(mensajeVeto);
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(EditarUser_Activity.this);
                        builder.setMessage(getString(R.string.usuario_actualizado_correctamente) +
                                getString(R.string.mensaje_usuario_actualizado_usuario) + etNombre.getText().toString() + " " + etApellido.getText().toString() + "\n" +
                                getString(R.string.mensaje_usuario_actualizado_email) + etEmail.getText().toString() + "\n" +
                                getString(R.string.mensaje_usuario_actualizado_telefono) + etTelefono.getText().toString() + "\n");
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(EditarUser_Activity.this, GestionarUsuarios_Activity.class);
                            intent.putExtra("idUsuario", idUsuario);
                            startActivity(intent);
                            finish();
                        });
                        connection.close();
                        builder.create().show();

                    } catch (SQLException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    private void obtenerDatosUsuario() {
        try {
            Connection connection = MySQLConnection.getConnection();
            String sql = "SELECT * FROM Usuarios WHERE IdUsuario = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, idUsuario);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String nombre = resultSet.getString("NombreUsuario");
                String apellido = resultSet.getString("ApellidosUsuario");
                String email = resultSet.getString("CorreoElectronico");
                String telefono = resultSet.getString("NumTelefono");
                boolean estaVetado = resultSet.getBoolean("EstaVetado");

                etNombre.setText(nombre);
                etApellido.setText(apellido);
                etEmail.setText(email);
                etTelefono.setText(telefono);
                cbVetado.setChecked(estaVetado);

                // Mostrar u ocultar la zona de veto según el estado de estaVetado
                zonaVeto.setVisibility(estaVetado ? View.VISIBLE : View.GONE);
            }

            resultSet.close();
            connection.close();
        } catch (SQLException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void enviarMensajeABaseDeDatos(Mensaje mensaje) {
        Connection connection = null;
        try {
            connection = MySQLConnection.getConnection();

            if (connection == null) {
                connection = MySQLConnection.getConnection();
            }

            String sql = "INSERT INTO Mensajes (AsuntoMensaje, TipoMensaje, ContenidoMensaje, FechaHoraMensaje, IdUsuario) VALUES (?, ?, ?, ?, ?)";

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            java.sql.Timestamp timestamp = new java.sql.Timestamp(calendar.getTimeInMillis());


            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, mensaje.getAsunto());
            statement.setString(2, mensaje.getTipoMensaje());
            statement.setString(3, mensaje.getContenido());
            statement.setTimestamp(4, timestamp);
            statement.setInt(5, mensaje.getIdUsuario());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al enviar mensaje a la base de datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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
