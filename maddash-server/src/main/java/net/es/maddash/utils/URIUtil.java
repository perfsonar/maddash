package net.es.maddash.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class URIUtil {
    /**
     * Encode given string for use in a URI
     * 
     * @param uriPart the string to encode
     * @return the encoded string
     */
    static public String normalizeURIPart(String uriPart) {
        try {
            uriPart = URLEncoder.encode(uriPart, "UTF-8");
        } catch (UnsupportedEncodingException e) {}
        return uriPart;
    }
    
    /**
     * Decodes given string that was extracted from a URI
     * 
     * @param uriPart the string to decode
     * @return the decoded string
     */
    static public String decodeUriPart(String uriPart) {
        try {
            uriPart = URLDecoder.decode(uriPart, "UTF-8");
        } catch (UnsupportedEncodingException e) {}
        return uriPart;
    }
}
