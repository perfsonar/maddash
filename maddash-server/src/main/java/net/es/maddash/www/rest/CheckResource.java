package net.es.maddash.www.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;
import net.sf.json.JSONObject;

@Path("/maddash/grids/{gridName}/{rowName}/{colName}/{checkName}")
public class CheckResource {
    Logger log = Logger.getLogger(GridResource.class);
    Logger netLogger = Logger.getLogger("netLogger");
    @Context UriInfo uriInfo;
    @DefaultValue("10") @QueryParam("resultsPerPage") int pageResults;
    @DefaultValue("0") @QueryParam("page") int page;
    
    final private String GET_EVENT = "maddash.www.rest.CheckResource.get";
    
    @Produces("application/json")
    @GET
    public Response get(@PathParam("gridName") String gridName, 
            @PathParam("rowName") String rowName, 
            @PathParam("colName") String colName, 
            @PathParam("checkName") String checkName, 
            @Context HttpHeaders httpHeaders){
        NetLogger netLog = NetLogger.getTlogger();
        this.netLogger.info(netLog.start(GET_EVENT));
        
        JSONObject json = null;
        try{
            json = MaDDashGlobals.getInstance().getResourceManager().getCheck(gridName, rowName, 
                    colName, checkName, uriInfo, pageResults, page);
        }catch(Exception e){
            this.netLogger.error(netLog.error(GET_EVENT, e.getMessage()));
            return Response.serverError().entity(e.getMessage()).build();
        }
        //detect if not found
        if(json == null){
            this.netLogger.error(netLog.error(GET_EVENT, "Check resource not found"));
            return Response.status(Status.NOT_FOUND).entity("Check resource not found").build();
        }
        
        this.netLogger.info(netLog.end(GET_EVENT));
        return Response.ok().entity(json.toString()).build();
    }
}
