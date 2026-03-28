package edu.pucmm.eict.jqd.graphql;

import edu.pucmm.eict.jqd.datos.AlmacenDatos;
import edu.pucmm.eict.jqd.entidades.Estudiante;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLSubscription;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Resolver de suscripciones (subscriptions) GraphQL.
 *
 * Los métodos retornan {@code Publisher<T>} (implementado por Reactor {@link Flux}).
 * graphql-java ejecuta cada emisión del Publisher como un evento de la suscripción
 * y lo envía al cliente por WebSocket usando el protocolo graphql-ws.
 *
 * ─── Cómo probar suscripciones ──────────────────────────────────────────────
 *
 * Herramienta recomendada: Altair GraphQL Client (https://altairgraphql.dev)
 *   → URL:  http://localhost:7000/graphql
 *   → Transport: WebSocket (graphql-ws)
 *
 * Ejemplo 1 — nuevosEstudiantes:
 * <pre>
 * subscription {
 *   nuevosEstudiantes {
 *     id nombre carrera
 *   }
 * }
 * </pre>
 * Después ejecutar la mutación agregarEstudiante para ver el evento en tiempo real.
 *
 * Ejemplo 2 — contadorEstudiantes:
 * <pre>
 * subscription {
 *   contadorEstudiantes(intervalSegundos: 3)
 * }
 * </pre>
 */
public class SuscripcionGraphql {

    private final AlmacenDatos datos = AlmacenDatos.getInstance();

    /**
     * Flux CALIENTE (hot publisher) — emite cada nuevo Estudiante creado
     * por la mutación {@code agregarEstudiante}.
     *
     * Patrón: Mutation → Sinks.Many → este Flux → cliente WebSocket.
     */
    @GraphQLSubscription(name = "nuevosEstudiantes",
                         description = "Emite un evento en tiempo real cada vez que se agrega un estudiante.")
    public Publisher<Estudiante> nuevosEstudiantes() {
        return datos.fluxNuevosEstudiantes();
    }

    /**
     * Flux FRÍO (cold publisher) — emite el total de estudiantes cada N segundos.
     *
     * Demuestra suscripciones con intervalo fijo. Cada cliente que se suscribe
     * recibe su propio Flux independiente (comportamiento "cold").
     */
    @GraphQLSubscription(name = "contadorEstudiantes",
                         description = "Emite periódicamente el total de estudiantes registrados.")
    public Publisher<Integer> contadorEstudiantes(
            @GraphQLArgument(name = "intervalSegundos",
                             description = "Intervalo de refresco en segundos",
                             defaultValue = "5") int intervalSegundos) {
        int intervalo = Math.max(1, intervalSegundos);
        return Flux.interval(Duration.ofSeconds(intervalo))
                   .map(tick -> datos.getEstudiantes().size());
    }
}
