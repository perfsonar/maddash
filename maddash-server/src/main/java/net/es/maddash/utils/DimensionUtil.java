package net.es.maddash.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.es.maddash.jobs.CheckSchedulerJob;
import net.sf.json.JSONObject;

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

	static public List<String> translateNames(List<String> names, HashMap<String,String> labelMap){
		ArrayList<String> translatedNames = new ArrayList<String>();
		//translate column to label if possible
		for(String name : names){
			String label = name;
			if(labelMap.containsKey(name) && labelMap.get(name) != null){
				label = labelMap.get(name);
			}
			translatedNames.add(label);
		}
		return translatedNames;
	}

	public static ArrayList<JSONObject> translateNames(ArrayList<JSONObject> namedObjs,
			HashMap<String, String> labelMap) {
		//translate column to label if possible
		for(JSONObject namedObj : namedObjs){
			String label = namedObj.getString("name");
			if(labelMap.containsKey(label) && labelMap.get(label) != null){
				label = labelMap.get(label);
			}
			namedObj.put("name", label);
		}
		return namedObjs;
	}

}
