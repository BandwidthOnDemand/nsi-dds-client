package net.es.nsi.dds.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;

/**
 *
 * @author hacksaw
 */
public class RestClient {
  private final Client client;
  private final boolean debug;

  // Time for idle data timeout.
  private static final String TCP_SO_TIMEOUT = "tcpSoTimeout";
  private static final int SO_TIMEOUT = 60 * 1000;

  // Time for the socket to connect.
  private static final String TCP_CONNECT_TIMEOUT = "tcpConnectTimeout";
  private static final int CONNECT_TIMEOUT = 20 * 1000;

  // Time to block for a socket from the connection manager.
  private static final String TCP_CONNECT_REQUEST_TIMEOUT = "tcpConnectRequestTimeout";
  private static final int CONNECT_REQUEST_TIMEOUT = 30 * 1000;

  public RestClient(boolean debug) {
    this.debug = debug;
    ClientConfig clientConfig = configureClient();
    client = ClientBuilder.newBuilder().withConfig(clientConfig).build();
    client.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, Level.FINEST.getName());
  }

  private ClientConfig configureClient() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    return getClientConfig(connectionManager);
  }

  private ClientConfig getClientConfig(PoolingHttpClientConnectionManager connectionManager) {
    ClientConfig clientConfig = new ClientConfig();

    // We want to use the Apache connector for chunk POST support.
    clientConfig.connectorProvider(new ApacheConnectorProvider());
    connectionManager.setDefaultMaxPerRoute(20);
    connectionManager.setMaxTotal(80);
    connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
    clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);

    clientConfig.register(GZipEncoder.class);
    clientConfig.register(new MoxyXmlFeature());
    clientConfig.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED);

    clientConfig.property(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, Utilities.getNameSpace());
    clientConfig.property(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
    clientConfig.property(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, '.');

    if (debug) {
      LoggingFeature lf = new LoggingFeature(java.util.logging.Logger.getGlobal(),
              LoggingFeature.Verbosity.PAYLOAD_TEXT);
      clientConfig.register(lf);
    }

    // Apache specific configuration.
    RequestConfig.Builder custom = RequestConfig.custom();
    custom.setExpectContinueEnabled(true);
    custom.setRelativeRedirectsAllowed(true);
    custom.setRedirectsEnabled(true);
    custom.setSocketTimeout(Integer.parseInt(System.getProperty(TCP_SO_TIMEOUT, Integer.toString(SO_TIMEOUT))));
    custom.setConnectTimeout(Integer.parseInt(System.getProperty(TCP_CONNECT_TIMEOUT,
            Integer.toString(CONNECT_TIMEOUT))));
    custom.setConnectionRequestTimeout(Integer.parseInt(System.getProperty(TCP_CONNECT_REQUEST_TIMEOUT,
            Integer.toString(CONNECT_REQUEST_TIMEOUT))));
    clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, custom.build());

    return clientConfig;
  }

  public Client get() {
    return client;
  }

  public void close() {
    client.close();
  }

  private static class FollowRedirectFilter implements ClientResponseFilter {
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
      if (requestContext == null || responseContext == null || responseContext.getStatus() != Response.Status.FOUND.getStatusCode()) {
        return;
      }

      Client inClient = requestContext.getClient();
      Object entity = requestContext.getEntity();
      MultivaluedMap<String, Object> headers = requestContext.getHeaders();
      String method = requestContext.getMethod();
      Response resp;
      if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
        resp = inClient.target(responseContext.getLocation())
                .request(requestContext.getMediaType())
                .headers(headers)
                .method(requestContext.getMethod(), Entity.entity(new GenericEntity<JAXBElement<?>>((JAXBElement<?>) entity) {
                }, NsiConstants.NSI_DDS_V1_XML));
      } else {
        resp = inClient.target(responseContext.getLocation())
                .request(requestContext.getMediaType())
                .headers(headers)
                .method(requestContext.getMethod());
      }

      responseContext.setEntityStream((InputStream) resp.getEntity());
      responseContext.setStatusInfo(resp.getStatusInfo());
      responseContext.setStatus(resp.getStatus());
      responseContext.getHeaders().putAll(resp.getStringHeaders());
    }
  }
}
