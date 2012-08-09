package net.es.maddash;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import net.es.maddash.jobs.CheckSchedulerJob;
import net.es.maddash.jobs.CleanDBJob;
import net.es.maddash.utils.URIUtil;
import net.es.maddash.www.WebServer;
import net.es.maddash.www.rest.CellResource;
import net.es.maddash.www.rest.CheckResource;
import net.es.maddash.www.rest.DashboardsResource;
import net.es.maddash.www.rest.GridResource;
import net.es.maddash.www.rest.GridsResource;
import net.es.maddash.www.rest.RowResource;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * Singleton with global parameters. Initializes scheduler, database and various other settings 
 * 
 * @author Andy Lake <andy@es.net>
 *
 */
public class MaDDashGlobals {
    static private Logger log = Logger.getLogger(MaDDashGlobals.class);
    static private MaDDashGlobals instance = null;
    static private String configFile = null;

    private ComboPooledDataSource dataSource = null;
    private Scheduler scheduler;
    private ResourceManager resourceManager;
    private int jobBatchSize;
    private int threadPoolSize;
    private long dbDataMaxAge;
    private Map<String, Class> checkTypeClassMap;
    private HashMap<Integer, Boolean> scheduledChecks; 
    private CheckSchedulerJob checkShedJob;
    private WebServer webServer;
    private String resourceURL;

    //web related parameters
    private String webTitle;
    private JSONArray dashboards;
    private String defaultDashboard;

    //properties
    final private static String PROP_SERVER_HOST = "serverHost";
    final private static String PROP_HTTP = "http";
    final private static String PROP_HTTP_PORT = "port";
    final private static String PROP_HTTP_PROXY_MODE = "proxyMode";
    final private static String PROP_HTTPS = "https";
    final private static String PROP_HTTPS_PORT = "port";
    final private static String PROP_HTTPS_KEYSTORE = "keystore";
    final private static String PROP_HTTPS_KEYSTORE_PASSWORD = "keystorePassword";
    final private static String PROP_HTTPS_CLIENT_AUTH = "clientAuth";
    final private static String PROP_HTTPS_PROXY_MODE = "proxyMode";
    final private static String PROP_RESOURCE_URL = "resourceURL";
    final private static String PROP_RESOURCE_URL_PROTO = "protocol";
    final private static String PROP_RESOURCE_URL_HOST = "host";
    final private static String PROP_RESOURCE_URL_PORT = "port";
    final private String PROP_DATABASE = "database";
    final private String PROP_JOB_BATCH_SIZE = "jobBatchSize";
    final private String PROP_SKIP_TABLE_BUILD = "skipTableBuild";
    final private String PROP_JOB_THREAD_POOL_SIZE = "jobThreadPoolSize";
    final private String PROP_DISABLE_SCHEDULER = "disableScheduler";
    final private String PROP_DISABLE_CHECKS = "disableChecks";
    final private String PROP_DB_CLEAN_SCHED = "dbCleanSchedule";
    final private String PROP_DB_DATA_MAX_AGE = "dbDataMaxAge";
   // final private String PROP_WEB = "web";
   // final private String PROP_WEB_TITLE = "title";
    //final private String PROP_WEB_DEFAULT = "default";
    final private String PROP_DASHBOARDS = "dashboards";
    final private String PROP_DASHBOARDS_NAME = "name";
    final private String PROP_DASHBOARDS_GRIDS = "grids";
    final private String PROP_DASHBOARDS_GRIDS_NAME = "name";

    //final static private String JDBC_URL = "jdbc:sqlite:";
    final static private String JDBC_URL = "jdbc:derby:maddash;create=true";
    //final static private String JDBC_DRIVER = "org.sqlite.JDBC";
    final static private String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    final static private String DEFAULT_HOST = "localhost";
    final static private String DEFAULT_DB = "data/dashboard.db";
    final static private long DEFAULT_DB_DATA_MAX_AGE = 604800;//1 week
    final static private int DEFAULT_JOB_BATCH_SIZE = 250;
    final static private int DEFAULT_THREAD_POOL_SIZE = 50;
    final static private int C3P0_IDLE_TEST_PERIOD = 600;
    final static private String C3P0_TEST_QUERY = "SELECT id FROM checkTemplates";
    final static private boolean DEFAULT_DISABLE_SCHEDULER = false;
    final static private boolean DEFAULT_DISABLE_CHECKS = false;
    final static private boolean DEFAULT_PROXY_MODE = false;
    final static private String CLEAN_DB_SCHEDULE = "0 0 0/12 * * ?";//every 12 hours

    /**
     * Sets the configuration file to use
     * 
     * @param newConfigFile the configuration file to use on initialization
     */
    public static void init(String newConfigFile){
        configFile = newConfigFile;
    }

    /**
     * Loads configuration file, database and sets global variables.
     */
    private MaDDashGlobals() {
        //check config file
        if(configFile == null){
            throw new RuntimeException("No config file set.");
        }
        Map config = null;
        try {
            config = (Map) Yaml.load(new File(configFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
        
        //create resource manager
        this.resourceManager = new ResourceManager();
        
        //set server host
        String serverHost = DEFAULT_HOST;
        if(config.containsKey(PROP_SERVER_HOST) && config.get(PROP_SERVER_HOST) != null){
            serverHost = (String) config.get(PROP_SERVER_HOST);
        }
        log.debug("Server host is " + serverHost);

        this.jobBatchSize = DEFAULT_JOB_BATCH_SIZE;
        if(config.containsKey(PROP_JOB_BATCH_SIZE) && config.get(PROP_JOB_BATCH_SIZE) != null){
            this.jobBatchSize = (Integer) config.get(PROP_JOB_BATCH_SIZE);
        }
        log.debug("jobBatchSize is " + this.jobBatchSize);

        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        if(config.containsKey(PROP_JOB_THREAD_POOL_SIZE) && config.get(PROP_JOB_THREAD_POOL_SIZE) != null){
            this.threadPoolSize = (Integer) config.get(PROP_JOB_THREAD_POOL_SIZE);
        }
        log.debug("threadPoolSize is " + this.threadPoolSize);

        String dbCleanSched = CLEAN_DB_SCHEDULE;
        if(config.containsKey(PROP_DB_CLEAN_SCHED) && config.get(PROP_DB_CLEAN_SCHED) != null){
            dbCleanSched = (String) config.get(PROP_DB_CLEAN_SCHED);
        }
        log.debug("dbCleanSched is " + dbCleanSched);

        this.dbDataMaxAge = DEFAULT_DB_DATA_MAX_AGE;
        if(config.containsKey(PROP_DB_DATA_MAX_AGE) && config.get(PROP_DB_DATA_MAX_AGE) != null){
            this.dbDataMaxAge = (Integer) config.get(PROP_DB_DATA_MAX_AGE);
        }
        log.debug("dbDataMaxAge is " + this.dbDataMaxAge);

        boolean disableScheduler = DEFAULT_DISABLE_SCHEDULER;
        if(config.containsKey(PROP_DISABLE_SCHEDULER) && config.get(PROP_DISABLE_SCHEDULER) != null){
            disableScheduler = (((Integer) config.get(PROP_DISABLE_SCHEDULER)) != 0 ? true : false);
        }
        log.debug("disableScheduler is " + disableScheduler);

        boolean disableChecks = DEFAULT_DISABLE_CHECKS;
        if(config.containsKey(PROP_DISABLE_CHECKS) && config.get(PROP_DISABLE_CHECKS) != null){
            disableChecks = (((Integer) config.get(PROP_DISABLE_CHECKS)) != 0 ? true : false);
        }
        log.debug("disableChecks is " + disableChecks);

        //init database
        boolean skipTableBuild = false;
        if(config.containsKey(PROP_SKIP_TABLE_BUILD) && config.get(PROP_SKIP_TABLE_BUILD) != null){
            skipTableBuild = (((Integer) config.get(PROP_SKIP_TABLE_BUILD)) != 0 ? true : false);
        }
        String dbFile = DEFAULT_DB;
        if(config.containsKey(PROP_DATABASE) && config.get(PROP_DATABASE) != null){
            dbFile = (String) config.get(PROP_DATABASE);
        }
        try {
            this.initDatabase(dbFile, skipTableBuild);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } 

        //set web parameters
       /* Map webConfig = null;
        if(!config.containsKey(PROP_WEB) || config.get(PROP_WEB) == null){
            throw new RuntimeException("Dashboard does not contain a " + PROP_WEB +  " property");
        }
        webConfig = (Map) config.get(PROP_WEB);
        this.webTitle = DEFAULT_WEB_TITLE;
        if(webConfig.containsKey(PROP_WEB_TITLE) && webConfig.get(PROP_WEB_TITLE) != null){
            this.webTitle = (String) webConfig.get(PROP_WEB_TITLE);
        }
        log.debug("webTitle is " + this.webTitle);

        this.defaultDashboard = null;
        if(webConfig.containsKey(PROP_WEB_DEFAULT) && webConfig.get(PROP_WEB_DEFAULT) != null){
            this.defaultDashboard = (String) webConfig.get(PROP_WEB_DEFAULT);
        }
        log.debug("defaultDashboard is " + this.defaultDashboard); */
        
        //load dashboards
        if(config.containsKey(PROP_DASHBOARDS) && config.get(PROP_DASHBOARDS) != null){
            this.configureDashboards((List)config.get(PROP_DASHBOARDS));
        }
        //load tests
        try {
            this.checkTypeClassMap = ConfigLoader.load(config, this.dataSource);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }

        //init scheduler
        if(!disableScheduler){
            Properties props = new Properties();
            props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.setProperty("org.quartz.threadPool.threadCount", this.threadPoolSize + "");
            try{
                SchedulerFactory schedFactory = new StdSchedulerFactory(props);
                this.scheduler = schedFactory.getScheduler();
                this.scheduler.start();
                if(this.dbDataMaxAge >= 0L){
                    CronTrigger cleanCronTrigger = new CronTrigger("CleanTrigger", "CLEAN", dbCleanSched);
                    JobDetail cleanJobDetail = new JobDetail("CleanScheduler", "CLEAN", CleanDBJob.class);
                    this.scheduler.scheduleJob(cleanJobDetail, cleanCronTrigger);
                }
            }catch(Exception e){
                throw new RuntimeException(e.getMessage());
            }
            if(!disableChecks){
                //job that checks for new jobs is in own thread
                this.checkShedJob = new CheckSchedulerJob("MaDDashCheckSchedulerJob");
                this.checkShedJob.start();
            }
            this.scheduledChecks = new HashMap<Integer,Boolean>();
        }

        /*
         * NOTE: PackagesResourceConfig does not appear to work
         * with one-jar class loader so loading individually for now
         */
        String resourceProto = null;
        int resourcePort = 0;
        String resourceHost = serverHost; 
        String[] services = {
                DashboardsResource.class.getName(),
                GridsResource.class.getName(),
                GridResource.class.getName(),
                RowResource.class.getName(),
                CellResource.class.getName(),
                CheckResource.class.getName()
        };
        ResourceConfig rc = new ClassNamesResourceConfig(services);

        //create server
        this.webServer = new WebServer(serverHost,rc);

        //configure HTTP
        if(config.containsKey(PROP_HTTP) && config.get(PROP_HTTP) != null){
            resourcePort = this.configureHttp((Map)config.get(PROP_HTTP));
            resourceProto = "http";
        }

        //configure HTTPS
        if(config.containsKey(PROP_HTTPS) && config.get(PROP_HTTPS) != null){
            resourcePort = this.configureHttps((Map)config.get(PROP_HTTPS));
            resourceProto = "https";
        }

        //determine url
        Map urlConfig = null;
        if(config.containsKey(PROP_RESOURCE_URL)){
            urlConfig = (Map) config.get(PROP_RESOURCE_URL);
        }
        this.configureResourceURL(urlConfig, resourceProto, 
                resourceHost, resourcePort);
    }

    private void configureDashboards(List dashConfig) {
        this.dashboards = new JSONArray();
        HashMap<String,JSONArray> dashMap = new HashMap<String,JSONArray>();
        ArrayList<String> dashList = new ArrayList<String>();

            for(Map<String,Object> dashboard: (List<Map<String,Object>>)dashConfig){
                if(!dashboard.containsKey(PROP_DASHBOARDS_NAME) || dashboard.get(PROP_DASHBOARDS_NAME) == null){
                    throw new RuntimeException("Dashboard does not contain a " + PROP_DASHBOARDS_NAME +  " property under ");
                }
                if(!dashboard.containsKey(PROP_DASHBOARDS_GRIDS) || dashboard.get(PROP_DASHBOARDS_GRIDS) == null){
                    throw new RuntimeException("Dashboard does not contain a " + PROP_DASHBOARDS_GRIDS +  " property under ");
                }
                log.debug("Added dashboard " + (String)dashboard.get(PROP_DASHBOARDS_NAME));
                dashList.add((String)dashboard.get(PROP_DASHBOARDS_NAME));
                JSONArray grids = new JSONArray();
                for(Map configGrid : (List<Map>)dashboard.get(PROP_DASHBOARDS_GRIDS)){
                    if(!configGrid.containsKey(PROP_DASHBOARDS_GRIDS_NAME) || 
                            configGrid.get(PROP_DASHBOARDS_GRIDS_NAME) == null){
                        throw new RuntimeException("Grid list is missing " + PROP_DASHBOARDS_GRIDS_NAME + " atribute");
                    }
                    String name = (String)configGrid.get(PROP_DASHBOARDS_GRIDS_NAME);
                    JSONObject tmp = new JSONObject();
                    tmp.put("name", name);
                    tmp.put("uri", GridsResource.class.getAnnotation(Path.class).value() + "/" 
                            + URIUtil.normalizeURIPart(name));
                    grids.add(tmp);
                }
                dashMap.put((String)dashboard.get(PROP_DASHBOARDS_NAME), grids);
            }
            Collections.sort(dashList);
            
            for(String name : dashList){
                JSONObject tmp = new JSONObject();
                tmp.put("name", name);
                tmp.put("grids", dashMap.get(name));
                this.dashboards.add(tmp);
            }
    }

    private void configureResourceURL(Map urlConfig, String proto, 
            String host, int port) {

        //read config
        if(urlConfig != null){
            if(urlConfig.containsKey(PROP_RESOURCE_URL_PROTO) && 
                    urlConfig.get(PROP_RESOURCE_URL_PROTO) != null){
                proto = (String)urlConfig.get(PROP_RESOURCE_URL_PROTO);
            }

            if(urlConfig.containsKey(PROP_RESOURCE_URL_HOST) && 
                    urlConfig.get(PROP_RESOURCE_URL_HOST) != null){
                host = (String)urlConfig.get(PROP_RESOURCE_URL_HOST);
            }

            if(urlConfig.containsKey(PROP_RESOURCE_URL_PORT) && 
                    urlConfig.get(PROP_RESOURCE_URL_PORT) != null){
                port = (Integer)urlConfig.get(PROP_RESOURCE_URL_PORT);
            }
        }

        //set resource URL
        UriBuilder builder = UriBuilder.fromPath("/").scheme(proto).host(host);
        if(("http".equals(proto) && port == 80) || ("https".equals(proto) && port == 443)){
            this.resourceURL = builder.build().toASCIIString();
        }else{
            this.resourceURL = builder.port(port).build().toASCIIString();
        }
        log.debug("resourceURL=" + this.resourceURL);
    }

    private int configureHttp(Map httpConfig) {
        this.log.debug("Configuring HTTP...");
        boolean proxyMode = DEFAULT_PROXY_MODE;
        if(httpConfig.containsKey(PROP_HTTP_PROXY_MODE) && 
                httpConfig.get(PROP_HTTP_PROXY_MODE) != null){
            proxyMode = (Boolean) httpConfig.get(PROP_HTTP_PROXY_MODE);
        }
        if(!httpConfig.containsKey(PROP_HTTP_PORT) ||
                httpConfig.get(PROP_HTTP_PORT) == null){
            throw new RuntimeException("No port specified in http configuration");
        }
        int port = (Integer)httpConfig.get(PROP_HTTP_PORT);
        try {
            this.webServer.addHttpListener(port, proxyMode);
        } catch (IOException e) {
            this.log.error("Error configuring HTTP: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error configuring HTTP: " + e.getMessage());
        }
        this.log.debug("    port=" + httpConfig.get(PROP_HTTP_PORT));
        return port;
    }

    private int configureHttps(Map httpsConfig) {
        this.log.debug("Configuring HTTPS...");
        if(!httpsConfig.containsKey(PROP_HTTPS_PORT) ||
                httpsConfig.get(PROP_HTTPS_PORT) == null){
            throw new RuntimeException("No port specified in https configuration");
        }
        int port =  (Integer)httpsConfig.get(PROP_HTTPS_PORT);
        this.log.debug("    port=" + port);

        if(!httpsConfig.containsKey(PROP_HTTPS_KEYSTORE) ||
                httpsConfig.get(PROP_HTTPS_KEYSTORE) == null){
            throw new RuntimeException("No keystore specified in https configuration");
        }
        String keystore = (String)httpsConfig.get(PROP_HTTPS_KEYSTORE);
        this.log.debug("    keystore=" + keystore);

        if(!httpsConfig.containsKey(PROP_HTTPS_KEYSTORE_PASSWORD) ||
                httpsConfig.get(PROP_HTTPS_KEYSTORE_PASSWORD) == null){
            throw new RuntimeException("No keystorePassword specified in https configuration");
        }
        String keystorePass = (String)httpsConfig.get(PROP_HTTPS_KEYSTORE_PASSWORD);
        this.log.debug("    keystorePass=" + keystorePass);

        String clientAuth = WebServer.HTTPS_CLIENT_AUTH_OFF;
        if(httpsConfig.containsKey(PROP_HTTPS_CLIENT_AUTH) &&
                httpsConfig.get(PROP_HTTPS_CLIENT_AUTH) != null){
            clientAuth = (String) httpsConfig.get(PROP_HTTPS_CLIENT_AUTH);
        }

        //get proxy mode
        boolean proxyMode = DEFAULT_PROXY_MODE;
        if(httpsConfig.containsKey(PROP_HTTPS_PROXY_MODE) && 
                httpsConfig.get(PROP_HTTPS_PROXY_MODE) != null){
            proxyMode = (Boolean) httpsConfig.get(PROP_HTTPS_PROXY_MODE);
        }
        this.log.debug("    clientAuth=" + clientAuth);

        try {
            this.webServer.addHttpsListener(port, keystore, keystorePass, clientAuth, proxyMode);
        } catch (IOException e) {
            this.log.error("Error configuring HTTPS: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error configuring HTTPS: " + e.getMessage());
        }

        return port;
    }

    /**
     * @return the server
     */
    public WebServer getWebServer() {
        return this.webServer;
    }

    /**
     * Returns shared instance of this class
     * 
     * @return shared instance of this class
     * @throws PropertyVetoException
     * @throws SchedulerException
     * @throws ParseException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    synchronized static public MaDDashGlobals getInstance() {
        if(instance == null){
            instance = new MaDDashGlobals();
        }

        return instance;
    }

    /**
     * Connects to derby database and creates it if it does not exist. 
     * Also creates tables if they do not exist.
     * @param dbname the directory where the database files will be stored
     * @throws PropertyVetoException
     * @throws SQLException
     */
    synchronized private void initDatabase(String dbname, boolean skipTableBuild) throws PropertyVetoException, SQLException{
        if(dataSource == null){
            System.setProperty("derby.system.home", dbname);
            dataSource = new ComboPooledDataSource();
            //Set c3p0 properties
            //allow one connection for each thread 
            dataSource.setMaxPoolSize(this.threadPoolSize);
            //sets how often to set for stale connections
            dataSource.setIdleConnectionTestPeriod(C3P0_IDLE_TEST_PERIOD);
            //set query used to test stale connection
            dataSource.setPreferredTestQuery(C3P0_TEST_QUERY);
            //set class that sets thread isolation level
            dataSource.setConnectionCustomizerClassName(MaDDashConnectionCustomizer.class.getName());

            dataSource.setDriverClass(JDBC_DRIVER);
            dataSource.setJdbcUrl(JDBC_URL);
            log.debug("Set database to " + dbname);
            log.debug("JDBC_DRIVER is " + JDBC_DRIVER);
            log.debug("JDBC_URL is " + JDBC_URL);
            Connection conn = this.dataSource.getConnection();

            //Create tables
            if(!skipTableBuild){
                this.execSQLCreate("CREATE TABLE checks (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                        "checkTemplateId INTEGER NOT NULL, gridName VARCHAR(500) NOT NULL, " +
                        "rowName VARCHAR(500) NOT NULL, colName VARCHAR(500) NOT NULL, checkName " +
                        "VARCHAR(500) NOT NULL, rowOrder INT NOT NULL, colOrder INT NOT " +
                        "NULL, description VARCHAR(2000) NOT NULL, prevCheckTime BIGINT " +
                        "NOT NULL, nextCheckTime BIGINT NOT NULL, checkStatus INTEGER " +
                        "NOT NULL, prevResultCode INTEGER NOT NULL, statusMessage VARCHAR(2000) NOT NULL, " +
                        "resultCount INTEGER NOT NULL, active INTEGER NOT NULL)", conn);
                this.execSQLCreate("CREATE TABLE checkTemplates (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                        " checkType VARCHAR(500) NOT NULL, checkParams VARCHAR(32672), checkInterval INTEGER NOT NULL, " +
                        "retryInterval INTEGER NOT NULL, retryAttempts INTEGER NOT NULL, " +
                        "timeout INTEGER NOT NULL)", conn);
                this.execSQLCreate("CREATE TABLE results (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "checkId INTEGER NOT NULL, checkTime BIGINT NOT NULL, returnCode " +
                        "INTEGER NOT NULL, returnMessage VARCHAR(2000) NOT NULL, returnParams VARCHAR(32672), " +
                        "resultCount INTEGER NOT NULL, checkStatus INTEGER NOT NULL)", conn);
                this.execSQLCreate("CREATE TABLE grids (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                        "gridName VARCHAR(500) NOT NULL, okLabel VARCHAR(2000) NOT NULL, " +
                        "warningLabel VARCHAR(2000) NOT NULL, criticalLabel VARCHAR(2000) NOT NULL, " +
                        "unknownLabel VARCHAR(2000) NOT NULL, notRunLabel VARCHAR(2000) NOT NULL )", conn);
                //Create indexes - always rebuilds indexes which can help performance.
                //    checks indexes
                this.execSQLCreate("DROP INDEX checksTemplateId", conn);
                this.execSQLCreate("CREATE INDEX checksTemplateId ON checks(checkTemplateId)", conn);
                this.execSQLCreate("DROP INDEX checksGridName", conn);
                this.execSQLCreate("CREATE INDEX checksGridName ON checks(gridName)", conn);
                this.execSQLCreate("DROP INDEX checksRowName", conn);
                this.execSQLCreate("CREATE INDEX checksRowName ON checks(rowName)", conn);
                this.execSQLCreate("DROP INDEX checksColName", conn);
                this.execSQLCreate("CREATE INDEX checksColName ON checks(colName)", conn);
                this.execSQLCreate("DROP INDEX checksCheckName", conn);
                this.execSQLCreate("CREATE INDEX checksCheckName ON checks(checkName)", conn);
                this.execSQLCreate("DROP INDEX checksActive", conn);
                this.execSQLCreate("CREATE INDEX checksActive ON checks(active)", conn);
                //    results indexes
                this.execSQLCreate("DROP INDEX resultsCheckId", conn);
                this.execSQLCreate("CREATE INDEX resultsCheckId ON results(checkId)", conn);
                this.execSQLCreate("DROP INDEX resultsCheckTime", conn);
                //DESC supposedly helps with MAX
                this.execSQLCreate("CREATE INDEX resultsCheckTime ON results(checkTime DESC)", conn);
                //grid index
                this.execSQLCreate("DROP INDEX gridsGridName", conn);
                this.execSQLCreate("CREATE UNIQUE INDEX gridsGridName ON grids(gridName)", conn);
                }
            
            conn.close();

        }
    }

    private void execSQLCreate(String sql, Connection conn) throws SQLException{
        try{
            conn.prepareStatement(sql).execute();
        }catch(SQLException e){
            if("X0Y32".equals(e.getSQLState())){
                log.debug("Cannot create table because it already exists: " + e.getMessage());
            }else if("42X65".equals(e.getSQLState())){
                log.debug("Cannot drop index because it does not exist: " + e.getMessage());
            }else{
                throw e;
            }
        }
    }

    /**
     * Updates map of checks that are currently being scheduled. Should be 
     * called with schedule set to true when job is initially scheduled and 
     * false when job completes. Prevent job from being scheduled twice.
     * 
     * @param checkId the id of the check to add/remove to map
     * @param schedule if true will be added to the map, false it will be removed
     */
    synchronized public void updateScheduledChecks(Integer checkId, Boolean schedule){
        if(schedule){
            this.scheduledChecks.put(checkId, schedule);
        }else if(this.scheduledChecks.containsKey(checkId)){
            this.scheduledChecks.remove(checkId);
        }
    }

    /**
     * Returns true if check is already scheduled, false otherwise
     * @param checkId the id of the check to verify
     * @return
     */
    public boolean isCheckScheduled(Integer checkId){
        return (this.scheduledChecks.containsKey(checkId) && this.scheduledChecks.get(checkId));
    }

    /**
     * Returns the database data source that should be used to obtain 
     * database connections
     * @return database data source
     */
    public ComboPooledDataSource getDataSource(){
        return this.dataSource;
    }

    /**
     * @return the quartz job scheduler
     */
    public Scheduler getScheduler(){
        return this.scheduler;
    }

    /**
     * @return the check classes indexed by name
     */
    public Map<String, Class> getCheckTypeClassMap(){
        return this.checkTypeClassMap;
    }

    /**
     * @return the JSON of the dashboards configured
     */
    public JSONArray getDashboards() {
        return this.dashboards;
    }

    /**
     * @return the title displayed at the top of the web page
     */
    public String getWebTitle() {
        return this.webTitle;
    }

    /**
     * @return the default dashboard to be displayed by the web interfacce
     */
    public String getDefaultDashboard() {
        return this.defaultDashboard;
    }

    /**
     * @return the maximum number of jobs to have in the job queue
     */
    public int getJobBatchSize() {
        return this.jobBatchSize;
    }

    /**
     * @return the dbDataMaxAge
     */
    public long getDbDataMaxAge() {
        return this.dbDataMaxAge;
    }

    /**
     * @return the resourceManager
     */
    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }
}
