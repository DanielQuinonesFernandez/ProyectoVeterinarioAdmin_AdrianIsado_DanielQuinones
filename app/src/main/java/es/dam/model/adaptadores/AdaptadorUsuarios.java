package es.dam.model.adaptadores;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.activity_inicio.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import es.dam.model.objetos.Usuario;
import es.dam.sql.MySQLConnection;

public class AdaptadorUsuarios extends BaseAdapter {

    private final Context context;
    private ArrayList<Usuario> usuarios;
    private ArrayList<Usuario> usuariosFiltrados;

    public AdaptadorUsuarios(Context context, ArrayList<Usuario> usuarios) {
        this.context = context;
        this.usuarios = usuarios;
        this.usuariosFiltrados = new ArrayList<>(usuarios); // Inicializar la lista de usuarios filtrados con todos los usuarios
    }

    @Override
    public int getCount() {
        return usuariosFiltrados.size();
    }

    @Override
    public Object getItem(int position) {
        return usuariosFiltrados.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_user, parent, false);
        }

        TextView tvIdUsuario = convertView.findViewById(R.id.tvIdUsuario);
        TextView tvNombreUsuario = convertView.findViewById(R.id.tvNombreUsuario);
        TextView tvApellidosUsuario = convertView.findViewById(R.id.tvApellidosUsuario);
        TextView tvCorreoElectronico = convertView.findViewById(R.id.tvCorreoElectronico);
        TextView tvNumTelefono = convertView.findViewById(R.id.tvNumTelefono);
        TextView tvRazonVeto = convertView.findViewById(R.id.tvRazonVeto);
        CheckBox checkBoxVetado = convertView.findViewById(R.id.checkBoxVetado);
        ImageView btnEliminar = convertView.findViewById(R.id.iconoEliminarUsuario);

        Usuario usuario = usuariosFiltrados.get(position);

        tvIdUsuario.setText(String.valueOf(usuario.getId()));
        tvNombreUsuario.setText(usuario.getNombre());
        tvApellidosUsuario.setText(usuario.getApellidos());
        tvCorreoElectronico.setText(usuario.getCorreoElectronico());
        tvNumTelefono.setText(String.valueOf(usuario.getNumTelefono()));

        // Marcar el CheckBox dependiendo del estado vetado
        checkBoxVetado.setChecked(usuario.isVetado());

        // Mostrar u ocultar la razón del veto
        if (usuario.isVetado()) {
            String razonVeto = obtenerRazonVeto(usuario.getId());
            tvRazonVeto.setText("Razón del Veto: " + razonVeto);
            tvRazonVeto.setVisibility(View.VISIBLE);
        } else {
            tvRazonVeto.setVisibility(View.GONE);
        }

        btnEliminar.setOnClickListener(v -> {
            String mensaje = "¿Estás seguro de que quieres eliminar a " + usuario.getNombre() + "?";
            mostrarDialogoConfirmacionEliminar(mensaje, usuario);
        });

        return convertView;
    }

    private void eliminarUsuarioDeBaseDeDatos(Usuario usuario) {
        try {
            Connection connection = MySQLConnection.getConnection();
            if (connection != null) {
                connection.setAutoCommit(false);

                String eliminarCitasSql = "DELETE FROM Citas WHERE IdMascota IN (SELECT IdMascota FROM Mascotas WHERE IdUsuario = ?)";
                PreparedStatement eliminarCitasStatement = connection.prepareStatement(eliminarCitasSql);
                eliminarCitasStatement.setInt(1, usuario.getId());
                eliminarCitasStatement.executeUpdate();

                String eliminarMascotasSql = "DELETE FROM Mascotas WHERE IdUsuario = ?";
                PreparedStatement eliminarMascotasStatement = connection.prepareStatement(eliminarMascotasSql);
                eliminarMascotasStatement.setInt(1, usuario.getId());
                eliminarMascotasStatement.executeUpdate();

                String eliminarMensajesSql = "DELETE FROM Mensajes WHERE IdUsuario = ?";
                PreparedStatement eliminarMensajesStatement = connection.prepareStatement(eliminarMensajesSql);
                eliminarMensajesStatement.setInt(1, usuario.getId());
                eliminarMensajesStatement.executeUpdate();

                String eliminarUsuarioSql = "DELETE FROM Usuarios WHERE IdUsuario = ?";
                PreparedStatement eliminarUsuarioStatement = connection.prepareStatement(eliminarUsuarioSql);
                eliminarUsuarioStatement.setInt(1, usuario.getId());
                int rowsDeleted = eliminarUsuarioStatement.executeUpdate();

                if (rowsDeleted > 0) {
                    Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    ((Activity) context).recreate();
                } else {
                    Toast.makeText(context, "No se pudo eliminar el usuario", Toast.LENGTH_SHORT).show();
                }

                connection.commit();
                connection.close();
            } else {
                Toast.makeText(context, "No se pudo conectar a la base de datos", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String obtenerRazonVeto(int idUsuario) {
        String razonVeto = "";
        try {
            Connection connection = MySQLConnection.getConnection();
            if (connection != null) {
                String query = "SELECT RazonVeto FROM Usuarios WHERE IdUsuario = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, idUsuario);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    razonVeto = resultSet.getString("RazonVeto");
                }
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return razonVeto;
    }

    private void mostrarDialogoConfirmacionEliminar(String mensaje, Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar Usuario");
        builder.setMessage(mensaje);

        builder.setPositiveButton("Sí", (dialogInterface, i) -> {
            String confirmacionMensaje = "Usuario " + usuario.getNombre() + " eliminado";
            Toast.makeText(context, confirmacionMensaje, Toast.LENGTH_SHORT).show();
            eliminarUsuarioDeBaseDeDatos(usuario);
            ((Activity) context).recreate();
        });

        builder.setNegativeButton("Cancelar", (dialogInterface, i) -> {
        });

        builder.show();
    }

    // Método para filtrar la lista de usuarios por ID
    public void quitarFiltro() {
        usuariosFiltrados.clear();
        usuariosFiltrados.addAll(usuarios); // Restablecer la lista de usuarios filtrados con todos los usuarios
        notifyDataSetChanged();
    }

    // Método para filtrar la lista de usuarios por estado de veto
    public void filtrarPorVetados() {
        usuariosFiltrados.clear();
        for (Usuario usuario : usuarios) {
            if (usuario.isVetado()) {
                usuariosFiltrados.add(usuario);
            }
        }
        notifyDataSetChanged();
    }

    // Método para filtrar la lista de usuarios por usuarios no vetados
    public void filtrarPorNoVetados() {
        usuariosFiltrados.clear();
        for (Usuario usuario : usuarios) {
            if (!usuario.isVetado()) {
                usuariosFiltrados.add(usuario);
            }
        }
        notifyDataSetChanged();
    }

    // Método para filtrar la lista de usuarios por nombre de usuario
    public void filtrarPorUsuario(String nombreUsuario) {
        usuariosFiltrados.clear();
        for (Usuario usuario : usuarios) {
            if (usuario.getNombre().contains(nombreUsuario)) {
                usuariosFiltrados.add(usuario);
            }
        }
        notifyDataSetChanged();
    }


}



