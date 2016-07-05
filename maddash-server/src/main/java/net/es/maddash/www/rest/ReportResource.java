package net.es.maddash.www.rest;

import java.io.InputStream;

import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import net.es.maddash.DBMesh;
import net.es.maddash.madalert.JsonMesh;
import net.es.maddash.madalert.Madalert;
import net.es.maddash.madalert.Mesh;
import net.es.maddash.madalert.Report;

@Path("/maddash/report")
public class ReportResource {

    @Context
    private UriInfo context;

    @GET
    @Produces("application/json")
    public Response generateReport(@QueryParam("json") String jsonUrl, @QueryParam("grid") String gridName) {
        WebTarget webTarget;
        Client client = null;
        Report report = null;
        try {
            //build mesh
            Mesh mesh = null;
            if(jsonUrl != null && gridName != null){
                return Response.serverError().entity("You cannot specify both json and grid").build();
            }else if(jsonUrl != null){
                client = javax.ws.rs.client.ClientBuilder.newClient();
                webTarget = client.target(jsonUrl);
                WebTarget resource = webTarget;
                mesh = JsonMesh.from(Json.createReader(resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(InputStream.class)).readObject(), jsonUrl);
            }else if(gridName != null){
                mesh = new DBMesh(gridName, context.getPath());
            }else{
                return Response.serverError().entity("Must specify one of 'json' or 'grid' in parameters list").build();
            }
            report = Madalert.lookupRule(mesh.getName()).createReport(mesh);
        } catch(Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        } finally {
            if (client != null) {
                client.close();
            }
        }
        
        return Response.ok().entity(report.toJson().toString()).build();
    }
}
