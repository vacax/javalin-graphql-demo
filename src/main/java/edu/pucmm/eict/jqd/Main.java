package edu.pucmm.eict.jqd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pucmm.eict.jqd.graphql.GraphQLServicio;
import graphql.ExecutionResult;
import io.javalin.Javalin;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Punto de entrada del demo Javalin + GraphQL.
 *
 * ─── Arquitectura de endpoints ────────────────────────────────────────────
 *
 *  POST http://localhost:7000/graphql   → queries y mutations (HTTP/JSON)
 *  WS   ws://localhost:7000/graphql     → subscriptions (protocolo graphql-ws)
 *
 * ─── Queries de ejemplo ───────────────────────────────────────────────────
 *
 *  query {
 *    obtenerEstudiantes { id nombre carrera }
 *  }
 *
 *  query {
 *    obtenerGrupos {
 *      numeroClase nombre
 *      profesor { id nombre }
 *      estudiantes { id nombre }
 *    }
 *  }
 *
 * ─── Mutations de ejemplo ─────────────────────────────────────────────────
 *
 *  mutation {
 *    agregarEstudiante(nombre: "Ana Torres", carrera: "Ingeniería de Software") {
 *      id nombre carrera
 *    }
 *  }
 *
 *  mutation {
 *    inscribirEstudiante(numeroClase: 1, idEstudiante: 3) {
 *      numeroClase nombre
 *      estudiantes { id nombre }
 *    }
 *  }
 *
 * ─── Subscriptions de ejemplo (Altair / Apollo Studio / wscat) ────────────
 *
 *  subscription {
 *    nuevosEstudiantes { id nombre carrera }
 *  }
 *
 *  subscription {
 *    contadorEstudiantes(intervalSegundos: 3)
 *  }
 */
public class Main {

    public static void main(String[] args) {

        // ── Instanciar el servicio GraphQL (genera el esquema al arrancar) ─
        GraphQLServicio graphQL = new GraphQLServicio();
        ObjectMapper mapper     = new ObjectMapper();

        Javalin.create(config -> {

            // ── CORS abierto para facilitar pruebas desde herramientas externas
            config.bundledPlugins.enableCors(cors -> cors.addRule(r -> r.anyHost()));

            // ── Log de desarrollo (muestra cada request en la consola)
            config.bundledPlugins.enableDevLogging();

            // ═══════════════════════════════════════════════════════════════
            //  ENDPOINT GET — GraphiQL Playground
            //  Abrir http://localhost:7000/graphiql en el navegador
            // ═══════════════════════════════════════════════════════════════
            config.routes.get("/graphiql", ctx -> {
                try (var in = Main.class.getResourceAsStream("/graphiql.html")) {
                    if (in == null) { ctx.status(404).result("graphiql.html no encontrado"); return; }
                    ctx.html(new String(in.readAllBytes()));
                }
            });

            // ═══════════════════════════════════════════════════════════════
            //  ENDPOINT HTTP — Queries y Mutations
            //  Protocolo: JSON-over-HTTP (estándar GraphQL)
            //  Herramientas: curl, Postman, Bruno, fetch(), etc.
            // ═══════════════════════════════════════════════════════════════
            config.routes.post("/graphql", ctx -> {
                // Parsear el cuerpo del request
                Map<String, Object> body = mapper.readValue(
                        ctx.body(), new TypeReference<>() {});

                String query          = (String) body.get("query");
                @SuppressWarnings("unchecked")
                Map<String, Object> variables = (Map<String, Object>) body.get("variables");
                String operationName  = (String) body.get("operationName");

                // Ejecutar en GraphQL
                ExecutionResult resultado = graphQL.ejecutar(query, variables, operationName);

                // Responder con el formato estándar: { "data": {...}, "errors": [...] }
                ctx.json(resultado.toSpecification());
            });

            // ═══════════════════════════════════════════════════════════════
            //  ENDPOINT WebSocket — Subscriptions
            //  Protocolo: graphql-ws  (https://github.com/enisdenjo/graphql-ws)
            //  Herramientas: Altair, Apollo Studio, wscat
            //
            //  Flujo de mensajes:
            //    Cliente → { "type": "connection_init" }
            //    Servidor ← { "type": "connection_ack" }
            //    Cliente → { "type": "subscribe", "id": "1",
            //                "payload": { "query": "subscription { ... }" } }
            //    Servidor ← { "type": "next", "id": "1",
            //                 "payload": { "data": {...} } }  (repetido por cada evento)
            //    Cliente → { "type": "complete", "id": "1" }
            // ═══════════════════════════════════════════════════════════════
            config.routes.ws("/graphql", ws -> {

                    // Cada sesión WebSocket tiene su propio mapa de suscripciones activas.
                    // Se usa sessionAttribute() para aislar el estado por conexión.
                    // Clave del atributo: "subs"
                    ws.onConnect(wsCtx -> {
                        // Inicializar el mapa de suscripciones para esta sesión
                        wsCtx.attribute("subs", new ConcurrentHashMap<String, Disposable>());
                    });

                    ws.onMessage(wsCtx -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> mensaje = mapper.readValue(
                                wsCtx.message(), new TypeReference<>() {});

                        String tipo = (String) mensaje.get("type");

                        @SuppressWarnings("unchecked")
                        Map<String, Disposable> suscripcionesActivas =
                                wsCtx.attribute("subs");

                        switch (tipo) {

                            // ── Handshake inicial ──────────────────────────────
                            case "connection_init" ->
                                wsCtx.send("{\"type\":\"connection_ack\"}");

                            // ── Nueva suscripción ──────────────────────────────
                            case "subscribe" -> {
                                String id = (String) mensaje.get("id");

                                @SuppressWarnings("unchecked")
                                Map<String, Object> payload = (Map<String, Object>) mensaje.get("payload");
                                String query = (String) payload.get("query");
                                @SuppressWarnings("unchecked")
                                Map<String, Object> variables = (Map<String, Object>) payload.get("variables");

                                // Obtener el Publisher<ExecutionResult> desde graphql-java
                                Publisher<ExecutionResult> publisher = graphQL.suscribir(query, variables);

                                if (publisher == null) {
                                    wsCtx.send(mapper.writeValueAsString(Map.of(
                                            "type", "error", "id", id,
                                            "payload", "La operación no es una suscripción")));
                                    return;
                                }

                                // Suscribirse al Publisher y reenviar cada evento al cliente
                                Disposable disposable = Flux.from(publisher)
                                        .subscribe(
                                                resultado -> {
                                                    try {
                                                        String respuesta = mapper.writeValueAsString(Map.of(
                                                                "type", "next",
                                                                "id",   id,
                                                                "payload", resultado.toSpecification()));
                                                        wsCtx.send(respuesta);
                                                    } catch (Exception e) {
                                                        // Conexión cerrada; el error se ignora
                                                    }
                                                },
                                                error -> {
                                                    try {
                                                        wsCtx.send(mapper.writeValueAsString(Map.of(
                                                                "type", "error", "id", id,
                                                                "payload", error.getMessage())));
                                                    } catch (Exception ignored) {}
                                                },
                                                () -> {
                                                    try {
                                                        wsCtx.send(mapper.writeValueAsString(
                                                                Map.of("type", "complete", "id", id)));
                                                    } catch (Exception ignored) {}
                                                }
                                        );

                                suscripcionesActivas.put(id, disposable);
                            }

                            // ── Cancelar suscripción ───────────────────────────
                            case "complete" -> {
                                String id = (String) mensaje.get("id");
                                Disposable d = suscripcionesActivas.remove(id);
                                if (d != null) d.dispose();
                            }

                            // ── Keep-alive ─────────────────────────────────────
                            case "ping" ->
                                wsCtx.send("{\"type\":\"pong\"}");

                            default -> { /* tipo desconocido, ignorar */ }
                        }
                    });

                    ws.onClose(wsCtx -> {
                        // Liberar todas las suscripciones activas de esta sesión
                        Map<String, Disposable> subs = wsCtx.attribute("subs");
                        if (subs != null) {
                            subs.values().forEach(Disposable::dispose);
                            subs.clear();
                        }
                    });

                    ws.onError(wsCtx -> {
                        Map<String, Disposable> subs = wsCtx.attribute("subs");
                        if (subs != null) {
                            subs.values().forEach(Disposable::dispose);
                            subs.clear();
                        }
                    });
            });

        }).start(7000);

        System.out.println("""
            ╔══════════════════════════════════════════════════════════╗
            ║       Javalin + GraphQL Demo — PUCMM / EICT              ║
            ╠══════════════════════════════════════════════════════════╣
            ║  GraphiQL Playground →  GET  http://localhost:7000/graphiql ║
            ║  Queries/Mutations   →  POST http://localhost:7000/graphql  ║
            ║  Subscriptions       →  WS   ws://localhost:7000/graphql    ║
            ╚══════════════════════════════════════════════════════════╝
            """);
    }
}
