package es.dam.model.objetos;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Mensaje {
    private int id;
    private int idUsuario;
    private String asunto;
    private String contenido;
    private String fecha;
    private String tipoMensaje;
    private boolean leido;

    public Mensaje(int id, int idUsuario, String asunto, String contenido, String fecha, String tipoMensaje, boolean leido) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.asunto = asunto;
        this.contenido = contenido;
        this.fecha = fecha;
        this.tipoMensaje = tipoMensaje;
        this.leido = leido;
    }

    public Mensaje(int idUsuario, String asunto, String contenido, String tipoMensaje) {
        this.idUsuario = idUsuario;
        this.asunto = asunto;
        this.contenido = contenido;
        this.fecha = obtenerFechaActual();
        this.tipoMensaje = tipoMensaje;
        this.leido = false;
    }

    public static String obtenerFechaActual() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date date = new Date();
        return formatter.format(date);
    }

    public String getFecha() {
        return fecha;
    }

    public int getId() {
        return id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getContenido() {
        return contenido;
    }


    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public boolean isLeido() {
        return leido;
    }

    @Override
    public String toString() {
        return "Mensaje{" +
                "id=" + id +
                ", idUsuario=" + idUsuario +
                ", asunto='" + asunto + '\'' +
                ", contenido='" + contenido + '\'' +
                ", fecha=" + fecha +
                ", tipoMensaje='" + tipoMensaje + '\'' +
                ", leido=" + leido +
                '}';
    }
}
