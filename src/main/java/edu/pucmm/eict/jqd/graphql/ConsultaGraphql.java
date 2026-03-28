package edu.pucmm.eict.jqd.graphql;

import edu.pucmm.eict.jqd.datos.AlmacenDatos;
import edu.pucmm.eict.jqd.entidades.Estudiante;
import edu.pucmm.eict.jqd.entidades.GrupoClase;
import edu.pucmm.eict.jqd.entidades.Profesor;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;

import java.util.List;
import java.util.Optional;

/**
 * Resolver de consultas (queries) GraphQL.
 *
 * Cada método anotado con {@code @GraphQLQuery} se expone como una operación
 * de consulta en el esquema GraphQL generado por graphql-spqr.
 *
 * Ejemplo de consulta:
 * <pre>
 * query {
 *   obtenerEstudiantes {
 *     id nombre carrera
 *   }
 * }
 * </pre>
 */
public class ConsultaGraphql {

    private final AlmacenDatos datos = AlmacenDatos.getInstance();

    // ─── Estudiantes ─────────────────────────────────────────────────────────

    @GraphQLQuery(name = "obtenerEstudiantes",
                  description = "Retorna la lista completa de estudiantes registrados")
    public List<Estudiante> obtenerEstudiantes() {
        return datos.getEstudiantes();
    }

    @GraphQLQuery(name = "obtenerEstudiante",
                  description = "Busca un estudiante por su identificador único")
    public Optional<Estudiante> obtenerEstudiante(
            @GraphQLArgument(name = "id", description = "Identificador del estudiante") int id) {
        return datos.getEstudiante(id);
    }

    // ─── Profesores ──────────────────────────────────────────────────────────

    @GraphQLQuery(name = "obtenerProfesores",
                  description = "Retorna la lista completa de profesores registrados")
    public List<Profesor> obtenerProfesores() {
        return datos.getProfesores();
    }

    @GraphQLQuery(name = "obtenerProfesor",
                  description = "Busca un profesor por su identificador único")
    public Optional<Profesor> obtenerProfesor(
            @GraphQLArgument(name = "id", description = "Identificador del profesor") int id) {
        return datos.getProfesor(id);
    }

    // ─── Grupos de Clase ─────────────────────────────────────────────────────

    @GraphQLQuery(name = "obtenerGrupos",
                  description = "Retorna la lista completa de grupos de clase")
    public List<GrupoClase> obtenerGrupos() {
        return datos.getGrupos();
    }

    @GraphQLQuery(name = "obtenerGrupo",
                  description = "Busca un grupo de clase por su número")
    public Optional<GrupoClase> obtenerGrupo(
            @GraphQLArgument(name = "numeroClase", description = "Número del grupo de clase") int numeroClase) {
        return datos.getGrupo(numeroClase);
    }

    @GraphQLQuery(name = "estudiantesDeGrupo",
                  description = "Retorna los estudiantes inscritos en un grupo específico")
    public List<Estudiante> estudiantesDeGrupo(
            @GraphQLArgument(name = "numeroClase", description = "Número del grupo de clase") int numeroClase) {
        return datos.getGrupo(numeroClase)
                    .map(GrupoClase::getEstudiantes)
                    .orElse(List.of());
    }
}
