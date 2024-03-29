/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import jakarta.ws.rs.core.MediaType;

/**
 *
 * @author hacksaw
 */
public class Utilities {
    public static Map<String, String> getNameSpace() {
        Map<String, String> namespacePrefixMapper = new HashMap<>(8);
        namespacePrefixMapper.put("http://schemas.ogf.org/nsi/2013/12/services/point2point", "p");
        namespacePrefixMapper.put("http://schemas.ogf.org/nsi/2013/12/services/definition", "s");
        namespacePrefixMapper.put("http://schemas.ogf.org/nsi/2013/12/services/types", "y");
        namespacePrefixMapper.put("http://schemas.ogf.org/nsi/2014/02/discovery/nsa", "n");
        namespacePrefixMapper.put("http://schemas.ogf.org/nsi/2014/02/discovery/types", "d");
        namespacePrefixMapper.put("http://schemas.ogf.org/nml/2013/05/base#", "b");
        namespacePrefixMapper.put("urn:ietf:params:xml:ns:vcard-4.0", "v");
        namespacePrefixMapper.put("http://nordu.net/namespaces/2013/12/gnsbod", "g");

        return namespacePrefixMapper;
    }

    public static boolean validMediaType(String mediaType) {
        HashSet<String> mediaTypes = new HashSet<String>() {
            private static final long serialVersionUID = 1L;
            {
                add(MediaType.APPLICATION_JSON);
                add(MediaType.APPLICATION_XML);
                add("application/vnd.net.es.dds.v1+json");
                add("application/vnd.net.es.dds.v1+xml");
            }
        };

        return mediaTypes.contains(mediaType);
    }
}
