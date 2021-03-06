package org.kie.kogito.app;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static io.vertx.core.http.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;

@ApplicationScoped
public class VertxRouter {

    @Inject
    @ConfigProperty(name = "kogito.dataindex.http.url", defaultValue = "http://localhost:8180")
    String dataIndexHttpURL;

    @Inject
    @ConfigProperty(name = "kogito.dataindex.ws.url", defaultValue = "ws://localhost:8180")
    String dataIndexWsURL;

    @Inject
    Vertx vertx;

    private Buffer resource;

    @PostConstruct
    public void init() {
        try {
            resource = Buffer.buffer(vertx.fileSystem()
                                             .readFileBlocking("META-INF/resources/index.html")
                                             .toString(UTF_8)
                                             .replace("__GRAPHQL_HTTP_ENDPOINT__", "\"" + dataIndexHttpURL + "/graphql\"")
                                             .replace("__GRAPHQL_WS_ENDPOINT__", "\"" + dataIndexWsURL + "/graphql\""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void setupRouter(@Observes Router router) {
        router.route().handler(LoggerHandler.create());
        router.route().handler(FaviconHandler.create());
        router.route().handler(StaticHandler.create());
        router.route(GET, "/").handler(ctx -> ctx.response().end(resource));
    }
}
