package com.matson.tos.nascent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;  
import javax.ws.rs.core.MediaType; 
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
 
@Path("/lanedisconnect")
public class LaneDisconnectService {
	
	private static Logger logger = Logger.getLogger(LaneDisconnectService.class);
 
	@GET
	@Path("/query")
	@Produces(MediaType.TEXT_XML) 
	public Response getUsers(
		@QueryParam("clerkId") String clerkId,
		@QueryParam("laneId") String laneId) {
		
		
		String output = "<?xml version=\"1.0\"?><lanedisconnect><clerkId>" +clerkId + "</clerkId> " +" <laneId>" +laneId + "</laneId> </lanedisconnect>";
		logger.info("Lane disconnect service Request ::::: clerkId -  "+clerkId+"  laneId - " + laneId);
		logger.info("Lane disconnect service Responce ::::: "+ output);
 
		return Response.status(200).entity(output).build();
		
	}
 
}