package edu.pucmm.eict.jqd.datos;

import edu.pucmm.eict.jqd.entidades.Estudiante;
import edu.pucmm.eict.jqd.entidades.GrupoClase;
import edu.pucmm.eict.jqd.entidades.Profesor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Almacén de datos en memoria.
 * Singleton que actúa como repositorio para el demo.
 */
public class AlmacenDatos {

    private static final AlmacenDatos INSTANCIA = new AlmacenDatos();

    private final List<Estudiante> estudiantes = new CopyOnWriteArrayList<>();
    private final List<Profesor>   profesores  = new CopyOnWriteArrayList<>();
    private final List<GrupoClase> grupos      = new CopyOnWriteArrayList<>();

    private final AtomicInteger contadorEstudiante = new AtomicInteger(1);
    private final AtomicInteger contadorProfesor   = new AtomicInteger(1);
    private final AtomicInteger contadorGrupo      = new AtomicInteger(1);

    /**
     * Sink (publicador caliente) para emitir eventos cuando se agrega un estudiante.
     * Los subscribers de GraphQL Subscription lo escuchan.
     */
    private final Sinks.Many<Estudiante> estudianteSink =
            Sinks.many().multicast().onBackpressureBuffer();

    private AlmacenDatos() {
        inicializarDatos();
    }

    public static AlmacenDatos getInstance() {
        return INSTANCIA;
    }

    // ─── Datos de ejemplo ────────────────────────────────────────────────────

    private void inicializarDatos() {
        Profesor p1 = new Profesor(contadorProfesor.getAndIncrement(), "Dr. García",   "Ingeniería de Software");
        Profesor p2 = new Profesor(contadorProfesor.getAndIncrement(), "Dra. Martínez","Ciencias de la Computación");
        profesores.addAll(Arrays.asList(p1, p2));

        Estudiante e1 = new Estudiante(contadorEstudiante.getAndIncrement(), "Juan Pérez",       "Ingeniería de Software");
        Estudiante e2 = new Estudiante(contadorEstudiante.getAndIncrement(), "María López",      "Ingeniería de Sistemas");
        Estudiante e3 = new Estudiante(contadorEstudiante.getAndIncrement(), "Carlos Rodríguez", "Ingeniería de Software");
        estudiantes.addAll(Arrays.asList(e1, e2, e3));

        GrupoClase g1 = new GrupoClase(contadorGrupo.getAndIncrement(), "Programación Web",  p1, new ArrayList<>(Arrays.asList(e1, e2)));
        GrupoClase g2 = new GrupoClase(contadorGrupo.getAndIncrement(), "Bases de Datos",    p2, new ArrayList<>(Arrays.asList(e2, e3)));
        grupos.addAll(Arrays.asList(g1, g2));
    }

    // ─── Estudiantes ─────────────────────────────────────────────────────────

    public List<Estudiante> getEstudiantes() {
        return estudiantes;
    }

    public Optional<Estudiante> getEstudiante(int id) {
        return estudiantes.stream().filter(e -> e.getId() == id).findFirst();
    }

    public Estudiante agregarEstudiante(String nombre, String carrera) {
        Estudiante e = new Estudiante(contadorEstudiante.getAndIncrement(), nombre, carrera);
        estudiantes.add(e);
        // Emitir evento para subscribers (suscripciones GraphQL)
        estudianteSink.tryEmitNext(e);
        return e;
    }

    public Optional<Estudiante> actualizarEstudiante(int id, String nombre, String carrera) {
        return getEstudiante(id).map(e -> {
            if (nombre  != null) e.setNombre(nombre);
            if (carrera != null) e.setCarrera(carrera);
            return e;
        });
    }

    public boolean eliminarEstudiante(int id) {
        return estudiantes.removeIf(e -> e.getId() == id);
    }

    // ─── Profesores ──────────────────────────────────────────────────────────

    public List<Profesor> getProfesores() {
        return profesores;
    }

    public Optional<Profesor> getProfesor(int id) {
        return profesores.stream().filter(p -> p.getId() == id).findFirst();
    }

    public Profesor agregarProfesor(String nombre, String carrera) {
        Profesor p = new Profesor(contadorProfesor.getAndIncrement(), nombre, carrera);
        profesores.add(p);
        return p;
    }

    public Optional<Profesor> actualizarProfesor(int id, String nombre, String carrera) {
        return getProfesor(id).map(p -> {
            if (nombre  != null) p.setNombre(nombre);
            if (carrera != null) p.setCarrera(carrera);
            return p;
        });
    }

    // ─── Grupos de clase ─────────────────────────────────────────────────────

    public List<GrupoClase> getGrupos() {
        return grupos;
    }

    public Optional<GrupoClase> getGrupo(int numeroClase) {
        return grupos.stream().filter(g -> g.getNumeroClase() == numeroClase).findFirst();
    }

    public Optional<GrupoClase> agregarGrupo(String nombre, int idProfesor) {
        return getProfesor(idProfesor).map(p -> {
            GrupoClase g = new GrupoClase(contadorGrupo.getAndIncrement(), nombre, p, new ArrayList<>());
            grupos.add(g);
            return g;
        });
    }

    public Optional<GrupoClase> inscribirEstudiante(int numeroClase, int idEstudiante) {
        Optional<GrupoClase>  grupo      = getGrupo(numeroClase);
        Optional<Estudiante>  estudiante = getEstudiante(idEstudiante);
        if (grupo.isPresent() && estudiante.isPresent()) {
            GrupoClase g = grupo.get();
            Estudiante e = estudiante.get();
            if (g.getEstudiantes().stream().noneMatch(x -> x.getId() == e.getId())) {
                g.getEstudiantes().add(e);
            }
            return Optional.of(g);
        }
        return Optional.empty();
    }

    // ─── Reactor Flux para subscriptions ─────────────────────────────────────

    /** Flux caliente: emite cada nuevo Estudiante creado via agregarEstudiante(). */
    public Flux<Estudiante> fluxNuevosEstudiantes() {
        return estudianteSink.asFlux();
    }
}
