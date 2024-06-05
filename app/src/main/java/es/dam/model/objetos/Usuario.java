package es.dam.model.objetos;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Usuario implements Serializable {
    private final int id;
    private final String nombre;
    private final String apellidos;
    private String correoElectronico;
    private String contrasenia;
    private int numTelefono;
    private boolean estaVetado;
    private String razonVeto;

    public Usuario(int id, String nombre, String apellidos, String correoElectronico, String contrasenia, int numTelefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correoElectronico = correoElectronico;
        this.contrasenia = contrasenia;
        this.numTelefono = numTelefono;
    }

    public Usuario(int id, String nombre, String apellidos) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    public Usuario(int id, String nombre, String apellidos, String correoElectronico, int numTelefono, boolean estaVetado, String razonVeto) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correoElectronico = correoElectronico;
        this.numTelefono = numTelefono;
        this.estaVetado = estaVetado;
        this.razonVeto = razonVeto;
    }

    public int getId() {
        return id;
    }

    public boolean isVetado() {
        return estaVetado;
    }

    public String getRazonVeto() {
        return razonVeto;
    }

    public String getNombre() {
        return nombre;
    }


    public String getApellidos() {
        return apellidos;
    }


    public String getCorreoElectronico() {
        return correoElectronico;
    }


    public String getContrasenia() {
        return contrasenia;
    }
    public void setContrasenia(String contrasenia){
        this.contrasenia = contrasenia;
    }


    public int getNumTelefono() {
        return numTelefono;
    }


    @Override
    public String toString() {
        return nombre + " " + apellidos;
    }
}
