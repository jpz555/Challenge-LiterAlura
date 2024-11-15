package com.alura.literalura.services;

public interface IConvierteDatos {
    <T> T convertirDatos(String json, Class<T> clase);
}
