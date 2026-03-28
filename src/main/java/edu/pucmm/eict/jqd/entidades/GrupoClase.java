package edu.pucmm.eict.jqd.entidades;

import java.util.ArrayList;
import java.util.List;

public class GrupoClase {
    private int numeroClase;
    private String nombre;
    private Profesor profesor;
    private List<Estudiante> estudiantes;

    public GrupoClase() {
        this.estudiantes = new ArrayList<>();
    }

    public GrupoClase(int numeroClase, String nombre, Profesor profesor, List<Estudiante> estudiantes) {
        this.numeroClase = numeroClase;
        this.nombre = nombre;
        this.profesor = profesor;
        this.estudiantes = estudiantes != null ? estudiantes : new ArrayList<>();
    }

    public int getNumeroClase() { return numeroClase; }
    public void setNumeroClase(int numeroClase) { this.numeroClase = numeroClase; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public List<Estudiante> getEstudiantes() { return estudiantes; }
    public void setEstudiantes(List<Estudiante> estudiantes) { this.estudiantes = estudiantes; }

    @Override
    public String toString() {
        return "GrupoClase{numeroClase=" + numeroClase + ", nombre='" + nombre + "', profesor=" + profesor + "}";
    }
}
