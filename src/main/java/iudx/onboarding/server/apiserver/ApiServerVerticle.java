package iudx.onboarding.server.apiserver;

import static iudx.onboarding.server.apiserver.util.Constants.ALLOWED_HEADERS;
import static iudx.onboarding.server.apiserver.util.Constants.ALLOWED_METHODS;
import static iudx.onboarding.server.apiserver.util.Constants.APPLICATION_JSON;
import static iudx.onboarding.server.apiserver.util.Constants.CONTENT_TYPE;
import static iudx.onboarding.server.apiserver.util.Util.errorResponse;
import static iudx.onboarding.server.common.Constants.TOKEN_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import iudx.onboarding.server.common.Api;
import iudx.onboarding.server.common.HttpStatusCode;
import iudx.onboarding.server.token.TokenService;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Onboarding Server API Verticle.
 *
 * <h1>Onboarding Server API Verticle</h1>
 *
 * <p>The API Server verticle implements the IUDX Onboarding Server APIs. It handles the API
 * requests from the clients and interacts with the associated Service to respond.
 *
 * @see io.vertx.core.Vertx
 * @see AbstractVerticle
 * @see HttpServer
 * @see Router
 * @see io.vertx.servicediscovery.ServiceDiscovery
 * @see io.vertx.servicediscovery.types.EventBusService
 * @see io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
 * @version 1.0
 * @since 2020-05-31
 */
public class ApiServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(ApiServerVerticle.class);

  private HttpServer server;
  private Router router;
  private int port;
  private boolean isSSL;
  private String dxApiBasePath;
  private TokenService tokenService;

  /**
   * This method is used to start the Verticle. It deploys a verticle in a cluster, reads the
   * configuration, obtains a proxy for the Event bus services exposed through service discovery,
   * start an HTTPs server at port 8443 or an HTTP server at port 8080.
   *
   * @throws Exception which is a startup exception TODO Need to add documentation for all the
   */
  @Override
  public void start() throws Exception {
    /* Create a reference to HazelcastClusterManager. */

    router = Router.router(vertx);

    /* Get base paths from config */
    dxApiBasePath = config().getString("dxApiBasePath");
    Api api = Api.getInstance(dxApiBasePath);

    /* Define the APIs, methods, endpoints and associated methods. */

    router = Router.router(vertx);
    configureCorsHandler(router);

    putCommonResponseHeaders();

    // attach custom http error responses to router
    configureErrorHandlers(router);

    router.route().handler(BodyHandler.create());
    router.route().handler(TimeoutHandler.create(10000, 408));

    /* NGSI-LD api endpoints */

    router.post(api.getOnboardingUrl()).handler(this::handleOnboardingQuery);
    router.post(api.getIngestionUrl()).handler(this::handleIngestionQuery);
    router.post(api.getTokenUrl()).handler(this::handleTokenRequest);

    /* Read ssl configuration. */
    HttpServerOptions serverOptions = new HttpServerOptions();
    setServerOptions(serverOptions);
    serverOptions.setCompressionSupported(true).setCompressionLevel(5);
    server = vertx.createHttpServer(serverOptions);
    server.requestHandler(router).listen(port);

    tokenService = TokenService.createProxy(vertx, TOKEN_ADDRESS);
    /* Print the deployed endpoints */
    LOGGER.info("API server deployed on: " + port);
  }

  /**
   * Configures the CORS handler on the provided router.
   *
   * @param router The router instance to configure the CORS handler on.
   */
  private void configureCorsHandler(Router router) {
    router.route().handler(
      CorsHandler.create("*")
        .allowedHeaders(ALLOWED_HEADERS)
        .allowedMethods(ALLOWED_METHODS)
    );
  }

  /**
   * Configures error handlers for the specified status codes on the provided router.
   *
   * @param router The router instance to configure the error handlers on.
   */
  private void configureErrorHandlers(Router router) {
    HttpStatusCode[] statusCodes = HttpStatusCode.values();
    Stream.of(statusCodes).forEach(code -> {
      router.errorHandler(code.getValue(), errorHandler -> {
        HttpServerResponse response = errorHandler.response();
        if (response.headWritten()) {
          try {
            response.close();
          } catch (RuntimeException e) {
            LOGGER.error("Error: " + e);
          }
          return;
        }
        response.putHeader(CONTENT_TYPE, APPLICATION_JSON)
          .setStatusCode(code.getValue())
          .end(errorResponse(code));
      });
    });
  }

  /**
   * Sets common response headers to be included in HTTP responses.
   */
  private void putCommonResponseHeaders() {
    router.route().handler(requestHandler -> {
      requestHandler
        .response()
        .putHeader("Cache-Control", "no-cache, no-store,  must-revalidate,max-age=0")
        .putHeader("Pragma", "no-cache")
        .putHeader("Expires", "0")
        .putHeader("X-Content-Type-Options", "nosniff");
      requestHandler.next();
    });
  }

  private void setServerOptions(HttpServerOptions serverOptions) {
    isSSL = config().getBoolean("ssl");
    if (isSSL) {
      LOGGER.debug("Info: Starting HTTPs server");
      port = config().getInteger("httpPort") == null ? 8443 : config().getInteger("httpPort");
    } else {
      LOGGER.debug("Info: Starting HTTP server");
      serverOptions.setSsl(false);
      port = config().getInteger("httpPort") == null ? 8080 : config().getInteger("httpPort");
    }
  }
  private void handleTokenRequest(RoutingContext routingContext) {
    tokenService.createToken(routingContext.getBodyAsJson());
  }
  private void handleOnboardingQuery(RoutingContext routingContext) {}
  private void handleIngestionQuery(RoutingContext routingContext) {}

}
