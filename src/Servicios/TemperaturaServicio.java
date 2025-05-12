package Servicios;

import entidades.TemperaturaRegistro;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TemperaturaServicio {

    private final List<TemperaturaRegistro> listaRegistros = new ArrayList<>();
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void importarDatos(String ruta) {
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta))) {
            lector.lines()
                    .skip(1) 
                    .map(linea -> linea.split(","))
                    .map(campos -> new TemperaturaRegistro(
                            campos[0],
                            LocalDate.parse(campos[1], FORMATO),
                            Double.parseDouble(campos[2])
                    ))
                    .forEach(listaRegistros::add);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    public Map<String, Double> obtenerPromedios(LocalDate desde, LocalDate hasta) {
        Predicate<TemperaturaRegistro> enRango = registro ->
                !registro.getFecha().isBefore(desde) && !registro.getFecha().isAfter(hasta);

        return listaRegistros.stream()
                .filter(enRango)
                .collect(Collectors.groupingBy(
                        TemperaturaRegistro::getCiudad,
                        Collectors.averagingDouble(TemperaturaRegistro::getTemperatura)
                ));
    }

    public Optional<TemperaturaRegistro> buscarMaxima(LocalDate fechaDeseada) {
        return listaRegistros.stream()
                .filter(r -> r.getFecha().equals(fechaDeseada))
                .max(Comparator.comparingDouble(TemperaturaRegistro::getTemperatura));
    }

    public Optional<TemperaturaRegistro> buscarMinima(LocalDate fechaDeseada) {
        return listaRegistros.stream()
                .filter(r -> r.getFecha().equals(fechaDeseada))
                .min(Comparator.comparingDouble(TemperaturaRegistro::getTemperatura));
    }

    public List<TemperaturaRegistro> listarTodos() {
        return listaRegistros;
    }
}
