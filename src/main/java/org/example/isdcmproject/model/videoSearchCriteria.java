package org.example.isdcmproject.model;

import java.time.LocalDate;

public class videoSearchCriteria {
    private String consultaLibre;
    private String titulo;
    private String categoria;
    private String resolucion;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Integer duracionMin;
    private Integer duracionMax;

    public String getConsultaLibre() {
        return consultaLibre == null ? "" : consultaLibre;
    }

    public void setConsultaLibre(String consultaLibre) {
        this.consultaLibre = consultaLibre;
    }

    public String getTitulo() {
        return titulo == null ? "" : titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCategoria() {
        return categoria == null ? "" : categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getResolucion() {
        return resolucion == null ? "" : resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Integer getDuracionMin() {
        return duracionMin;
    }

    public void setDuracionMin(Integer duracionMin) {
        this.duracionMin = duracionMin;
    }

    public Integer getDuracionMax() {
        return duracionMax;
    }

    public void setDuracionMax(Integer duracionMax) {
        this.duracionMax = duracionMax;
    }
}
