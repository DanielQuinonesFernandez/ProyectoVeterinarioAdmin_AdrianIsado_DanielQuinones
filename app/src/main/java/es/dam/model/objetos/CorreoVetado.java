package es.dam.model.objetos;

public class CorreoVetado {
    private int id;
    private String razonVeto;
    private String correoVetado;

    public CorreoVetado(int id, String razonVeto, String correoVetado) {
        this.id = id;
        this.razonVeto = razonVeto;
        this.correoVetado = correoVetado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRazonVeto() {
        return razonVeto;
    }

    public void setRazonVeto(String razonVeto) {
        this.razonVeto = razonVeto;
    }

    public String getCorreoVetado() {
        return correoVetado;
    }

    public void setCorreoVetado(String correoVetado) {
        this.correoVetado = correoVetado;
    }

    @Override
    public String toString() {
        return "CorreoVetado{" +
                "id=" + id +
                ", razonVeto='" + razonVeto + '\'' +
                ", correoVetado='" + correoVetado + '\'' +
                '}';
    }
}
