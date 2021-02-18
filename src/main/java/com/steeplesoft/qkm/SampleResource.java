package com.steeplesoft.qkm;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/sample")
public class SampleResource {
    @GET
    @Path("admin")
    @RolesAllowed("admin")
    public String admin() {
        return "admin";
    }

    @GET
    @Path("user")
    @RolesAllowed("user")
    public String user() {
        return "user";
    }
}
