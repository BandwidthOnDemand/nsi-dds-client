package net.es.nsi.dds.client;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBElement;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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
    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.getSocketFactory())
        .register("https", SSLConnectionSocketFactory.getSystemSocketFactory())
        .build();
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    return getClientConfig(connectionManager);
  }

  private ClientConfig getClientConfig(PoolingHttpClientConnectionManager connectionManager) {
    ClientConfig clientConfig = new ClientConfig();

    ConnectionConfig.Builder customConnection = ConnectionConfig.custom();
    customConnection.setConnectTimeout(Long.parseLong(System.getProperty(TCP_CONNECT_TIMEOUT,
        Integer.toString(CONNECT_TIMEOUT))), TimeUnit.MILLISECONDS);

    // We want to use the Apache connector for chunk POST support.
    clientConfig.connectorProvider(new ApacheConnectorProvider());
    connectionManager.setDefaultMaxPerRoute(20);
    connectionManager.setMaxTotal(80);
    connectionManager.closeIdle(TimeValue.ofSeconds(30));
    connectionManager.setDefaultConnectionConfig(customConnection.build());
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
    //custom.setRelativeRedirectsAllowed(true);
    custom.setRedirectsEnabled(true);
    custom.setResponseTimeout(Long.parseLong(System.getProperty(TCP_SO_TIMEOUT, Integer.toString(SO_TIMEOUT))),
        TimeUnit.MILLISECONDS);
    custom.setConnectionRequestTimeout(Long.parseLong(System.getProperty(TCP_CONNECT_REQUEST_TIMEOUT,
            Integer.toString(CONNECT_REQUEST_TIMEOUT))), TimeUnit.MILLISECONDS);
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
