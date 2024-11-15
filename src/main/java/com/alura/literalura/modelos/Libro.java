package com.alura.literalura.modelos;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(unique = true)
    private String titulo;
    private String nombreAutor;
    private String idioma;
    private Integer numeroDeDescargas;
    @ManyToOne()
    private Autor autor;

    public Libro(){

    }

    public Libro(DatosLibros libro) {
        this.titulo = libro.titulo();
        this.numeroDeDescargas = libro.numeroDeDescargas();
        Autor autor = new Autor(libro.autor().get(0));
        this.nombreAutor = autor.getNombre();
        this.idioma = libro.idiomas().get(0);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNombreAutor() {
        return nombreAutor;
    }

    public void setNombreAutor(String nombreAutor) {
        this.nombreAutor = nombreAutor;
    }


    public Integer getNumeroDeDescargas() {
        return numeroDeDescargas;
    }

    public void setNumeroDeDescargas(Integer numeroDeDescargas) {
        this.numeroDeDescargas = numeroDeDescargas;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }


    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    @Override
    public String toString() {
        return  "---------- Libro ----------" +
                "\nTitulo: " + titulo  +
                "\nAutor = " + nombreAutor  +
                "\nIdioma = " + idioma +
                "\nNumero de descargas = " + numeroDeDescargas +
                "\n-------------------------" ;
    }
}
