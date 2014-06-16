package net.es.maddash.www;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;

public class WebServer {
    Logger log = Logger.getLogger(WebServer.class);
    HttpServer webServer;
    String hostname;
    
    final static public String HTTPS_CLIENT_AUTH_REQUIRE = "require";
    final static public String HTTPS_CLIENT_AUTH_WANT = "want";
    final static public String HTTPS_CLIENT_AUTH_OFF = "off";
    
    public WebServer(String hostname, ResourceConfig rc){
        this.hostname = hostname;
        this.webServer = new HttpServer();
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, rc);
        this.webServer.getServerConfiguration().addHttpHandler(handler, "/");
    }
    
    public void addHttpListener(int port, boolean proxyMode) throws IOException{
        NetworkListener httpNl = new NetworkListener("http-"+port, this.hostname, port);
        this.webServer.addListener(httpNl);
        /*
         * Start the server to populate the filter chain. 
         * start() checks if its already running so this won't start 
         * it twice if called multiple times
         */
        this.webServer.start();
        
        /* Finally add custom filter that will clear out attempts to 
         * clame SSL auth in the HTTP headers
         */
        httpNl.getFilterChain().add( httpNl.getFilterChain().size() - 1, 
                new MaDDashClientAuthProxyFilter(proxyMode));
    }
    
    public void addHttpsListener(int port, String keystore, 
            String keystorePassword, String clientAuth, boolean proxyMode) throws IOException{
        NetworkListener httpsNl = new NetworkListener("https-"+port, this.hostname, port);
        httpsNl.setSecure(true);
        SSLContextConfigurator sslContext = new SSLContextConfigurator();
        sslContext.setKeyStoreFile(keystore);
        sslContext.setKeyStorePass(keystorePassword);
        sslContext.setTrustStoreFile(keystore);
        sslContext.setTrustStorePass(keystorePassword);
        boolean wantClientAuth = false;
        boolean requireClientAuth = false;
        if(HTTPS_CLIENT_AUTH_WANT.equals(clientAuth.toLowerCase())){
            wantClientAuth = true;
        }else if(HTTPS_CLIENT_AUTH_REQUIRE.equals(clientAuth.toLowerCase())){
            wantClientAuth = true;
            requireClientAuth = true;
        }else if(!HTTPS_CLIENT_AUTH_OFF.equals(clientAuth.toLowerCase())){
            throw new RuntimeException("Unrecognized clientAuth " + clientAuth);
        }
        
        /* Note: clientMode (2nd param) means server does not 
         * authenticate to client - which we never want
         */
        SSLEngineConfigurator sslEngineConfig = new SSLEngineConfigurator(sslContext, 
                false, requireClientAuth, wantClientAuth);
        httpsNl.setSSLEngineConfig(sslEngineConfig);
        this.webServer.addListener(httpsNl);
        
        /*
         * Start the server to populate the filter chain. 
         * start() checks if its already running so this won't start 
         * it twice if called multiple times
         */
        this.webServer.start();
        
        /* Finally add custom filter that will copy client cert info to 
         * HTTP headers similar to how mod_ssl passes certificate DN through a proxy.
         * The certificate itself is verified by the default SSLFilter created when
         * we start the server. We have to do this after we start otherwise we manually 
         * have to rebuild the filter chain which is near impossible given that many of 
         * the Filters use values private to the HttpServer class
         */
         httpsNl.getFilterChain().add( httpsNl.getFilterChain().size() - 1, 
                new MaDDashClientAuthProxyFilter(proxyMode));
        
        //debugging output to show the final filter chain
        for(Filter filter: httpsNl.getFilterChain()){
                this.log.debug("Filter: " + filter + "");
        }
    }

    public void start() throws IOException {
        this.webServer.start();
    }
    
    public void stop(){
        this.webServer.stop();
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return this.hostname;
    }
    
}
