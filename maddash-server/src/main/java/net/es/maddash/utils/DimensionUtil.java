package net.es.maddash.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import net.es.maddash.jobs.CheckSchedulerJob;

import org.apache.log4j.Logger;

public class DimensionUtil {
	static Logger log = Logger.getLogger(CheckSchedulerJob.class);
	
   static public Map<String,String> getParams(String configIdent, Connection conn){
	   HashMap<String,String> params = new HashMap<String,String>();
	   try{
		   PreparedStatement stmt = conn.prepareStatement("SELECT keyName, value FROM dimensions WHERE configIdent=?");
		   stmt.setString(1, configIdent);
		   ResultSet results = stmt.executeQuery();
		   while(results.next()){
			   params.put(results.getString(1), results.getString(2));
		   }
	   }catch(Exception e){
		   log.warn("Unable to get hos parameters for " + configIdent);
	   }
	   return params;
   }
   
}
