package exo.rest.service;



import javax.ws.rs.*;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

import javax.ws.rs.core.*;
import javax.jcr.Node;

import javax.jcr.Session;

import javax.ws.rs.GET;

import javax.ws.rs.Path;

import javax.ws.rs.PathParam;

import org.exoplatform.services.jcr.RepositoryService;

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;

import javax.annotation.security.RolesAllowed;


import org.exoplatform.commons.utils.ListAccess;

import org.exoplatform.container.ExoContainer;

import org.exoplatform.container.ExoContainerContext;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;

import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

import org.exoplatform.services.security.Identity;

import org.exoplatform.services.security.IdentityRegistry;

import org.json.JSONArray;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;


/**

 * Rest User Service!

 */

@Path("/demo")


public class RestUserService implements ResourceContainer {


    private static final Log log = ExoLogger.getLogger(RestUserService.class);

    private static final String LOGIN_HISTORY_HOME = "exo:LoginHistoryHome";
    private static final String LAST_LOGIN_TIME = "exo:LoginHisSvc_lastLogin";
    private Session getSession(SessionProvider sessionProvider) throws Exception {
        RepositoryService repositoryService_ = (RepositoryService) ExoContainerContext.getCurrentContainer()

                .getComponentInstanceOfType(RepositoryService.class);
        ManageableRepository currentRepo = repositoryService_.getCurrentRepository();
        return sessionProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);
    }

    @GET

    @Path("/hello/{name}")

    @RolesAllowed({"administrators"})

    public String hello(@PathParam("name")

                                String name) {

        return "Hello " + name;

    }


    @GET

    @Path("/listusers/{offset}")

    public Response getListUserName(@Context  SecurityContext sc,@PathParam("offset") Integer offset) {

        JSONArray list = new JSONArray();

        JSONObject jsonObject = new JSONObject();

        String groupToCheck = "/platform/administrators";

        CacheControl cacheControl = new CacheControl();       cacheControl.setNoCache(true);

        cacheControl.setNoStore(true);



        if (sc.getUserPrincipal() == null || !this.isMemberOf(sc.getUserPrincipal().getName(), groupToCheck)) {


            jsonObject.put("rights","NOT-ALLOWED");

            list.put(jsonObject);


        } else {


            OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()

                    .getComponentInstanceOfType(OrganizationService.class);

            UserHandler userHandler = organizationService.getUserHandler();

            try {

                ListAccess<User> allUsers = userHandler.findAllUsers();



                if(offset == null || offset < 0)

                    offset = 0;

                int limit = 1000;

                int total = limit + offset;

                int totalUsers = allUsers.getSize();



                if(offset < totalUsers && total > totalUsers){

                    total = totalUsers;

                }

                User[] users = null;


                for (int i = offset; i < total; i++) {

                    users = allUsers.load(i,1);

                    jsonObject = new JSONObject();

                    jsonObject.put("username", users[0].getUserName());

                    list.put(jsonObject);

                }

            } catch (Exception e) {

                // TODO Auto-generated catch block

                e.printStackTrace();

            }



        }



        return Response.ok(list.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();


    }

    @GET
    @Path("display/user")
    public Response DisplayUser(@Context  SecurityContext sc){
        JSONArray list = new JSONArray();

        JSONObject jsonObject = new JSONObject();

        String groupToCheck = "/platform/administrators";

        CacheControl cacheControl = new CacheControl();       cacheControl.setNoCache(true);

        cacheControl.setNoStore(true);


        if (sc.getUserPrincipal() == null || !this.isMemberOf(sc.getUserPrincipal().getName(), groupToCheck)) {


            jsonObject.put("rights","NOT-ALLOWED");

            list.put(jsonObject);


        } else {


            OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()

                    .getComponentInstanceOfType(OrganizationService.class);

            UserHandler userHandler = organizationService.getUserHandler();
            User[] users = null;


            try {

                ListAccess<User> allUsers = userHandler.findAllUsers();
                for (int i = 0; i < allUsers.getSize(); i++) {
                    users = allUsers.load(i,1);

                    jsonObject = new JSONObject();

                    jsonObject.put("username", users[0].getUserName());

                    list.put(jsonObject);
                }
            }
         catch (Exception e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }



    }



        return Response.ok(list.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();



}
//@RolesAllowed("administrator")
@POST
@Path("/create/user")
public Response CreateUser(@Context  SecurityContext sc, UserImpl newuser)
{
    CacheControl cacheControl = new CacheControl();       cacheControl.setNoCache(true);

    cacheControl.setNoStore(true);


        OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()

                .getComponentInstanceOfType(OrganizationService.class);

        UserHandler userHandler = organizationService.getUserHandler();

        try {

            User user = userHandler.findUserByName(newuser.getUserName(),UserStatus.ANY);
            if(user != null)  return Response.noContent().build();
            Query query = new Query();
            query.setEmail(newuser.getEmail());
            if(userHandler.findUsersByQuery(query).getSize()>0)
                return Response.noContent().build();


            userHandler.createUser(newuser, true);


} catch (Exception e) {
            e.printStackTrace();
        }

    return Response.ok("User created").build();
}

@POST
@Path("/update/user/{id}")
public Response UpdateUser (@Context UriInfo uriInfo, @PathParam("id") String id  ,UserImpl updated_user) {

    CacheControl cacheControl = new CacheControl();       cacheControl.setNoCache(true);

    cacheControl.setNoStore(true);


    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()

            .getComponentInstanceOfType(OrganizationService.class);

    UserHandler userHandler = organizationService.getUserHandler();

    try {
        User user = userHandler.findUserByName(id,UserStatus.ANY);
        if(user != null)
        {
            userHandler.saveUser(updated_user,true);
            return Response.ok("User updated").build();
        }


        } catch (Exception e) {
        e.printStackTrace();
    }

    return Response.ok("DONE").build();
}


@DELETE
@Path("delete/user/{id}")
public Response DeleteUser(@Context UriInfo uriInfo, @PathParam("id") String id)
{

    CacheControl cacheControl = new CacheControl();       cacheControl.setNoCache(true);

    cacheControl.setNoStore(true);
    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()

            .getComponentInstanceOfType(OrganizationService.class);

    UserHandler userHandler = organizationService.getUserHandler();
    try {

        User u = userHandler.findUserByName(id,UserStatus.ANY);
        if(u != null)  {
            userHandler.removeUser(id, false);
            return Response.ok("User with the following id "+id+" has been deleted.").build();
        }

        else{
            return Response.ok("User with the following id "+id+" does not exist.").build();
        }
    }catch (Exception e) {
        e.printStackTrace();
    }
    return Response.ok("DONE!").build();
}

@GET
@Path("manage/userstatus/{id}")
public Response UserStatus (@Context UriInfo uriInfo, @PathParam("id")  String id, UserImpl user)
{
    CacheControl cacheControl = new CacheControl();       cacheControl.setNoCache(true);

    cacheControl.setNoStore(true);
    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()

            .getComponentInstanceOfType(OrganizationService.class);

    UserHandler userHandler = organizationService.getUserHandler();
    try {

        User u = userHandler.findUserByName(id,UserStatus.ANY);
        if(!u.getUserName().equals("root") && u.isEnabled()){
            userHandler.setEnabled(id,false, false);

        }
        else{
            userHandler.setEnabled(id,true,true);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return Response.ok("Done").build();
}

@GET
@Path("/manage/userstatus/bylastlogtime/{time}")
public Response UserStatutsByLastLogTime(@Context UriInfo uriInfo, @PathParam("time") long time)
{

    SessionProvider sProvider = SessionProvider.createSystemProvider();

    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()

            .getComponentInstanceOfType(OrganizationService.class);

    UserHandler userHandler = organizationService.getUserHandler();
    User[] users = null;
    try {

        Date d = new Date(time);
        ListAccess<User> allUsers = userHandler.findAllUsers();

        Session session = getSession(sProvider);

        Node homeNode = session.getRootNode().getNode(LOGIN_HISTORY_HOME);
        for (int i = 0; i < allUsers.getSize(); i++) {

            users = allUsers.load(i,allUsers.getSize());
            Node userNode = homeNode.getNode(users[i].getUserName());
            long lastLogin = userNode.getProperty(LAST_LOGIN_TIME).getLong();

            if (!users[i].getUserName().equals("root") && lastLogin <= time)
            {log.info("User " + users[i] + " Last login: " + (new Date(lastLogin)) );

            }
            else
                log.info("No users have logged in before " + (new Date(time)) );
            sProvider.close();
        }

}catch (Exception e) {
        e.printStackTrace();
    }
    return Response.ok("DONE").build();
}

    private boolean isMemberOf(String username, String role) {

        ExoContainer container = ExoContainerContext.getCurrentContainer();

        IdentityRegistry identityRegistry = (IdentityRegistry) container.getComponentInstanceOfType(IdentityRegistry.class);

        Identity identity = identityRegistry.getIdentity(username);

        return identity.isMemberOf(role);

    }


}