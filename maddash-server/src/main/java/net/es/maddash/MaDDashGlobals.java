package net.es.maddash;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.es.maddash.jobs.CheckSchedulerJob;
import net.es.maddash.jobs.CleanDBJob;
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
    private int serverPort;
    private int jobBatchSize;
    private int threadPoolSize;
    private long dbDataMaxAge;
    private String urlRoot;
    private Map<String, Class> checkTypeClassMap;
    private HashMap<Integer, Boolean> scheduledChecks; 
    
    //web related parameters
    private String webTitle;
    private JSONArray dashboards;
    private String defaultDashboard;
    
    //properties
    final private String PROP_SERVER_PORT = "serverPort";
    final private String PROP_DATABASE = "database";
    final private String PROP_URL_ROOT = "urlRoot";
    final private String PROP_JOB_BATCH_SIZE = "jobBatchSize";
    final private String PROP_JOB_THREAD_POOL_SIZE = "jobThreadPoolSize";
    final private String PROP_DISABLE_SCHEDULER = "disableScheduler";
    final private String PROP_DB_CLEAN_SCHED = "dbCleanSchedule";
    final private String PROP_DB_DATA_MAX_AGE = "dbDataMaxAge";
    final private String PROP_WEB = "web";
    final private String PROP_WEB_TITLE = "title";
    final private String PROP_WEB_DASHBOARDS = "dashboards";
    final private String PROP_WEB_DASHBOARDS_NAME = "name";
    final private String PROP_WEB_DASHBOARDS_GRIDS = "grids";
    final private String PROP_WEB_DEFAULT = "default";
    
    //final static private String JDBC_URL = "jdbc:sqlite:";
    final static private String JDBC_URL = "jdbc:derby:maddash;create=true";
    //final static private String JDBC_DRIVER = "org.sqlite.JDBC";
    final static private String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    final static private int DEFAULT_PORT = 8881;
    final static private String DEFAULT_WEB_TITLE = "MaDDash";
    final static private String DEFAULT_URL_ROOT = "/maddash";
    final static private String DEFAULT_DB = "data/dashboard.db";
    final static private long DEFAULT_DB_DATA_MAX_AGE = 86400*180;//every 180 days
    final static private int DEFAULT_JOB_BATCH_SIZE = 250;
    final static private int DEFAULT_THREAD_POOL_SIZE = 50;
    final static private int C3P0_IDLE_TEST_PERIOD = 600;
    final static private String C3P0_TEST_QUERY = "SELECT id FROM checkTemplates FETCH NEXT 1 ROWS ONLY";
    final static private boolean DEFAULT_DISABLE_SCHEDULER = false;
    final static private String CHECK_SCHEDULE = "0 * * * * ?";
    final static private String CLEAN_DB_SCHEDULE = "0 0,12 * * * ?";//every 12 hours
    
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
     * 
     * @throws PropertyVetoException
     * @throws SchedulerException
     * @throws ParseException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private MaDDashGlobals() throws PropertyVetoException, SchedulerException, ParseException, FileNotFoundException, ClassNotFoundException, SQLException{
        //check config file
        if(configFile == null){
            throw new RuntimeException("No config file set.");
        }
        Map config = (Map) Yaml.load(new File(configFile));

        //set server port
        this.serverPort = DEFAULT_PORT;
        if(config.containsKey(PROP_SERVER_PORT) && config.get(PROP_SERVER_PORT) != null){
            this.serverPort = (Integer) config.get(PROP_SERVER_PORT);
        }
        log.debug("Server port is " + this.serverPort);
        
        //set url root
        this.urlRoot = DEFAULT_URL_ROOT;
        if(config.containsKey(PROP_URL_ROOT) && config.get(PROP_URL_ROOT) != null){
            this.urlRoot = (String) config.get(PROP_URL_ROOT);
        }
        log.debug("urlRoot is " + this.serverPort);
        
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
        
        //init database
        String dbFile = DEFAULT_DB;
        if(config.containsKey(PROP_DATABASE) && config.get(PROP_DATABASE) != null){
            dbFile = (String) config.get(PROP_DATABASE);
        }
        this.initDatabase(dbFile);
        
        //set web parameters
        Map webConfig = null;
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
        log.debug("defaultDashboard is " + this.defaultDashboard);
        
        HashMap<String,List<Map>> dashMap = new HashMap<String,List<Map>>();
        ArrayList<String> dashList = new ArrayList<String>();
        if(webConfig.containsKey(PROP_WEB_DASHBOARDS) && webConfig.get(PROP_WEB_DASHBOARDS) != null){
            for(Map<String,Object> dashboard: (List<Map<String,Object>>)webConfig.get(PROP_WEB_DASHBOARDS)){
                if(!dashboard.containsKey(PROP_WEB_DASHBOARDS_NAME) || dashboard.get(PROP_WEB_DASHBOARDS_NAME) == null){
                    throw new RuntimeException("Dashboard does not contain a " + PROP_WEB_DASHBOARDS_NAME +  " property under ");
                }
                if(!dashboard.containsKey(PROP_WEB_DASHBOARDS_GRIDS) || dashboard.get(PROP_WEB_DASHBOARDS_GRIDS) == null){
                    throw new RuntimeException("Dashboard does not contain a " + PROP_WEB_DASHBOARDS_GRIDS +  " property under ");
                }
                log.debug("Added dashboard " + (String)dashboard.get(PROP_WEB_DASHBOARDS_NAME));
                dashList.add((String)dashboard.get(PROP_WEB_DASHBOARDS_NAME));
                dashMap.put((String)dashboard.get(PROP_WEB_DASHBOARDS_NAME), (List<Map>)dashboard.get(PROP_WEB_DASHBOARDS_GRIDS));
            }
            Collections.sort(dashList);
            this.dashboards = new JSONArray();
            for(String name : dashList){
                JSONObject tmp = new JSONObject();
                tmp.put("name", name);
                tmp.put("grids", dashMap.get(name));
                this.dashboards.add(tmp);
            }
        }
        
        //load tests
        this.checkTypeClassMap = ConfigLoader.load(config, this.dataSource);
        
        //init scheduler
        if(!disableScheduler){
            Properties props = new Properties();
            props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.setProperty("org.quartz.threadPool.threadCount", this.threadPoolSize + "");
            SchedulerFactory schedFactory = new StdSchedulerFactory(props);
            this.scheduler = schedFactory.getScheduler();
            this.scheduler.start();
            CronTrigger checkCronTrigger = new CronTrigger("CheckTrigger", "CHECKS", CHECK_SCHEDULE);
            JobDetail checkJobDetail = new JobDetail("CheckScheduler", "CHECKS", CheckSchedulerJob.class);
            this.scheduler.scheduleJob(checkJobDetail, checkCronTrigger);
            if(this.dbDataMaxAge >= 0L){
                CronTrigger cleanCronTrigger = new CronTrigger("CleanTrigger", "CLEAN", dbCleanSched);
                JobDetail cleanJobDetail = new JobDetail("CleanScheduler", "CLEAN", CleanDBJob.class);
                this.scheduler.scheduleJob(cleanJobDetail, cleanCronTrigger);
            }
            this.scheduledChecks = new HashMap<Integer,Boolean>();
        }
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
    synchronized static public MaDDashGlobals getInstance() throws PropertyVetoException, SchedulerException, ParseException, FileNotFoundException, ClassNotFoundException, SQLException{
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
    synchronized private void initDatabase(String dbname) throws PropertyVetoException, SQLException{
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
            
            dataSource.setDriverClass(JDBC_DRIVER);
            dataSource.setJdbcUrl(JDBC_URL);
            log.debug("Set database to " + dbname);
            log.debug("JDBC_DRIVER is " + JDBC_DRIVER);
            log.debug("JDBC_URL is " + JDBC_URL);
            
            Connection conn = this.dataSource.getConnection();
            try{
                conn.prepareStatement("CREATE TABLE checks (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "checkTemplateId INTEGER NOT NULL, gridName VARCHAR(500) NOT NULL, " +
                    "rowName VARCHAR(500) NOT NULL, colName VARCHAR(500) NOT NULL, checkName " +
                    "VARCHAR(500) NOT NULL, rowOrder INT NOT NULL, colOrder INT NOT " +
                    "NULL, description VARCHAR(2000) NOT NULL, prevCheckTime BIGINT " +
                    "NOT NULL, nextCheckTime BIGINT NOT NULL, checkStatus INTEGER " +
                    "NOT NULL, prevResultCode INTEGER NOT NULL, statusMessage VARCHAR(2000) NOT NULL, " +
                    "resultCount INTEGER NOT NULL, active INTEGER NOT NULL)").execute();
                log.debug("Created table checks");
            }catch(SQLException e){
                if("X0Y32".equals(e.getSQLState())){
                    log.debug("Table checks exists");
                }else{
                    throw e;
                }
            }
            try{
                conn.prepareStatement("CREATE TABLE checkTemplates (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    " checkType VARCHAR(500) NOT NULL, checkParams VARCHAR(2000), checkInterval INTEGER NOT NULL, " +
                    "retryInterval INTEGER NOT NULL, retryAttempts INTEGER NOT NULL, " +
                    "timeout INTEGER NOT NULL)").execute();
            }catch(SQLException e){
                if("X0Y32".equals(e.getSQLState())){
                    log.debug("Table checkTemplates exists");
                }else{
                    throw e;
                }
            }
            try{
                conn.prepareStatement("CREATE TABLE results (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "checkId INTEGER NOT NULL, checkTime BIGINT NOT NULL, returnCode " +
                    "INTEGER NOT NULL, returnMessage VARCHAR(2000) NOT NULL, returnParams VARCHAR(2000), " +
                    "resultCount INTEGER NOT NULL, checkStatus INTEGER NOT NULL)").execute();
            }catch(SQLException e){
                if("X0Y32".equals(e.getSQLState())){
                    log.debug("Table results exists");
                }else{
                    throw e;
                }
            }
            conn.close();
            
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
     * @return the port where the REST server runs
     */
    public int getServerPort(){
        return this.serverPort;
    }
    
    /**
     * @return the root of the rest URL (e.g. /maddash)
     */
    public String getUrlRoot(){
        return this.urlRoot;
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
}
