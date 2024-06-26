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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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

import es.dam.model.objetos.Mascota;
import es.dam.model.objetos.Mensaje;
import es.dam.model.objetos.Usuario;
import es.dam.sql.MySQLConnection;

public class AniadirCita_Activity extends AppCompatActivity {

    private final ArrayList<Mascota> mascotasDeEsteUsuario = new ArrayList<>();
    private final ArrayList<Usuario> todosLosUsuarios = new ArrayList<>();
    private EditText etDescripcionCita, edOtroTipoCita, etFechaCita;
    private Spinner spinnerSelecMascota, spinnerSelecTipoCita, spinnerSelectUser;
    private TimePicker timePicker;
    private TextView tvFechaHoraSeleccionada, tvSeleccionaMascota, btnAniadirCita;
    private ArrayList<String> mascotasUsuarios = new ArrayList<>();
    private int idUsuario, idCita;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aniadir_cita);

        // Inicializar vistas
        tvFechaHoraSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        tvSeleccionaMascota = findViewById(R.id.tvSeleccionaMascota);
        btnAniadirCita = findViewById(R.id.btnAniadirCita);
        ImageButton btnFechaCita = findViewById(R.id.btnFechaCita);

        timePicker = findViewById(R.id.timePickerPedirCita);

        edOtroTipoCita = findViewById(R.id.edOtroTipoCita);
        etDescripcionCita = findViewById(R.id.etOtrosDatosRelevantes);
        etFechaCita = findViewById(R.id.etFechaCita);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDateTime now = LocalDateTime.now();
            etFechaCita.setText(dtf.format(now));
        } else {
            etFechaCita.setText(R.string.fecha_por_defecto);
        }

        rellenarArraylistDesdeBBDDUsuarios();
        obtenerDescripcionCita();

        spinnerSelecMascota = findViewById(R.id.combobox_seleccionMascota);
        ArrayAdapter<String> adapterSelecMascota = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mascotasUsuarios) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
                }
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(mascotasUsuarios.get(position)); // Aquí se establece el texto del elemento en la posición 'position'
                return convertView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                }
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(getItem(position)); // Aquí se establece el texto del elemento en la posición 'position'
                return convertView;
            }
        };
        adapterSelecMascota.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelecMascota.setAdapter(adapterSelecMascota);


        seleccionarMascotaCitaSeleccionada();

        spinnerSelecTipoCita = findViewById(R.id.combobox_seleccionTipoCita);
        ArrayAdapter<CharSequence> adapterSelecTipoCita = ArrayAdapter.createFromResource(this,
                R.array.tipos_de_cita, android.R.layout.simple_spinner_item);
        adapterSelecTipoCita.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelecTipoCita.setAdapter(adapterSelecTipoCita);

        seleccionarTipoCita(adapterSelecTipoCita);

        spinnerSelectUser = findViewById(R.id.combobox_seleccionUsuario);
        ArrayAdapter<Usuario> adapterSelecUser = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, todosLosUsuarios);
        adapterSelecUser.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectUser.setAdapter(adapterSelecUser);

        btnFechaCita.setOnClickListener(v -> mostrarCalendario());

        spinnerSelectUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Usuario usuarioSeleccionado = (Usuario) parent.getItemAtPosition(position);
                idUsuario = usuarioSeleccionado.getId();
                spinnerSelecMascota.setVisibility(View.VISIBLE);
                tvSeleccionaMascota.setVisibility(View.VISIBLE);

                // Limpiar la lista de mascotas y la selección del spinner de mascotas
                mascotasUsuarios.clear();
                ArrayAdapter<String> adapterSelecMascota = (ArrayAdapter<String>) spinnerSelecMascota.getAdapter();
                adapterSelecMascota.clear();
                adapterSelecMascota.notifyDataSetChanged();

                // Llamar al método para rellenar las mascotas con la ID del usuario seleccionado
                rellenarArraylistDesdeBBDDMascotas();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });


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

        btnAniadirCita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connection connection = null;
                try {
                    connection = MySQLConnection.getConnection();
                    if (connection == null) {
                        Toast.makeText(AniadirCita_Activity.this, getString(R.string.error_conectar_bd), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validar que los campos no estén vacíos
                    if (spinnerSelecMascota.getSelectedItem() == null ||
                            spinnerSelecTipoCita.getSelectedItem() == null ||
                            etFechaCita.getText().toString().isEmpty() ||
                            etDescripcionCita.getText().toString().isEmpty()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(AniadirCita_Activity.this);
                        builder.setMessage(getString(R.string.campos_vacios));
                        builder.setPositiveButton("OK", null);
                        builder.create().show();
                        return;
                    }

                    int hora = timePicker.getHour();
                    String minutoStr = String.format("%02d", timePicker.getMinute());

                    String sql = "INSERT INTO Citas (TipoCita, FechaHora, DescripcionCita, IdMascota) VALUES (?, ?, ?, ?)";
                    String mascotaSeleccionada = spinnerSelecMascota.getSelectedItem().toString();
                    int idMascotaElegida = obtenerIdMascota(mascotaSeleccionada);

                    // Verificar si el ID de la mascota elegida es válido
                    if (idMascotaElegida == -1) {
                        Toast.makeText(AniadirCita_Activity.this, getString(R.string.error_no_mascota_seleccionada), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, spinnerSelecTipoCita.getSelectedItem().toString());
                    statement.setString(2, etFechaCita.getText().toString() + " " + hora + ":" + minutoStr);
                    statement.setString(3, etDescripcionCita.getText().toString());
                    statement.setInt(4, idMascotaElegida);
                    statement.executeUpdate();

                    // Crear y enviar objeto Mensaje
                    Mensaje mensaje = new Mensaje(idUsuario, getString(R.string.cita_anadida), getString(R.string.cita_anadida_exito), getString(R.string.cita_anadida));
                    enviarMensajeABaseDeDatos(mensaje);

                    AlertDialog.Builder builder = new AlertDialog.Builder(AniadirCita_Activity.this);
                    builder.setMessage(getString(R.string.cita_asignada_correctamente) +
                            getString(R.string.mensaje_cita_asignada_fecha) + etFechaCita.getText().toString() + " " + hora + ":" + minutoStr + "\n" +
                            getString(R.string.mensaje_cita_asignada_tipo) + spinnerSelecTipoCita.getSelectedItem().toString() + "\n" +
                            getString(R.string.mensaje_cita_asignada_descripcion) + etDescripcionCita.getText().toString() + "\n" +
                            getString(R.string.mensaje_cita_asignada_mascota) + mascotaSeleccionada);
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        Intent intent = new Intent(AniadirCita_Activity.this, GestionarCitas_Activity.class);
                        intent.putExtra("idUsuario", idUsuario);
                        startActivity(intent);
                        finish();
                    });
                    connection.close();
                    builder.create().show();

                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
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


    private void rellenarArraylistDesdeBBDDMascotas() {
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
                mascotasUsuarios.add(idNombreConcatenado);
            }

            resultSet.close();
            connection.close();

            if (mascotasUsuarios.isEmpty()) {
                Toast.makeText(this, getString(R.string.mascotas_no_encontradas), Toast.LENGTH_LONG).show();
                finish();
            } else {
                spinnerSelecMascota.setSelection(0);
                ((ArrayAdapter) spinnerSelecMascota.getAdapter()).notifyDataSetChanged();
            }
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

    private void rellenarArraylistDesdeBBDDUsuarios() {
        try {
            Connection connection = MySQLConnection.getConnection();

            String sql = "SELECT * FROM Usuarios";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            todosLosUsuarios.clear();

            while (resultSet.next()) {
                int idUsuario = resultSet.getInt("IdUsuario");
                String nombreUsuario = resultSet.getString("NombreUsuario");
                String apellidos = resultSet.getString("ApellidosUsuario");

                todosLosUsuarios.add(new Usuario(idUsuario, nombreUsuario, apellidos));
            }

            resultSet.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
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
