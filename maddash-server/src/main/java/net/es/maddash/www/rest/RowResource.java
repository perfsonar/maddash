package net.es.maddash.www.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import net.es.maddash.MaDDashGlobals;
import net.es.maddash.NetLogger;
import net.sf.json.JSONObject;

@Path("/maddash/grids/{gridName}/{rowName}")
public class RowResource {
    Logger log = Logger.getLogger(GridResource.class);
    Logger netLogger = Logger.getLogger("netLogger");
    @Context UriInfo uriInfo;
    
    final private String GET_EVENT = "maddash.www.rest.RowResource.get";
    
    @Produces("application/json")
    @GET
    public Response get(@PathParam("gridName") String gridName, @PathParam("rowName") String rowName, @Context HttpHeaders httpHeaders){
        NetLogger netLog = NetLogger.getTlogger();
        this.netLogger.info(netLog.start(GET_EVENT));
        
        JSONObject json = null;
        try{
            json = MaDDashGlobals.getInstance().getResourceManager().getRow(gridName, rowName, uriInfo);
        }catch(Exception e){
            this.netLogger.error(netLog.error(GET_EVENT, e.getMessage()));
            return Response.serverError().entity(e.getMessage()).build();
        }
        //detect if not found
        if(json == null){
            this.netLogger.error(netLog.error(GET_EVENT, "Row resource not found"));
            return Response.status(Status.NOT_FOUND).entity("Row resource not found").build();
        }
        
        this.netLogger.info(netLog.end(GET_EVENT));
        return Response.ok().entity(json.toString()).build();
    }
}
