package net.es.maddash.www;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpPacket;
import org.glassfish.grizzly.http.HttpContent;

import static org.glassfish.grizzly.ssl.SSLUtils.*;

/**
 * Extracts SSL peers certificate information and adds it to the HTTP header
 * 
 * See http://httpd.apache.org/docs/2.2/mod/mod_ssl.html for header info
 * 
 * @author Andy Lake <andy@es.net>
 *
 */
public class MaDDashClientAuthProxyFilter extends BaseFilter{
    Logger log = Logger.getLogger(MaDDashClientAuthProxyFilter.class);
    
    protected boolean proxyMode;
    
    //Subject DN
    static public final String SSL_CLIENT_S_DN = "SSL_CLIENT_S_DN";
    //Issuer DN
    static public final String SSL_CLIENT_I_DN = "SSL_CLIENT_I_DN";
    //Indicate the certificate was already checked (NONE, SUCCESS, GENEROUS or FAILED)
    static public final String SSL_CLIENT_VERIFY = "SSL_CLIENT_VERIFY";
    //Field where proxy indicates IP of original client
    static public final String X_FORWARDED_FOR = "X-Forwarded-For";
    
    public MaDDashClientAuthProxyFilter(boolean proxyMode){
        this.proxyMode = proxyMode;
    }
    
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        if(HttpPacket.isHttp(ctx.getMessage())){
            HttpContent httpContent = (HttpContent) ctx.getMessage();
            
            this.log.debug("PEER ADDRESS=" + ctx.getConnection().getPeerAddress());
            this.log.debug("HTTP HEADERS:");
            this.log.debug(httpContent.getHttpHeader());

            /**
             * Clear out SSL headers to prevent identity spoofing. If proxy mode is on
             * then a proxy MUST be the only way to access this machine otherwise someone
             * can claim auth by modifying headers
             */
            if(this.proxyMode){
                return ctx.getInvokeAction();
            }
            if(httpContent.getHttpHeader().containsHeader(SSL_CLIENT_S_DN)){
                httpContent.getHttpHeader().setHeader(SSL_CLIENT_S_DN, null);
            }
            if(httpContent.getHttpHeader().containsHeader(SSL_CLIENT_I_DN)){
                httpContent.getHttpHeader().setHeader(SSL_CLIENT_I_DN, null);
            }
            if(httpContent.getHttpHeader().containsHeader(SSL_CLIENT_VERIFY)){
                httpContent.getHttpHeader().setHeader(SSL_CLIENT_VERIFY, null);
            }
            if(httpContent.getHttpHeader().containsHeader(X_FORWARDED_FOR)){
                httpContent.getHttpHeader().setHeader(X_FORWARDED_FOR, null);
            }
            
            /**
             * Set client IP
             */
             this.extractIPAddress((InetSocketAddress) ctx.getConnection().getPeerAddress(), 
                     httpContent.getHttpHeader());
             
            /**
             * Check for SSL headers
             */
            SSLEngine sslEngine = getSSLEngine(ctx.getConnection());
            if(sslEngine == null){
                return ctx.getInvokeAction();
            }
            
            //Extract certificates
            Certificate[] certs = this.getPeerCertificates(getSSLEngine(ctx.getConnection()));
            X509Certificate[] x509Certs = this.extractX509Certs(certs);
            if(x509Certs == null || x509Certs.length == 0 || 
                    x509Certs[0].getSubjectDN() == null){
                return ctx.getInvokeAction();
            }
            
            //set headers. first cert in chain is always peer cert
            httpContent.getHttpHeader().addHeader(SSL_CLIENT_S_DN, x509Certs[0].getSubjectDN().getName());
            if(x509Certs[0].getIssuerDN() != null){
                httpContent.getHttpHeader().addHeader(SSL_CLIENT_I_DN, x509Certs[0].getIssuerDN().getName());
            }
            httpContent.getHttpHeader().addHeader(SSL_CLIENT_VERIFY, "SUCCESS");
        }
        return ctx.getInvokeAction();
    }

    private X509Certificate[] extractX509Certs(final Certificate[] certs) {
        if(certs == null){
            return null;
        }
        final X509Certificate[] x509Certs = new X509Certificate[certs.length];
        for(int i = 0, len = certs.length; i < len; i++) {
            if( certs[i] instanceof X509Certificate ) {
                x509Certs[i] = (X509Certificate)certs[i];
            } else {
                try {
                    final byte [] buffer = certs[i].getEncoded();
                    final CertificateFactory cf =
                        CertificateFactory.getInstance("X.509");
                    ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
                    x509Certs[i] = (X509Certificate)
                    cf.generateCertificate(stream);
                } catch(Exception ex) {
                    return null;
                }
            }
        }
        return x509Certs;
    }

    private Certificate[] getPeerCertificates(final SSLEngine sslEngine) {
        try {
            return sslEngine.getSession().getPeerCertificates();
        } catch( Throwable t ) {
            return null;
        }
    }
    
    private void extractIPAddress(InetSocketAddress peerAddress, HttpHeader httpHeader){
        if(peerAddress == null || peerAddress.getAddress() == null){
            return;
        }
        httpHeader.setHeader(X_FORWARDED_FOR, peerAddress.getAddress().getHostAddress());
    }
}
