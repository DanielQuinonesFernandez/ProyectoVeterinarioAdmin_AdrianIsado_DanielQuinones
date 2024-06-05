package com.example.activity_inicio;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import es.dam.model.objetos.*;
import es.dam.sql.MySQLConnection;

public class EditarCita_Activity extends AppCompatActivity {

    private final ArrayList<Mascota> mascotasDeEsteUsuario = new ArrayList<>();
    private EditText etDescripcionCita, edOtroTipoCita, etFechaCita;
    private Spinner spinnerSelecMascota, spinnerSelecTipoCita;
    private TimePicker timePicker;
    private TextView tvFechaHoraSeleccionada;
    private int idUsuario, idCita;
    private Spinner spinnerSelecTipoMensaje;
    private EditText etContenidoMensaje, etAsuntoMensaje;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_cita);

        idUsuario = getIntent().getIntExtra("idUsuario", -1);
        idCita = getIntent().getIntExtra("idCita", -1);

        // Inicializar vistas
        tvFechaHoraSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        ImageButton btnFechaCita = findViewById(R.id.btnFechaCita);

        timePicker = findViewById(R.id.timePickerPedirCita);

        edOtroTipoCita = findViewById(R.id.edOtroTipoCita);
        etDescripcionCita = findViewById(R.id.etOtrosDatosRelevantes);
        etFechaCita = findViewById(R.id.etFechaCita);

        spinnerSelecTipoMensaje = findViewById(R.id.combobox_seleccionTipoMensaje);
        etContenidoMensaje = findViewById(R.id.etContenidoMensaje);
        etAsuntoMensaje = findViewById(R.id.etAsuntoMensaje);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDateTime now = LocalDateTime.now();
            etFechaCita.setText(dtf.format(now));
        } else {
            etFechaCita.setText(R.string.fecha_por_defecto);
        }

        rellenarArraylistDesdeBBDD();
        obtenerDescripcionCita();

        spinnerSelecMascota = findViewById(R.id.combobox_seleccionMascota);
        ArrayAdapter<Mascota> adapterSelecMascota = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mascotasDeEsteUsuario);
        adapterSelecMascota.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelecMascota.setAdapter(adapterSelecMascota);

        seleccionarMascotaCitaSeleccionada();

        spinnerSelecTipoCita = findViewById(R.id.combobox_seleccionTipoCita);
        ArrayAdapter<CharSequence> adapterSelecTipoCita = ArrayAdapter.createFromResource(this,
                R.array.tipos_de_cita, android.R.layout.simple_spinner_item);
        adapterSelecTipoCita.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelecTipoCita.setAdapter(adapterSelecTipoCita);

        seleccionarTipoCita(adapterSelecTipoCita);

        ArrayAdapter<CharSequence> adapterSelecTipoMensaje = ArrayAdapter.createFromResource(this,
                R.array.tipos_de_mensaje, android.R.layout.simple_spinner_item);
        adapterSelecTipoMensaje.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelecTipoMensaje.setAdapter(adapterSelecTipoMensaje);

        btnFechaCita.setOnClickListener(v -> mostrarCalendario());

        TextView btnPedirCita = findViewById(R.id.btnEditarCita);
        btnPedirCita.setOnClickListener(v -> editarCita());

        etFechaCita.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int hora = timePicker.getHour();
                @SuppressLint("DefaultLocale") String minutoStr = String.format("%02d", timePicker.getMinute());
                tvFechaHoraSeleccionada.setText("Fecha seleccionada: " + etFechaCita.getText().toString() + " " + hora + ":" + minutoStr);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            int hora = timePicker.getHour();
            @SuppressLint("DefaultLocale") String minutoStr = String.format("%02d", timePicker.getMinute());
            tvFechaHoraSeleccionada.setText("Fecha seleccionada: " + etFechaCita.getText().toString() + " " + hora + ":" + minutoStr);
        });

        // Manejar el cambio de selección en el spinner de tipo de mensaje
        spinnerSelecTipoMensaje.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] tiposDeMensaje = getResources().getStringArray(R.array.tipos_de_mensaje);
                String tipoMensaje = tiposDeMensaje[position];
                String contenidoPredeterminado;
                String modDefecto = getString(R.string.mod_defecto);
                String modCambioHora = getString(R.string.mod_cambio_hora);
                String modCambioTipoCita = getString(R.string.mod_cambio_tipo_cita);
                String modCambioDescCita = getString(R.string.mod_cambio_desc);

                if (tipoMensaje.equals(modDefecto)) {
                    contenidoPredeterminado = getString(R.string.contenido_modificacion_por_defecto);
                } else if (tipoMensaje.equals(modCambioHora)) {
                    contenidoPredeterminado = getString(R.string.contenido_cambio_de_hora);
                } else if (tipoMensaje.equals(modCambioTipoCita)) {
                    contenidoPredeterminado = getString(R.string.contenido_cambio_de_tipo_de_cita);
                } else if (tipoMensaje.equals(modCambioDescCita)) {
                    contenidoPredeterminado = getString(R.string.contenido_cambio_de_descripcion_de_la_cita);
                } else {
                    contenidoPredeterminado = "";
                }
                etContenidoMensaje.setText(contenidoPredeterminado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerSelecTipoCita.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                edOtroTipoCita.setText("");
                if (spinnerSelecTipoCita.getSelectedItem().toString().equals("Otros - Por favor, consulte con el veterinario")) {
                    edOtroTipoCita.setVisibility(View.VISIBLE);
                    edOtroTipoCita.requestFocus();
                } else {
                    edOtroTipoCita.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void obtenerDescripcionCita() {
        try {
            Connection connection = MySQLConnection.getConnection();
            String sql = "SELECT DescripcionCita FROM Citas WHERE IdCita = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, idCita);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String descripcionCita = resultSet.getString("DescripcionCita");
                etDescripcionCita.setText(descripcionCita);
            }

            resultSet.close();
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    private void seleccionarMascotaCitaSeleccionada() {
        try {
            Connection connection = MySQLConnection.getConnection();

            String sql = "SELECT IdMascota FROM Citas WHERE IdCita = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, idCita);

            ResultSet resultSet = statement.executeQuery();

            int idMascotaCitaSeleccionada = -1;

            if (resultSet.next()) {
                idMascotaCitaSeleccionada = resultSet.getInt("IdMascota");
            }

            resultSet.close();
            connection.close();

            // Una vez obtenido el id de la mascota asociada a la cita, seleccionamos la mascota en el Spinner
            if (idMascotaCitaSeleccionada != -1) {
                for (int i = 0; i < mascotasDeEsteUsuario.size(); i++) {
                    if (mascotasDeEsteUsuario.get(i).getId() == idMascotaCitaSeleccionada) {
                        spinnerSelecMascota.setSelection(i);
                        break;
                    }
                }
            }

        } catch (SQLException ignored) {
        }
    }

    private void seleccionarTipoCita(ArrayAdapter<CharSequence> adapterSelecTipoCita) {
        try {
            Connection connection = MySQLConnection.getConnection();

            String sql = "SELECT TipoCita FROM Citas WHERE IdCita = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, idCita);

            ResultSet resultSet = statement.executeQuery();

            String tipoCitaSeleccionada = null;

            if (resultSet.next()) {
                tipoCitaSeleccionada = resultSet.getString("TipoCita");
            }

            resultSet.close();
            connection.close();

            // Una vez obtenido el tipo de cita asociado a la cita, seleccionamos el tipo de cita en el Spinner
            if (tipoCitaSeleccionada != null) {
                for (int i = 0; i < adapterSelecTipoCita.getCount(); i++) {
                    if (adapterSelecTipoCita.getItem(i).equals(tipoCitaSeleccionada)) {
                        spinnerSelecTipoCita.setSelection(i);
                        break;
                    }
                }
            }

        } catch (SQLException ignored) {
        }
    }


    private void rellenarArraylistDesdeBBDD() {
        try {
            Connection connection = MySQLConnection.getConnection();

            String sql = "SELECT * FROM Mascotas WHERE idUsuario = ?";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, idUsuario);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int idMascota = resultSet.getInt("IdMascota");
                String nombreMascota = resultSet.getString("NombreMascota");
                String especie = resultSet.getString("Especie");
                Date fechaNacimiento = resultSet.getDate("FechaNacimiento");
                byte[] imagenBytes = resultSet.getBytes("Imagen");
                Bitmap imagenMascota = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                int idUsuario = resultSet.getInt("IdUsuario");

                mascotasDeEsteUsuario.add(new Mascota(idMascota, nombreMascota, especie, fechaNacimiento, imagenMascota, idUsuario));
            }

            resultSet.close();
            connection.close();

        } catch (SQLException ignored) {
        }
    }

    private void editarCita() {
        try {
            // Validar que los campos no estén vacíos
            if (spinnerSelecMascota.getSelectedItem() == null ||
                    spinnerSelecTipoCita.getSelectedItem() == null ||
                    etFechaCita.getText().toString().isEmpty() ||
                    etDescripcionCita.getText().toString().isEmpty() ||
                    etAsuntoMensaje.getText().toString().isEmpty() ||
                    spinnerSelecTipoMensaje.getSelectedItem() == null ||
                    etContenidoMensaje.getText().toString().isEmpty()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Por favor, complete todos los campos.");
                builder.setPositiveButton("OK", null);
                builder.create().show();
                return;
            }

            int hora = timePicker.getHour();
            @SuppressLint("DefaultLocale") String minutoStr = String.format("%02d", timePicker.getMinute());

            Connection connection = MySQLConnection.getConnection();

            String sql = "UPDATE Citas SET TipoCita = ?, FechaHora = ?, DescripcionCita = ?, IdMascota = ? WHERE IdCita = ?";
            int idMascotaElegida = obtenerIdMascota(spinnerSelecMascota.getSelectedItem().toString());

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, spinnerSelecTipoCita.getSelectedItem().toString());
            statement.setString(2, etFechaCita.getText().toString() + " " + hora + ":" + minutoStr);
            statement.setString(3, etDescripcionCita.getText().toString());
            statement.setInt(4, idMascotaElegida);
            statement.setInt(5, idCita);
            statement.executeUpdate();

            // Crear y enviar objeto Mensaje
            Mensaje mensaje = new Mensaje(idUsuario, etAsuntoMensaje.getText().toString(), etContenidoMensaje.getText().toString(),
            spinnerSelecTipoMensaje.getSelectedItem().toString());
            enviarMensajeABaseDeDatos(mensaje);

            connection.close();

            AlertDialog.Builder builder = new AlertDialog.Builder(EditarCita_Activity.this);
            builder.setMessage("Has actualizado correctamente la cita:\n" +
                    "- Fecha: " + etFechaCita.getText().toString() + " " + hora + ":" + minutoStr + "\n" +
                    "- Tipo de cita: " + spinnerSelecTipoCita.getSelectedItem().toString() + "\n" +
                    "- Descripción: " + etDescripcionCita.getText().toString() + "\n" +
                    "- Mascota: " + spinnerSelecMascota.getSelectedItem().toString());
            builder.setPositiveButton("OK", (dialog, which) -> {
                Intent intent = new Intent(EditarCita_Activity.this, GestionarCitas_Activity.class);
                intent.putExtra("idUsuario", idUsuario);
                startActivity(intent);
                finish();
            });
            connection.close();
            builder.create().show();

        } catch (SQLException ignored) {
        }
    }

    private void enviarMensajeABaseDeDatos(Mensaje mensaje) {
        Connection connection = null;
        try {
            connection = MySQLConnection.getConnection();
            String sql = "INSERT INTO Mensajes (AsuntoMensaje, TipoMensaje, ContenidoMensaje, FechaHoraMensaje, IdUsuario) VALUES (?, ?, ?, ?, ?)";

            // Crear un objeto java.sql.Timestamp para la fecha actual
            java.util.Date currentDate = new java.util.Date();
            java.sql.Timestamp timestamp = new java.sql.Timestamp(currentDate.getTime());

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, mensaje.getAsunto());
            statement.setString(2, mensaje.getTipoMensaje());
            statement.setString(3, mensaje.getContenido());
            statement.setString(4, mensaje.getFecha()); // Establecer la fecha en la base de datos
            statement.setInt(5, mensaje.getIdUsuario()); // Establecer el ID de usuario en la base de datos
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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



    private int obtenerIdMascota(String mascotaElegida) {
        for (Mascota m : mascotasDeEsteUsuario) {
            if ((m.getNombre() + " [" + m.getEspecie() + "]").equals(mascotaElegida)) {
                return m.getId();
            }
        }
        return -1; // Valor predeterminado si no se encuentra la mascota
    }

    private void mostrarCalendario() {
        final Calendar calendario = Calendar.getInstance();
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    @SuppressLint("DefaultLocale") String fechaSeleccionada = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year);
                    etFechaCita.setText(fechaSeleccionada);
                }, anio, mes, dia);

        datePickerDialog.show();
    }

    private void limpiarCampos() {
        etDescripcionCita.setText("");
        edOtroTipoCita.setText("");
        etFechaCita.setText("");
    }
}
