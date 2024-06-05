package es.dam.model.objetos;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.Date;

public class Mascota {
    private int id;
    private final String nombre;
    private String especie;
    private Date fechaNacimiento;
    private Bitmap fotoMascota;
    private int idUsuario;

    private String MascotaIdt;


    public Mascota(int id, String nombre, String especie, Date fechaNacimiento, Bitmap fotoMascota, int idUsuario) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoMascota = fotoMascota;
        this.idUsuario = idUsuario;
    }

    public Mascota(String MascotaIdt, String nombre, String especie, Date fechaNacimiento, Bitmap fotoMascota, int idUsuario) {
        this.MascotaIdt = MascotaIdt;
        this.nombre = nombre;
        this.especie = especie;
        this.fechaNacimiento = fechaNacimiento;
        this.fotoMascota = fotoMascota;
        this.idUsuario = idUsuario;
    }

    public int getId() {
        return id;
    }


    public String getNombre() {
        return nombre;
    }


    public String getEspecie() {
        return especie;
    }


    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }


    public Bitmap getFotoMascota() {
        return fotoMascota;
    }


    public int getIdUsuario() {
        return idUsuario;
    }

    public Mascota(String nombre) {
        this.nombre = nombre;
    }

    public String getMascotaIdt() {
        return MascotaIdt;
    }

    @Override
    public String toString() {
        return id+"-"+nombre;
    }
}
