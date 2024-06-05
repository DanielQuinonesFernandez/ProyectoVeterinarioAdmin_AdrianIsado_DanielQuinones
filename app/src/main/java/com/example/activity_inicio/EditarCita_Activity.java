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
import android.widget.Toast;

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
    private ArrayList<String> mascotasDeEsteUsuario = new ArrayList<>();
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
        ArrayAdapter<String> adapterSelecMascota = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mascotasDeEsteUsuario);
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
                tvFechaHoraSeleccionada.setText(getString(R.string.fecha_seleccionada) + etFechaCita.getText().toString() + " " + hora + ":" + minutoStr);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            int hora = timePicker.getHour();
            @SuppressLint("DefaultLocale") String minutoStr = String.format("%02d", timePicker.getMinute());
            tvFechaHoraSeleccionada.setText(getString(R.string.fecha_seleccionada) + etFechaCita.getText().toString() + " " + hora + ":" + minutoStr);
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
                if (spinnerSelecTipoCita.getSelectedItem().toString().equals(getString(R.string.otros_consulte_veterinario))) {
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
        } catch (SQLException ex) {
            ex.printStackTrace();
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

            if (idMascotaCitaSeleccionada != -1) {
                for (int i = 0; i < mascotasDeEsteUsuario.size(); i++) {
                    String mascota = mascotasDeEsteUsuario.get(i);
                    int idMascota = Integer.parseInt(mascota.split("-")[0]); // Obtener el ID de la mascota del String
                    if (idMascota == idMascotaCitaSeleccionada) {
                        spinnerSelecMascota.setSelection(i);
                        break;
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
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
            String sql = "SELECT IdMascota, NombreMascota FROM Mascotas WHERE idUsuario = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, idUsuario);
            ResultSet resultSet = statement.executeQuery();

            mascotasDeEsteUsuario.clear();

            while (resultSet.next()) {
                int idMascota = resultSet.getInt("IdMascota");
                String nombreMascota = resultSet.getString("NombreMascota");
                String idNombreConcatenado = idMascota + "-" + nombreMascota;
                mascotasDeEsteUsuario.add(idNombreConcatenado);
            }
            resultSet.close();
            connection.close();

        } catch (SQLException ignored) {
        }
    }

    private int obtenerIdMascota(String mascotaElegida) {
        // Obtener la ID de la mascota a partir del texto seleccionado en el Spinner
        if (mascotaElegida != null && mascotaElegida.contains("-")) {
            String idMascotaStr = mascotaElegida.split("-")[0];
            return Integer.parseInt(idMascotaStr);
        }
        return -1; // Valor predeterminado si no se puede obtener la ID de la mascota
    }

    private void editarCita() {
        Connection connection = null;
        try {
            connection = MySQLConnection.getConnection();
            // Validar que los campos no estén vacíos
            if (spinnerSelecMascota.getSelectedItem() == null ||
                    spinnerSelecTipoCita.getSelectedItem() == null ||
                    etFechaCita.getText().toString().isEmpty() ||
                    etDescripcionCita.getText().toString().isEmpty() ||
                    etAsuntoMensaje.getText().toString().isEmpty() ||
                    spinnerSelecTipoMensaje.getSelectedItem() == null ||
                    etContenidoMensaje.getText().toString().isEmpty()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.campos_vacios));
                builder.setPositiveButton("OK", null);
                builder.create().show();
                return;
            }

            int hora = timePicker.getHour();
            @SuppressLint("DefaultLocale") String minutoStr = String.format("%02d", timePicker.getMinute());

            if (connection == null) {
                connection = MySQLConnection.getConnection();
            }

            String sql = "UPDATE Citas SET TipoCita = ?, FechaHora = ?, DescripcionCita = ?, IdMascota = ? WHERE IdCita = ?";

            String mascotaSeleccionada = spinnerSelecMascota.getSelectedItem().toString();
            int idMascotaElegida = obtenerIdMascota(mascotaSeleccionada);

            // Verificar si el ID de la mascota elegida es válido
            if (idMascotaElegida == -1) {
                Toast.makeText(EditarCita_Activity.this, getString(R.string.error_no_mascota_seleccionada), Toast.LENGTH_SHORT).show();
                return;
            }
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

            AlertDialog.Builder builder = new AlertDialog.Builder(EditarCita_Activity.this);
            builder.setMessage(getString(R.string.cita_asignada_correctamente) +
                    getString(R.string.mensaje_cita_asignada_fecha) + etFechaCita.getText().toString() + " " + hora + ":" + minutoStr + "\n" +
                    getString(R.string.mensaje_cita_asignada_tipo) + spinnerSelecTipoCita.getSelectedItem().toString() + "\n" +
                    getString(R.string.mensaje_cita_asignada_descripcion) + etDescripcionCita.getText().toString() + "\n" +
                    getString(R.string.mensaje_cita_asignada_mascota) + spinnerSelecMascota.getSelectedItem().toString());
            builder.setPositiveButton("OK", (dialog, which) -> {
                Intent intent = new Intent(EditarCita_Activity.this, GestionarCitas_Activity.class);
                intent.putExtra("idUsuario", idUsuario);
                startActivity(intent);
                finish();
            });
            builder.create().show();

        } catch (SQLException ex) {
            ex.printStackTrace();
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


    private void enviarMensajeABaseDeDatos(Mensaje mensaje) {
        Connection connection = null;
        try {
            connection = MySQLConnection.getConnection();
            String sql = "INSERT INTO Mensajes (AsuntoMensaje, TipoMensaje, ContenidoMensaje, FechaHoraMensaje, IdUsuario) VALUES (?, ?, ?, ?, ?)";

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            java.sql.Timestamp timestamp = new java.sql.Timestamp(calendar.getTimeInMillis());

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, mensaje.getAsunto());
            statement.setString(2, mensaje.getTipoMensaje());
            statement.setString(3, mensaje.getContenido());
            statement.setString(4, mensaje.getFecha());
            statement.setInt(5, mensaje.getIdUsuario());
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
