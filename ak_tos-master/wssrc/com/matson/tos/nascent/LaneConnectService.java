package com.matson.tos.nascent;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;  
import javax.ws.rs.core.MediaType; 
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.matson.tos.processor.MdbAcetsMessageProcessor;
 
@Path("/laneconnect")
public class LaneConnectService {
	
	private static Logger logger = Logger.getLogger(LaneConnectService.class);
 
	@GET
	@Path("/query")
	@Produces(MediaType.TEXT_XML) 
	public Response getUsers(
		@QueryParam("clerkId") String clerkId,
		@QueryParam("laneId") String laneId) {
		
		
		String output = "<?xml version=\"1.0\"?><laneconnect><clerkId>" +clerkId + "</clerkId> " +" <laneId>" +laneId + "</laneId> </laneconnect>";
		logger.info("Lane connect service Request ::::: clerkId -  "+clerkId+"  laneId - " + laneId);
		logger.info("Lane connect service Responce ::::: "+ output);
		
		
 
		return Response.status(200).entity(output).build();
		
	}
 
}