package com.example.activity_inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import es.dam.model.adaptadores.AdaptadorUsuarios;
import es.dam.model.objetos.Cita;
import es.dam.model.objetos.Usuario;
import es.dam.sql.MySQLConnection;

public class GestionarUsuarios_Activity extends AppCompatActivity {

    private ListView listViewUsuarios;
    private AdaptadorUsuarios adaptadorUsuarios;
    private ArrayList<Usuario> listaUsuarios;
    private TextView btnBuscarUsuario;
    private EditText etBuscarUsuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_usuarios);

        listViewUsuarios = findViewById(R.id.listViewUsuarios);
        btnBuscarUsuario = findViewById(R.id.btnBuscarUsuario);
        etBuscarUsuario = findViewById(R.id.etBuscarUsuario);

        // Cargar usuarios desde la base de datos
        cargarUsuariosDesdeBaseDeDatos();

        // Inicializar el adaptador de usuarios con la lista cargada desde la base de datos
        adaptadorUsuarios = new AdaptadorUsuarios(this, listaUsuarios);

        // Establecer el adaptador en el ListView
        listViewUsuarios.setAdapter(adaptadorUsuarios);

        listViewUsuarios.setOnItemClickListener((parent, view, position, id) -> {
            Usuario usuarioSeleccionado = listaUsuarios.get(position);
            int idUsuario = usuarioSeleccionado.getId();
            iniciarEditarUserActivity(idUsuario);
        });
    }

    private void cargarUsuariosDesdeBaseDeDatos() {
        listaUsuarios = new ArrayList<>();
        Connection connection = null;
        try {
            // Establecer conexión con la base de datos
            connection = MySQLConnection.getConnection();
            if (connection != null) {
                String query = "SELECT * FROM Usuarios";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    // Obtener los datos del usuario de la base de datos
                    int idUsuario = resultSet.getInt("IdUsuario");
                    String nombreUsuario = resultSet.getString("NombreUsuario");
                    String apellidosUsuario = resultSet.getString("ApellidosUsuario");
                    String correoElectronico = resultSet.getString("CorreoElectronico");
                    int numTelefono = resultSet.getInt("NumTelefono");
                    boolean estaVetado = resultSet.getBoolean("EstaVetado");
                    String razonVeto = resultSet.getString("RazonVeto");

                    // Crear un objeto Usuario y agregarlo a la lista de usuarios
                    Usuario usuario = new Usuario(idUsuario, nombreUsuario, apellidosUsuario, correoElectronico, numTelefono, estaVetado, razonVeto);
                    listaUsuarios.add(usuario);
                }
            } else {
                Toast.makeText(this, "No se pudo conectar a la base de datos", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cerrar la conexión
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void iniciarEditarUserActivity(int idUsuario) {
        Intent intent = new Intent(GestionarUsuarios_Activity.this, EditarUser_Activity.class);
        intent.putExtra("idUsuario", idUsuario);
        startActivity(intent);
    }

    // Método para mostrar el menú de filtrar
    public void mostrarMenuFiltrar(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_filtrar_users); // archivo xml de menú para el PopupMenu

        // Manejar eventos de clic en las opciones del menú
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.quitar_filtro) {
                    // Filtrar por ID
                    adaptadorUsuarios.quitarFiltro();
                    etBuscarUsuario.setVisibility(View.GONE);
                    btnBuscarUsuario.setVisibility(View.GONE);
                    return true;
                } else if (item.getItemId() == R.id.filtrar_por_veto) {
                    // Filtrar la lista de citas por fecha
                    adaptadorUsuarios.filtrarPorVetados();
                    etBuscarUsuario.setVisibility(View.GONE);
                    btnBuscarUsuario.setVisibility(View.GONE);
                    return true;
                }else if (item.getItemId() == R.id.filtrar_por_noVeto) {
                    // Filtrar la lista de citas por fecha
                    adaptadorUsuarios.filtrarPorNoVetados();
                    etBuscarUsuario.setVisibility(View.GONE);
                    btnBuscarUsuario.setVisibility(View.GONE);
                    return true;
                }else if (item.getItemId() == R.id.buscar_usuario) {
                    // Mostrar el EditText para buscar usuario
                    etBuscarUsuario.setVisibility(View.VISIBLE);
                    // Filtrar por usuario cuando se haga clic en el botón "Buscar"
                    btnBuscarUsuario.setVisibility(View.VISIBLE);
                    btnBuscarUsuario.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adaptadorUsuarios.filtrarPorUsuario(etBuscarUsuario.getText().toString().trim());
                        }
                    });
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Mostrar el menú
        popupMenu.show();
    }



}
