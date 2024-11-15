package com.alura.literalura.principal;

//import ch.qos.logback.core.encoder.JsonEscapeUtil;

import com.alura.literalura.modelos.*;
import com.alura.literalura.repositoy.AutorRepository;
import com.alura.literalura.repositoy.LibroRepository;
import com.alura.literalura.services.ConsumoAPI;
import com.alura.literalura.services.ConvierteDatos;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Principal {
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversorDatos = new ConvierteDatos();
    private final String API_URL = "https://gutendex.com/books/?search=";
    private Scanner teclado = new Scanner(System.in);
    private Libro libro;
    private DatosLibros datosLibros;
    // Conexión a base de datos
    private LibroRepository libroRepositorio;
    private AutorRepository autorRepositorio;
    private Optional<Autor> autorEncontrado;
    private List<Libro> libros;
    private List<Autor> autores;


    public Principal(AutorRepository autorRepositorio, LibroRepository libroRepositorio) {
        this.autorRepositorio = autorRepositorio;
        this.libroRepositorio = libroRepositorio;

    }

    public void mostrarMenu() {
        var opcion = -1;
        var menuInicio = """
               \n Elija la opción a través de su número
                1- Buscar libro por titulo.
                2- Buscar libros registrados en la base de datos.
                3- Listar autores registrados
                4- Listar autores vivos en un determinado año
                5- Listar libros por idioma
                ----------- Opciones Adicionales -------------
                6- Generar estadisticas (premium).
                7- Ver top 10 de los libros más descargados.
                8- Buscar por Nombre del Autor.
                9- Listar autores por cantidad de libros.
               
                0- Salir
                
                """;

        while (opcion != 0) {
            System.out.println("\n***** Bienvenido a LiterAlura *****");
            System.out.println("Tu biblioteca personal donde puedes almacenar los titulos de tus libros favoritos");
            System.out.println(menuInicio);

            try {
                opcion = teclado.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Error en el ingreso de datos, solo puede ingresar numeros enteros. {1,2,...,9}");

            } finally {
                teclado.nextLine();
            }

            switch (opcion) {
                case 1:
                    buscarLibroPorNombre();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresPorDeterminadoFecha();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 6:
                    generarEstadisticas();
                    break;
                case 7:
                    listarTop10LibrosDescargados();
                    break;
                case 8:
                    consultarAutorPorNombre();
                    break;
                case 9:
                    listarCantidadDeLibroPorAutor();
                    break;
                case 0:
                    System.out.println("Saliendo de la aplicación....");
                    break;
                default:
                    System.out.println("Opcion ingresada no valida, por favor vuelva a intentar.");
//                        throw new EntradaInvalidaException("Vuelava a intentar, Ingrese una opción valida.");
                    break;
            }

        }
    }


    private Datos getDatosLibros() {
        System.out.println("Ingrese el nombre  del libro que desea buscar");
        String nombre = teclado.nextLine();
        String nombreDelLibro = nombre.toLowerCase().replace(" ", "%20");
        var json = consumoAPI.obtenerDatosApi(API_URL + nombreDelLibro);
        var datos = conversorDatos.convertirDatos(json, Datos.class);
        return datos;

    }

    private void buscarLibroPorNombre(){
        Datos datos = getDatosLibros();
        System.out.println(datos);


        Optional<DatosLibros> libros = datos.resultados().stream()
                .findFirst();

        if (libros.isPresent()){
            System.out.println(libros);
            System.out.println("Autor: --" + libros.get().autor());
            Libro libro = new Libro(libros.get());
            Autor autor = new Autor(libros.get().autor().get(0));


            if (libroRepositorio.existsByTitulo(libro.getTitulo())){
                System.out.println("El libro que esta buscando ya esta registrado en la base de datos.");

            } else {
                autorEncontrado = autorRepositorio.findByNombreContainsIgnoreCase(autor.getNombre());
                if (autorEncontrado.isPresent()){
                    var autorIngresado = autorEncontrado.get();
                    libro.setAutor(autorIngresado);
                    libroRepositorio.save(libro);
                } else {
                    autor.setLibrosDelAutor(libro);
                    autorRepositorio.save(autor);
                }
                System.out.println(libro.toString());
                System.out.println("Libro guardado con exito.\n");
            }

        } else {
            System.out.println("Libro no encontrado");
        }


    }

    private void listarLibrosRegistrados(){
        System.out.println("Libros resgistrados en la base de datos.");
        libros = libroRepositorio.findAll();
        libros.stream()
                .forEach(System.out::println);
    }

    private void listarLibrosPorIdioma(){
        System.out.println("Ingrese el ídioma para buscar los libros:");
        var menuDeIdiomas = """
                es- español
                en- ingles
                fr- frances
                pt- portugues
                """;
        System.out.println(menuDeIdiomas);
        var idiomaSeleccionado = teclado.nextLine();
        libros = libroRepositorio.findByIdioma(idiomaSeleccionado);
        System.out.println("Libros listado por idiomas.");
        libros.stream()
                .sorted(Comparator.comparing(Libro::getNumeroDeDescargas).reversed())
                .forEach(System.out::println);
    }

    private void listarAutoresRegistrados(){
        System.out.println("------------ Autores -----------");
        autores = autorRepositorio.findAll();
        AtomicInteger contador = new AtomicInteger(1);
        autores.stream()
//                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(autor -> {
                    System.out.println("Autor numero: "
                            + contador.getAndIncrement());
                    System.out.println(autor.toString());
                });
    }


    private void listarAutoresPorDeterminadoFecha(){
        System.out.println("Ingrese el rango de fecha para encontrar autores vivos en determinado año: ");
        System.out.println("Ingrese la fecha inicial:");
        var primeraFecha = teclado.nextInt();
        System.out.println("Ingrese la fecha final:");
        var segundaFecha = teclado.nextInt();
        System.out.println("Autores vivos entre " + primeraFecha + "y " + segundaFecha);
        autores = autorRepositorio.buscarAutorVivoEnDeterminadaFecha(primeraFecha, segundaFecha);
        autores.forEach(System.out::println);
    }


    private void generarEstadisticas(){
        libros = libroRepositorio.findAll();
        IntSummaryStatistics estadisticas = libros.stream()
                .filter(libro -> libro.getNumeroDeDescargas() > 0)
                .collect(Collectors.summarizingInt(Libro::getNumeroDeDescargas));
        System.out.println("\nEstadisticas de libros en la bases de datos");
        System.out.println("Número de registros de la base de datos: " + estadisticas.getCount());
        System.out.println("Media de descargas: " + estadisticas.getAverage());
        System.out.println("Número mayor de descargas: " + estadisticas.getMax());
        System.out.println("Numero menor de descargas: " + estadisticas.getMin());

    }

    private void listarTop10LibrosDescargados(){
        System.out.println("\n Top 10 libros mas decargados");
        libros = libroRepositorio.findAll();
        AtomicInteger contador = new AtomicInteger(1);
        libros.stream()
                .sorted(Comparator.comparing(Libro::getNumeroDeDescargas).reversed())
                .map(libro -> contador.getAndIncrement() + ". " + libro.getTitulo() +
                        " - descargas: " + libro.getNumeroDeDescargas())
                .limit(10)
                .forEach(System.out::println);

    }

    private void consultarAutorPorNombre(){
        System.out.println("Ingrese el nombre del autor que deseaa buscar en la base de datos");
        var nombreDelAutor = teclado.nextLine();

        autorEncontrado = autorRepositorio.findByNombreContainsIgnoreCase(nombreDelAutor);

        if (autorEncontrado.isPresent()){
            Autor autorDevuelto = autorEncontrado.get();
            System.out.println("Autor encontrado");
            System.out.println(autorDevuelto.toString());
        } else {
            System.out.println("No se encontro el autor en la base de datos.");
            autorEncontrado = null;

        }

    }

    private void listarCantidadDeLibroPorAutor(){
        System.out.println("Cantidad de libros por autor.");
        autores = autorRepositorio.findAll();
        AtomicInteger contador = new AtomicInteger(1);
//        autores.stream()
//                .sorted(Comparator.comparingInt( autor -> autor.getLibrosAutor().size()*-1))
//                .forEach(autor -> System.out.println(contador.getAndIncrement() +
//                        "." +autor.getNombre() + "  - libros: "
//                        + autor.getLibrosAutor().size()));
        autores.stream()
                .sorted(Comparator.comparingInt( autor -> autor.getLibrosAutor().size()*-1))
                .forEach(autor -> {
                    String[] nombre = (autor.getNombre().split(","));
                    String nombreCompleto = nombre[1] + " " + nombre [0];
                    System.out.println(contador.getAndIncrement() +
                            ". " + nombreCompleto + "  - libros: "
                            + autor.getLibrosAutor().size());
                });

    }


}











