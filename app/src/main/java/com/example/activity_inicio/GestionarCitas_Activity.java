package com.example.activity_inicio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import es.dam.model.adaptadores.AdaptadorCitas;
import es.dam.model.objetos.Cita;
import es.dam.sql.MySQLConnection;

public class GestionarCitas_Activity extends AppCompatActivity {

    private ListView listViewCitas;
    private AdaptadorCitas adaptadorCitas;
    private ArrayList<Cita> listaCitas;
    private EditText etBuscarUsuario;
    private TextView btnBuscarUsuario;
    private ImageView añadirCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_citas);

        listViewCitas = findViewById(R.id.listViewCitas);
        etBuscarUsuario = findViewById(R.id.etBuscarUsuario);
        btnBuscarUsuario = findViewById(R.id.btnBuscarUsuario);
        añadirCita = findViewById(R.id.añadirCita);

        // Aquí llamas a un método para cargar las citas desde la base de datos
        cargarCitasDesdeBaseDeDatos();

        adaptadorCitas = new AdaptadorCitas(this, listaCitas);
        listViewCitas.setAdapter(adaptadorCitas);

        listViewCitas.setOnItemClickListener((parent, view, position, id) -> {
            Cita citaSeleccionada = listaCitas.get(position);
            int idUsuario = citaSeleccionada.getIdUsuario();
            int idCita = citaSeleccionada.getId();
            iniciarEditarCitaActivity(idUsuario, idCita);
        });

        // Configurar el listener para el ImageView de añadir usuario
        añadirCita.setOnClickListener(v -> iniciarAñadirUsuarioActivity());
    }

    // Método para cargar las citas desde la base de datos
    private void cargarCitasDesdeBaseDeDatos() {
        listaCitas = new ArrayList<>();
        try {
            Connection connection = MySQLConnection.getConnection();
            if (connection != null) {
                String query = "SELECT * FROM Citas";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    // Obtener los datos de la cita de la base de datos
                    int idCita = resultSet.getInt("IdCita");
                    String tituloCita = resultSet.getString("TipoCita");
                    String descripcionCita = resultSet.getString("DescripcionCita");
                    String fechaCita = resultSet.getString("FechaHora");
                    double importeHistorial = obtenerImporteCita(tituloCita);
                    int idMascota = resultSet.getInt("IdMascota");

                    // Crear un objeto Cita y agregarlo a la lista de citas
                    Cita cita = new Cita(idCita, fechaCita, descripcionCita, importeHistorial, tituloCita, idMascota);
                    listaCitas.add(cita);
                }
                connection.close();
            } else {
                Toast.makeText(this, "No se pudo conectar a la base de datos", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Dentro de tu método donde inicias la actividad de editar cita
    private void iniciarEditarCitaActivity(int idUsuario, int idCita) {
        Intent intent = new Intent(GestionarCitas_Activity.this, EditarCita_Activity.class);
        intent.putExtra("idUsuario", idUsuario);
        intent.putExtra("idCita", idCita);
        startActivity(intent);
    }

    // Método para iniciar la actividad para añadir un usuario
    private void iniciarAñadirUsuarioActivity() {
        Intent intent = new Intent(GestionarCitas_Activity.this, AniadirCita_Activity.class);
        startActivity(intent);
    }

    // Método para obtener el importe de la cita a partir del tipo de cita
    private double obtenerImporteCita(String citaElegida) {
        // Eliminar el símbolo del euro y espacios en blanco al principio y al final
        if (!citaElegida.equals("Otros - Por favor, consulte con el veterinario")) {
            String cantidad = citaElegida.replaceAll("[^0-9.]", "").trim();
            return Double.parseDouble(cantidad);
        } else {
            return 0.0;
        }
    }

    // Método para mostrar el menú de filtrar
    public void mostrarMenuFiltrar(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_filtrar_citas); // archivo xml de menú para el PopupMenu

        // Manejar eventos de clic en las opciones del menú
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.filtrar_por_id) {
                // Filtrar por ID
                adaptadorCitas.filtrarPorId();
                etBuscarUsuario.setVisibility(View.GONE);
                btnBuscarUsuario.setVisibility(View.GONE);
                return true;
            } else if (item.getItemId() == R.id.filtrar_por_fecha) {
                // Filtrar la lista de citas por fecha
                adaptadorCitas.filtrarPorFecha();
                etBuscarUsuario.setVisibility(View.GONE);
                btnBuscarUsuario.setVisibility(View.GONE);
                return true;
            } else if (item.getItemId() == R.id.buscar_usuario) {
                // Mostrar el EditText para buscar usuario
                etBuscarUsuario.setVisibility(View.VISIBLE);
                // Filtrar por usuario cuando se haga clic en el botón "Buscar"
                btnBuscarUsuario.setVisibility(View.VISIBLE);
                btnBuscarUsuario.setOnClickListener(v -> adaptadorCitas.filtrarPorUsuario(etBuscarUsuario.getText().toString().trim()));
                return true;
            } else {
                return false;
            }
        });

        // Mostrar el menú
        popupMenu.show();
    }
}
