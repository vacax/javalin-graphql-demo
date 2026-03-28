package edu.pucmm.eict.jqd.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.reactivestreams.Publisher;

import java.util.Map;

/**
 * Servicio central de GraphQL.
 *
 * Responsabilidades:
 * <ol>
 *   <li>Generar el esquema GraphQL a partir de los resolvers anotados con
 *       graphql-spqr ({@code @GraphQLQuery}, {@code @GraphQLMutation},
 *       {@code @GraphQLSubscription}).</li>
 *   <li>Exponer métodos para ejecutar queries, mutations y subscriptions.</li>
 * </ol>
 *
 * graphql-spqr inspecciona las clases en tiempo de arranque, lee las anotaciones
 * y construye el objeto {@link GraphQLSchema} equivalente al que se definiría
 * manualmente en SDL ({@code schema.graphql}).
 */
public class GraphQLServicio {

    private final GraphQL graphQL;

    public GraphQLServicio() {
        // ── Generar esquema desde las clases anotadas ─────────────────────
        GraphQLSchema schema = new GraphQLSchemaGenerator()
                .withBasePackages("edu.pucmm.eict.jqd.entidades")
                .withOperationsFromSingletons(
                        new ConsultaGraphql(),    // @GraphQLQuery   → type Query
                        new MutacionGraphql(),    // @GraphQLMutation → type Mutation
                        new SuscripcionGraphql()  // @GraphQLSubscription → type Subscription
                )
                .generate();

        // ── Construir el ejecutor GraphQL ──────────────────────────────────
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    /**
     * Ejecuta una query o mutation HTTP.
     *
     * @param query          documento GraphQL (p.ej. {@code "query { obtenerEstudiantes { id } }"})
     * @param variables      variables del documento (puede ser null)
     * @param operationName  nombre de la operación (puede ser null)
     * @return resultado con datos y/o errores
     */
    public ExecutionResult ejecutar(String query, Map<String, Object> variables, String operationName) {
        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(query)
                .variables(variables != null ? variables : Map.of())
                .operationName(operationName)
                .build();
        return graphQL.execute(input);
    }

    /**
     * Inicia una suscripción GraphQL.
     *
     * graphql-java ejecuta el documento y retorna un {@link Publisher} de
     * {@link ExecutionResult}. Cada elemento emitido corresponde a un evento
     * de la suscripción que debe enviarse al cliente por WebSocket.
     *
     * @param query     documento GraphQL con operación subscription
     * @param variables variables del documento (puede ser null)
     * @return publisher de eventos; null si la operación no es una subscription
     */
    @SuppressWarnings("unchecked")
    public Publisher<ExecutionResult> suscribir(String query, Map<String, Object> variables) {
        ExecutionInput input = ExecutionInput.newExecutionInput()
                .query(query)
                .variables(variables != null ? variables : Map.of())
                .build();
        ExecutionResult result = graphQL.execute(input);
        return result.getData(); // graphql-java retorna Publisher<ExecutionResult> para subscriptions
    }
}
