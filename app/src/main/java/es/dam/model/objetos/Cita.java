package es.dam.model.objetos;

import androidx.annotation.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import es.dam.sql.MySQLConnection;

public class Cita {
    private int id;
    private String fechaCita;
    private String descripcionCita;
    private double precioCita;
    private String tituloCita;
    private int idMascota;

    public Cita(int id, String fechaCita, String descripcionCita, double precioCita, String tituloCita, int idMascota) {
        this.id = id;
        this.fechaCita = fechaCita;
        this.descripcionCita = descripcionCita;
        this.precioCita = precioCita;
        this.tituloCita = tituloCita;
        this.idMascota = idMascota;
    }

    public Cita() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFechaCita() {
        return fechaCita;
    }
    public String setFechaCita(String fechaCita) {
        this.fechaCita = fechaCita;
        return fechaCita;
    }
    public String getDescripcionCita() {
        return descripcionCita;
    }

    public void setDescripcionCita(String descripcionCita) {
        this.descripcionCita = descripcionCita;
    }

    public double getPrecioCita() {
        return precioCita;
    }

    public void setPrecioCita(double precioCita) {
        this.precioCita = precioCita;
    }

    public String getTituloCita() {
        return tituloCita;
    }

    public void setTituloCita(String tituloCita) {
        this.tituloCita = tituloCita;
    }

    public int getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(int idMascota) {
        this.idMascota = idMascota;
    }

    public int getIdUsuario() {
        int idUsuario = -1; // Default value or error indicator

        try {
            Connection connection = MySQLConnection.getConnection();

            String sql = "SELECT IdUsuario FROM Mascotas WHERE IdMascota = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, idMascota);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                idUsuario = resultSet.getInt("IdUsuario");
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }

        return idUsuario;
    }

    @NonNull
    @Override
    public String toString() {
        return "Cita{" +
                "id=" + id +
                ", fechaCita=" + fechaCita +
                ", descripcionCita='" + descripcionCita + '\'' +
                ", precioCita=" + precioCita +
                ", tituloCita='" + tituloCita + '\'' +
                ", idMascota=" + idMascota +
                '}';
    }
}
