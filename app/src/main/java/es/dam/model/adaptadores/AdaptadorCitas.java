package es.dam.model.adaptadores;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.activity_inicio.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import es.dam.model.objetos.Cita;
import es.dam.sql.MySQLConnection;

public class AdaptadorCitas extends BaseAdapter {
    private final Context context;
    private ArrayList<Cita> citas;
    private ArrayList<Cita> citasFiltradas; // Lista de citas filtradas

    public AdaptadorCitas(Context context, ArrayList<Cita> citas) {
        this.context = context;
        this.citas = citas;
        this.citasFiltradas = new ArrayList<>(citas); // Inicializar la lista de citas filtradas con todas las citas
    }

    @Override
    public int getCount() {
        return citasFiltradas.size();
    }

    @Override
    public Object getItem(int position) {
        return citasFiltradas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_cita, parent, false);

        TextView tvIdCita = rowView.findViewById(R.id.tvIdCita);
        TextView tvFechaDeLaCita = rowView.findViewById(R.id.tvFechaDeLaCita);
        TextView tvHoraDeLaCita = rowView.findViewById(R.id.tvHoraDeLaCita);
        TextView tvPersonaAsignada = rowView.findViewById(R.id.tvPersonaAsignada);
        TextView tvMascotaAsignada = rowView.findViewById(R.id.tvMascotaAsignada);
        TextView tvTipoCita = rowView.findViewById(R.id.tvTipoCita);
        ImageView btnEliminar = rowView.findViewById(R.id.iconoEliminarMascota);

        Cita cita = citasFiltradas.get(position);

        tvIdCita.setText(String.valueOf(cita.getId()));

        // Obtener fecha y hora por separado usando substring
        String fechaCita = cita.getFechaCita();
        String fecha = fechaCita.substring(0, 10); // Extraer la fecha (primeros 10 caracteres)
        String hora = fechaCita.substring(11); // Extraer la hora (desde el índice 11 hasta el final)

        tvFechaDeLaCita.setText(fecha);
        tvHoraDeLaCita.setText(hora);
        tvPersonaAsignada.setText(obtenerPersonaAsignada(cita.getId()));
        tvMascotaAsignada.setText(obtenerMascotaAsignada(cita.getIdMascota()));
        tvTipoCita.setText(cita.getTituloCita());

        // Configurar el OnClickListener para el botón de eliminar
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoConfirmacionEliminar(cita);
            }
        });

        return rowView;
    }

    // Método para mostrar el diálogo de confirmación antes de eliminar la cita
    private void mostrarDialogoConfirmacionEliminar(Cita cita) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Eliminar Cita");
        builder.setMessage("¿Estás seguro de que quieres eliminar esta cita?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        // Configurar el botón positivo
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Lógica para eliminar la cita
                try {
                    Connection connection = MySQLConnection.getConnection();
                    String sql = "DELETE FROM Citas WHERE IdCita = ?";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setInt(1, cita.getId());
                    statement.executeUpdate();
                    connection.close();

                    // Eliminar la cita de la lista y notificar al adaptador
                    citas.remove(cita);
                    notifyDataSetChanged();
                    // Recargar la actividad
                    ((Activity) context).recreate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        // Configurar el botón negativo
        builder.setNegativeButton("Cancelar", null);

        // Mostrar el diálogo
        builder.show();
    }


    // Método para obtener el nombre del usuario asignado a una cita
    private String obtenerPersonaAsignada(int idCita) {
        String personaAsignada = "";
        try {
            Connection connection = MySQLConnection.getConnection();

            // Obtener el ID del usuario a partir del ID de la mascota asignada a la cita
            String query = "SELECT IdUsuario FROM Mascotas WHERE IdMascota = (SELECT IdMascota FROM Citas WHERE IdCita = ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, idCita);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String idUsuario = resultSet.getString("IdUsuario");
                if (idUsuario.startsWith("DEL")) {
                    // Elimina los primeros tres caracteres "DEL"
                    idUsuario = idUsuario.substring(3);
                }

                // Ahora, usando el ID del usuario, podemos obtener el nombre de la persona asignada
                query = "SELECT * FROM Usuarios WHERE IdUsuario = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, idUsuario);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    personaAsignada = resultSet.getString("NombreUsuario") + " " + resultSet.getString("ApellidosUsuario");

                }
            }

            connection.close();
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return personaAsignada;
    }

    // Método para obtener el nombre de la mascota asignada a una cita
    private String obtenerMascotaAsignada(int idMascota) {
        String mascotaAsignada = "";
        try {
            Connection connection = MySQLConnection.getConnection();

            String query = "SELECT NombreMascota FROM Mascotas WHERE IdMascota = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, idMascota);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                mascotaAsignada = resultSet.getString("NombreMascota");
            }

            connection.close();
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return mascotaAsignada;
    }

    // Método para filtrar la lista de citas por fecha
    public void filtrarPorFecha() {
        Collections.sort(citasFiltradas, new Comparator<Cita>() {
            @Override
            public int compare(Cita cita1, Cita cita2) {
                // Obtener las fechas como cadenas
                String fechaCita1 = cita1.getFechaCita();
                String fechaCita2 = cita2.getFechaCita();

                // Comparar las fechas como cadenas
                return fechaCita2.compareTo(fechaCita1);
            }
        });
        notifyDataSetChanged();
    }


    // Método para filtrar la lista de citas por el nombre de usuario
    public void filtrarPorUsuario(String nombreUsuario) {
        ArrayList<Cita> citasTemp = new ArrayList<>();
        for (Cita cita : citas) {
            if (obtenerPersonaAsignada(cita.getId()).contains(nombreUsuario)) {
                citasTemp.add(cita);
            }
        }
        citasFiltradas = citasTemp;
        notifyDataSetChanged();
    }

    // Método para restablecer la lista de citas filtradas por ID
    public void filtrarPorId() {
        citasFiltradas.clear(); // Limpiar la lista de citas filtradas
        citasFiltradas.addAll(citas); // Agregar todas las citas de nuevo
        notifyDataSetChanged();
    }

}
