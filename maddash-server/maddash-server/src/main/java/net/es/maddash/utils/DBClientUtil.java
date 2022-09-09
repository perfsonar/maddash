package net.es.maddash.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBClientUtil {
    final static private String JDBC_URL = "jdbc:derby:maddash;create=true";
    final static private String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    
    public static void main(String[] args){
        //Read command line options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("d", "database"), "directory containing derby database").withRequiredArg().ofType(String.class); 
                acceptsAll(Arrays.asList("f", "file"), "SQL file to run").withRequiredArg().ofType(String.class);
                
               
            }
        };
        
        OptionSet opts = parser.parse(args);
        if(opts.has("h")){
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e){}
            System.exit(0);
        }
        
        String dbname = "";
        if(opts.has("d")){
            dbname = (String) opts.valueOf("d");
        }else{
            System.err.println("You must specify the -d option");
            System.exit(1);
        }
        
        String file = null;
        if(opts.has("f")){
            file = (String) opts.valueOf("f");
            file = file.trim();
        }else{
            System.err.println("You must specify the -f option");
            System.exit(1);
        }
        
        Connection conn = null;
        try {
            File fin = new File(file);
            BufferedReader reader = new BufferedReader(new FileReader(fin));
            System.setProperty("derby.system.home", dbname);
            ComboPooledDataSource dataSource = new ComboPooledDataSource();
            dataSource.setDriverClass(JDBC_DRIVER);
            dataSource.setJdbcUrl(JDBC_URL);
            conn = dataSource.getConnection();
            String line = "";
            while((line = reader.readLine()) != null){
                System.out.println(line);
                conn.createStatement().execute(line);
            }
            conn.close();
        } catch (Exception e) {
            if(conn != null){
                try{
                    conn.close();
                }catch(SQLException e2){}
            }
            e.printStackTrace();
        } 
        
    }
}
