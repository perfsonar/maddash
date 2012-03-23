package net.es.maddash;

import gov.lbl.netlogger.LogMessage;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
/**
 * Utility class for generating NetLogger output. 
 * Based on similar class from OSCARS.
 * 
 * @author Andy Lake<alake@es.net>
 */
public class NetLogger {
    private String moduleName = null;
    HashMap<String, String> fieldMap;
    
    static public final String GUID_KW = "guid";
    static public final String MSG_KW = "msg";
    static public final String STATUS_KW = "status";
    static public final String URL_KW = "url";
    
    private static ThreadLocal<NetLogger> LOG = new ThreadLocal<NetLogger>() {   
        protected NetLogger initialValue() {
            return new NetLogger();
        }
    };
    
    public static void setTlogger(NetLogger log){
        LOG.set(log);
    }
    public static NetLogger getTlogger() {
        return (LOG.get());
    }

    public NetLogger() {
        this.fieldMap = new HashMap<String, String>();
        this.fieldMap.put(GUID_KW, UUID.randomUUID().toString());
    }

    /**
     * Generates an event suffixed with ".end" and status 0. This method sets the
     * <i>event</i> field.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage end(String event){
        return this.end(event, null, null);
    }
    
    /**
     * Generates an event suffixed with ".end" with status 0. This method sets the
     * <i>event</i> and <i>msg</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage end(String event, String msg){
        return this.end(event, msg, null);
    }
    
    /**
     * Generates an event suffixed with ".end". This method sets the
     * <i>event</i>, <i>msg</i>, and <i>url</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL  to a local file or the JDBC 
     *        URL that identifies a database.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage end(String event, String msg, String url){
        return this.end(event, msg, url, null);
    }
    
    /**
     * Generates an event suffixed with ".end". This method sets the
     * <i>event</i>, <i>msg</i>, and <i>url</i> fields.
     * It also accepts a HashMap that defines additional fields not 
     * specified by the other arguments.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL  to a local file or the JDBC 
     *        URL that identifies a database.
     * @param entryFieldMap a HashMap with additional fields not defined by the 
     *                      other arguments. The HashMap key and value correspond 
     *                      the key and value in the NetLogger field. If a field is 
     *                      used frequently it is recommended that this class be 
     *                      sub-classed as opposed to passing a HashMap.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage end(String event, String msg, String url, HashMap<String, String> entryFieldMap){
        if(entryFieldMap == null){
            entryFieldMap = new HashMap<String, String>();
        }
        entryFieldMap.put(STATUS_KW, "0");
        if(msg != null){
            entryFieldMap.put(MSG_KW, msg);
        }
        if(url != null){
            entryFieldMap.put(URL_KW, url);
        }
        
        return this.getMsg(event+".end", entryFieldMap);
    }
    
    /**
     * Generates an event suffixed with ".end" and status -1. This method sets the
     * <i>event</i>, <i>errSeverity</i> and <i>msg</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param errSeverity a String indicating the severity of the error.
     * @param msg a String containing additional information about the error
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage error(String event, String msg){
        return this.error(event, msg, null, null);
    }
    
    /**
     * Generates an event suffixed with ".end" and status -1. This method sets the
     * <i>event</i>, <i>errSeverity</i>, <i>msg</i>, and <i>url</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param errSeverity a String indicating the severity of the error.
     * @param msg a String containing additional information about the error
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL to a local file or the JDBC 
     *        URL that identifies a database.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage error(String event, String msg, String url){
        return this.error(event, msg, url, null);
    }
    
    /**
     * Generates an event suffixed with ".end" and status -1. This method sets the
     * <i>event</i>, <i>errSeverity</i>, <i>msg</i>, and <i>url</i> fields.
     * It also accepts a HashMap that defines additional fields not 
     * specified by the other arguments.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".end".
     * @param errSeverity a String indicating the severity of the error.
     * @param msg a String containing additional information about the error
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL to a local file or the JDBC 
     *        URL that identifies a database.
     * @param entryFieldMap a HashMap with additional fields not defined by the 
     *                      other arguments. The HashMap key and value correspond 
     *                      the key and value in the NetLogger field. If a field is 
     *                      used frequently it is recommended that this class be 
     *                      sub-classed as opposed to passing a HashMap.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage error(String event, String msg, String url, HashMap<String, String> entryFieldMap){
        if(entryFieldMap == null){
            entryFieldMap = new HashMap<String, String>();
        }
        
        entryFieldMap.put(STATUS_KW, "-1");
        
        if(msg != null){
            entryFieldMap.put(MSG_KW, msg);
        }
        if(url != null){
            entryFieldMap.put(URL_KW, url);
        }
        
        return this.getMsg(event+".end", entryFieldMap);
    }
    
    /**
     * Generates an event suffixed with ".start". This method sets the
     * <i>event</i> field.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".start".
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage start(String event){
        return this.start(event, null, null, null);
    }
    
    /**
     * Generates an event suffixed with ".start". This method sets the
     * <i>event</i> and <i>msg</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".start".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage start(String event, String msg){
        return this.start(event, msg, null, null);
    }
    
    /**
     * Generates an event suffixed with ".start". This method sets the
     * <i>event</i>, <i>msg</i>, and <i>url</i> fields.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".start".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL  to a local file or the JDBC 
     *        URL that identifies a database.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage start(String event, String msg, String url){
        return this.start(event, msg, url, null);
    }
    
    /**
     * Generates an event suffixed with ".start". This method sets the
     * <i>event</i>, <i>msg</i>, and <i>url</i> fields. It also accepts
     * a HashMap that defines additional fields not specified by the other
     * arguments.
     *  
     * @param event the name of the event to occur. It will automatically 
     *        be prefixed with the module name and suffixed with ".start".
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @param url a String representing a URL to an I/O resources. For example,
     *        a URL to an external service, a URL  to a local file or the JDBC 
     *        URL that identifies a database.
     * @param entryFieldMap a HashMap with additional fields not defined by the 
     *                      other arguments. The HashMap key and value correspond 
     *                      the key and value in the NetLogger field. If a field is 
     *                      used frequently it is recommended that this class be 
     *                      sub-classed as opposed to passing a HashMap.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage start(String event, String msg, String url, HashMap<String, String> entryFieldMap){
        if(entryFieldMap == null){
            entryFieldMap = new HashMap<String, String>();
        }
        if(msg != null){
            entryFieldMap.put(MSG_KW, msg);
        }
        if(url != null){
            entryFieldMap.put(URL_KW, url);
        }
        
        return this.getMsg(event+".start", entryFieldMap);
    }
    
    /**
     * Generates a logging event automatically prefixed with the module name
     * and with fields specified in the provided HashMap. It also sets the time 
     * with the current time (in nanoseconds).
     *  
     * @param event the name of the event to occur.
     * @param msg a String containing additional information about the event 
     *            such as an error message.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage getMsg(String event, String msg){
        HashMap<String,String> entryFieldMap = new HashMap<String,String>();
        if(msg != null){
            entryFieldMap.put(MSG_KW, msg);
        }
        return this.getMsg(event, entryFieldMap);
    }
    /**
     * Generates a logging event automatically prefixed with the module name
     * and with fields specified in the provided HashMap. It also sets the time 
     * with the current time (in nanoseconds).
     *  
     * @param event the name of the event to occur.
     * @param entryFieldMap a HashMap with additional fields not defined by the 
     *                      other arguments. The HashMap key and value correspond 
     *                      the key and value in the NetLogger field. If a field is 
     *                      used frequently it is recommended that this class be 
     *                      sub-classed as opposed to passing a HashMap.
     * @return a LogMessage from the NetLogger API that can be passed to a 
     *         logging library.
     */
    public LogMessage getMsg(String event, HashMap<String,String> entryFieldMap){
        LogMessage logMsg = new LogMessage(this.moduleName + "." + event);
        
        /* Set fields used in all messages */
        for(String field : fieldMap.keySet()){
            if(fieldMap.get(field) != null){
                this.addField(field, fieldMap.get(field), logMsg);
            }
        }
        
        /* Set fields local to this message */
        for(String field : entryFieldMap.keySet()){
            if(entryFieldMap.get(field) != null){
                this.addField(field, entryFieldMap.get(field), logMsg);
            }
        }
        
        /* Set timestamp */
        logMsg.setTimeStampNanos(System.nanoTime());
        
        return logMsg;
    }
    
    /**
     * Returns the ModuleName for this netLogger
     * 
     * @return a String with the moduleName, this will be null
     *     if the netLogger object has not been initialized
     */
    public String getModuleName() {
        return this.moduleName;
    }
    
    /**
     * Returns the UUID that identifies the global transaction
     * 
     * @return a String with the UUID included in each log entry that identifies the
     *    global transaction
     */
    public String getGUID() {
        return this.fieldMap.get(GUID_KW);
    }
    /**
     * Sets the identifier included in each log entry generated by this class.
     * The GUID is automatically generated at instantiation so this class should
     * only need to be called in special situations.
     * 
     * @param guid a String with the identifier to set. 
     */
    public void setGUID(String guid) {
        this.fieldMap.put(GUID_KW, guid);
    }
    
    /**
     * Return the HashMap with all of the fields used globally across 
     * each log entry generated by this instance. You may set additional 
     * fields used across all messages by calling the <code>put</code> 
     * method on the HashMap.
     * 
     * @return the HashMap containing the fields used globally across 
     * each log entry generated by this instance.
     */
    public HashMap<String, String> getFieldMap(){
        return this.fieldMap;
    }
    
    /**
     * Chops off lines beyond 1, adds quotes around string, and replaces 
     * any double quotes with single quotes. Should be used for fields 
     * that potentially have spaces.
     * 
     * @param msg the String to be formatted
     * @return the formatted string
     */
    private String quote(String msg){
        if(msg.indexOf("\n") != -1){
            msg = msg.substring(0, msg.indexOf("\n")).trim();
        }
        msg = "\"" + msg.replaceAll("\"", "'") + "\"";
        return msg;
    }
    
    /**
     * Private method for adding a correctly quoted field to the log message.
     * 
     * @param key the name of the field to add
     * @param value the value to assign
     * @param netLogMsg the log message to add the field
     */
    private void addField(String key, String value, LogMessage netLogMsg){
        if(value != null && value.matches(".*\\s.*")){
            value = this.quote(value);
        }
        netLogMsg.add(key, value);
    }
    
    /**
     * Serializes a list for output by NetLogger. Surrounds strings with 
     * brackets and separates by commas. Example: [string1,string2,string3]
     * 
     * @param list the list to serialize
     * @return a String representation of the list
     */
    static public String serializeList(List<String> list){
        if(list == null){
            return "null";
        }
        return serializeArray(list.toArray(new String[list.size()]));
    }
    
    /**
     * Serializes an array for output by NetLogger. Surrounds strings with 
     * brackets and separates by commas.Example: [string1,string2,string3]
     * 
     * @param array the array to serialize
     * @return a String representation of the array
     */
    static public String serializeArray(String[] array){
        if(array == null){
            return "null";
        }
        String str = "[";
        for(int i = 0; i < array.length; i++){
            if(i != 0){ 
                str += ","; 
            };
            str += array[i];
        }
        str += "]";
        return str;
    }
}
