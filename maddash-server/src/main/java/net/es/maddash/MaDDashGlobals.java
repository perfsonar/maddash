package net.es.maddash;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class MaDDashGlobals {
    static private Logger log = Logger.getLogger(MaDDashGlobals.class);
    static private MaDDashGlobals instance = null;
    static private String configFile = null;
    
    private ComboPooledDataSource dataSource = null;
    private Scheduler scheduler;
    private int serverPort;
    private int jobBatchSize;
    private int threadPoolSize;
    private String urlRoot;
    private Map<String, Class> checkTypeClassMap;
    private HashMap<Integer, Boolean> scheduledChecks; 
    
    //web related paramters
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
    final static private int DEFAULT_JOB_BATCH_SIZE = 250;
    final static private int DEFAULT_THREAD_POOL_SIZE = 50;
    final static private boolean DEFAULT_DISABLE_SCHEDULER = false;
    final static private String CHECK_SCHEDULE = "0 * * * * ?";
    final static private String CLEAN_DB_SCHEDULE = "0 0 * * * ?";//every hour
    
    public static void init(String tmpConfigFile){
        configFile = tmpConfigFile;
    }
    
    public MaDDashGlobals() throws PropertyVetoException, SchedulerException, ParseException, FileNotFoundException, ClassNotFoundException, SQLException{
        //check config file
        if(configFile == null){
            throw new RuntimeException("No config file set.");
        }
        Map config = (Map) Yaml.load(new File(configFile));
        
        //init database
        String dbFile = DEFAULT_DB;
        if(config.containsKey(PROP_DATABASE) && config.get(PROP_DATABASE) != null){
            dbFile = (String) config.get(PROP_DATABASE);
        }
        this.initDatabase(dbFile);
        
        //set server port
        this.serverPort = DEFAULT_PORT;
        if(config.containsKey(PROP_SERVER_PORT) && config.get(PROP_SERVER_PORT) != null){
            this.serverPort = (Integer) config.get(PROP_SERVER_PORT);
        }
        
        //set url root
        this.urlRoot = DEFAULT_URL_ROOT;
        if(config.containsKey(PROP_URL_ROOT) && config.get(PROP_URL_ROOT) != null){
            this.urlRoot = (String) config.get(PROP_URL_ROOT);
        }
        
        this.jobBatchSize = DEFAULT_JOB_BATCH_SIZE;
        if(config.containsKey(PROP_JOB_BATCH_SIZE) && config.get(PROP_JOB_BATCH_SIZE) != null){
            this.jobBatchSize = (Integer) config.get(PROP_JOB_BATCH_SIZE);
        }
        
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        if(config.containsKey(PROP_JOB_THREAD_POOL_SIZE) && config.get(PROP_JOB_THREAD_POOL_SIZE) != null){
            this.threadPoolSize = (Integer) config.get(PROP_JOB_THREAD_POOL_SIZE);
        }
        
        boolean disableScheduler = DEFAULT_DISABLE_SCHEDULER;
        if(config.containsKey(PROP_DISABLE_SCHEDULER) && config.get(PROP_DISABLE_SCHEDULER) != null){
            disableScheduler = (((Integer) config.get(PROP_DISABLE_SCHEDULER)) != 0 ? true : false);
        }
        
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
        this.defaultDashboard = null;
        if(webConfig.containsKey(PROP_WEB_DEFAULT) && webConfig.get(PROP_WEB_DEFAULT) != null){
            this.defaultDashboard = (String) webConfig.get(PROP_WEB_DEFAULT);
        }
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
                dashList.add((String)dashboard.get(PROP_WEB_DASHBOARDS_NAME));
                dashMap.put((String)dashboard.get(PROP_WEB_DASHBOARDS_NAME), (List<Map>)dashboard.get(PROP_WEB_DASHBOARDS_GRIDS));
            }
            Collections.sort(dashList);
            this.dashboards = new JSONArray();
            for(String name : dashList){
                System.out.println("name=" + name);
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
            CronTrigger cleanCronTrigger = new CronTrigger("CleanTrigger", "CLEAN", CLEAN_DB_SCHEDULE);
            JobDetail cleanJobDetail = new JobDetail("CleanScheduler", "CLEAN", CleanDBJob.class);
            this.scheduler.scheduleJob(cleanJobDetail, cleanCronTrigger);
            this.scheduledChecks = new HashMap<Integer,Boolean>();
        }
    }
    
    synchronized static public MaDDashGlobals getInstance() throws PropertyVetoException, SchedulerException, ParseException, FileNotFoundException, ClassNotFoundException, SQLException{
        if(instance == null){
            instance = new MaDDashGlobals();
        }
        
        return instance;
    }
    
    synchronized private void initDatabase(String dbname) throws PropertyVetoException, SQLException{
        if(dataSource == null){
            System.setProperty("derby.system.home", dbname);
            dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass(JDBC_DRIVER);
            dataSource.setJdbcUrl(JDBC_URL);
            
            Connection conn = this.dataSource.getConnection();
            try{
                conn.prepareStatement("CREATE TABLE checks (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "checkTemplateId INTEGER NOT NULL, gridName VARCHAR(500) NOT NULL, " +
                    "rowName VARCHAR(500) NOT NULL, colName VARCHAR(500) NOT NULL, checkName " +
                    "VARCHAR(500) NOT NULL, rowOrder INT NOT NULL, colOrder INT NOT " +
                    "NULL, description VARCHAR(2000) NOT NULL, prevCheckTime INTEGER " +
                    "NOT NULL, nextCheckTime INTEGER NOT NULL, checkStatus INTEGER " +
                    "NOT NULL, prevResultCode INTEGER NOT NULL, statusMessage VARCHAR(2000) NOT NULL, " +
                    "resultCount INTEGER NOT NULL, active INTEGER NOT NULL)").execute();
            }catch(SQLException e){
                if("X0Y32".equals(e.getSQLState())){
                    System.out.println("Using existing checks table");
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
                    System.out.println("Using existing checkTemplates table");
                }else{
                    throw e;
                }
            }
            try{
                conn.prepareStatement("CREATE TABLE results (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                    "checkId INTEGER NOT NULL, checkTime INTEGER NOT NULL, returnCode " +
                    "INTEGER NOT NULL, returnMessage VARCHAR(2000) NOT NULL, returnParams VARCHAR(2000), " +
                    "resultCount INTEGER NOT NULL, checkStatus INTEGER NOT NULL)").execute();
            }catch(SQLException e){
                if("X0Y32".equals(e.getSQLState())){
                    System.out.println("Using existing results table");
                }else{
                    throw e;
                }
            }
            conn.close();
            
        }
    }
    
    synchronized public void updateScheduledChecks(Integer checkId, Boolean schedule){
        if(schedule){
            this.scheduledChecks.put(checkId, schedule);
        }else if(this.scheduledChecks.containsKey(checkId)){
            this.scheduledChecks.remove(checkId);
        }
    }
    
    public boolean isCheckScheduled(Integer checkId){
        return (this.scheduledChecks.containsKey(checkId) && this.scheduledChecks.get(checkId));
    }
    
    public ComboPooledDataSource getDataSource(){
        return this.dataSource;
    }
    
    public int getServerPort(){
        return this.serverPort;
    }
    
    public String getUrlRoot(){
        return this.urlRoot;
    }
    
    public Scheduler getScheduler(){
        return this.scheduler;
    }
    
    public Map<String, Class> getCheckTypeClassMap(){
        return this.checkTypeClassMap;
    }

    /**
     * @return the dashboards
     */
    public JSONArray getDashboards() {
        return this.dashboards;
    }

    /**
     * @return the webTitle
     */
    public String getWebTitle() {
        return this.webTitle;
    }

    /**
     * @return the defaultDashboard
     */
    public String getDefaultDashboard() {
        return this.defaultDashboard;
    }

    /**
     * @return the jobBatchSize
     */
    public int getJobBatchSize() {
        return this.jobBatchSize;
    }

    /**
     * @param jobBatchSize the jobBatchSize to set
     */
    public void setJobBatchSize(int jobBatchSize) {
        this.jobBatchSize = jobBatchSize;
    }
}
