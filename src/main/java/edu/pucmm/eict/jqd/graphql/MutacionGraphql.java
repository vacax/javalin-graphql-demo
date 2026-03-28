package edu.pucmm.eict.jqd.graphql;

import edu.pucmm.eict.jqd.datos.AlmacenDatos;
import edu.pucmm.eict.jqd.entidades.Estudiante;
import edu.pucmm.eict.jqd.entidades.GrupoClase;
import edu.pucmm.eict.jqd.entidades.Profesor;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;

import java.util.Optional;

/**
 * Resolver de mutaciones (mutations) GraphQL.
 *
 * Cada método anotado con {@code @GraphQLMutation} se expone como una operación
 * de mutación en el esquema GraphQL generado por graphql-spqr.
 *
 * Ejemplo de mutación:
 * <pre>
 * mutation {
 *   agregarEstudiante(nombre: "Ana Torres", carrera: "Ingeniería de Software") {
 *     id nombre carrera
 *   }
 * }
 * </pre>
 *
 * NOTA: agregarEstudiante también dispara los subscribers de la suscripción
 * {@code nuevosEstudiantes}, demostrando la comunicación entre mutations y subscriptions.
 */
public class MutacionGraphql {

    private final AlmacenDatos datos = AlmacenDatos.getInstance();

    // ─── Estudiantes ─────────────────────────────────────────────────────────

    @GraphQLMutation(name = "agregarEstudiante",
                     description = "Agrega un nuevo estudiante. Dispara la suscripción nuevosEstudiantes.")
    public Estudiante agregarEstudiante(
            @GraphQLArgument(name = "nombre",  description = "Nombre completo")        String nombre,
            @GraphQLArgument(name = "carrera", description = "Carrera que estudia")    String carrera) {
        return datos.agregarEstudiante(nombre, carrera);
    }

    @GraphQLMutation(name = "actualizarEstudiante",
                     description = "Actualiza los datos de un estudiante (pasar null para no modificar un campo)")
    public Optional<Estudiante> actualizarEstudiante(
            @GraphQLArgument(name = "id")      int    id,
            @GraphQLArgument(name = "nombre",  defaultValue = "null") String nombre,
            @GraphQLArgument(name = "carrera", defaultValue = "null") String carrera) {
        return datos.actualizarEstudiante(id, nombre, carrera);
    }

    @GraphQLMutation(name = "eliminarEstudiante",
                     description = "Elimina un estudiante del sistema. Retorna true si fue eliminado.")
    public boolean eliminarEstudiante(
            @GraphQLArgument(name = "id", description = "Identificador del estudiante a eliminar") int id) {
        return datos.eliminarEstudiante(id);
    }

    // ─── Profesores ──────────────────────────────────────────────────────────

    @GraphQLMutation(name = "agregarProfesor",
                     description = "Agrega un nuevo profesor al sistema")
    public Profesor agregarProfesor(
            @GraphQLArgument(name = "nombre",  description = "Nombre completo") String nombre,
            @GraphQLArgument(name = "carrera", description = "Departamento o área") String carrera) {
        return datos.agregarProfesor(nombre, carrera);
    }

    @GraphQLMutation(name = "actualizarProfesor",
                     description = "Actualiza los datos de un profesor existente")
    public Optional<Profesor> actualizarProfesor(
            @GraphQLArgument(name = "id")      int    id,
            @GraphQLArgument(name = "nombre",  defaultValue = "null") String nombre,
            @GraphQLArgument(name = "carrera", defaultValue = "null") String carrera) {
        return datos.actualizarProfesor(id, nombre, carrera);
    }

    // ─── Grupos de Clase ─────────────────────────────────────────────────────

    @GraphQLMutation(name = "agregarGrupo",
                     description = "Crea un nuevo grupo de clase asignando un profesor responsable")
    public Optional<GrupoClase> agregarGrupo(
            @GraphQLArgument(name = "nombre",     description = "Nombre del grupo") String nombre,
            @GraphQLArgument(name = "idProfesor", description = "ID del profesor asignado") int idProfesor) {
        return datos.agregarGrupo(nombre, idProfesor);
    }

    @GraphQLMutation(name = "inscribirEstudiante",
                     description = "Inscribe un estudiante en un grupo de clase")
    public Optional<GrupoClase> inscribirEstudiante(
            @GraphQLArgument(name = "numeroClase",  description = "Número del grupo") int numeroClase,
            @GraphQLArgument(name = "idEstudiante", description = "ID del estudiante") int idEstudiante) {
        return datos.inscribirEstudiante(numeroClase, idEstudiante);
    }
}
