package net.es.nsi.dds.client;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;

/**
 *
 * @author hacksaw
 */
@Provider
public class JsonMoxyConfigurationContextResolver implements ContextResolver<MoxyJsonConfig> {
        private final MoxyJsonConfig config;

        public JsonMoxyConfigurationContextResolver() {
            config = new MoxyJsonConfig();
            config.setNamespacePrefixMapper(Utilities.getNameSpace());
            config.setNamespaceSeparator('.');
            config.setAttributePrefix("@");
            config.setFormattedOutput(true);
            config.property("jersey.config.client.chunkedEncodingSize", 8192);
        }

        @Override
        public MoxyJsonConfig getContext(Class<?> objectType) {
            return config;
        }
}
