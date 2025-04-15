package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.ListData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.ProfileType;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Transaction;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ListUsersResource {

    private static final String NOT_DEFINED = "NOT DEFINED";
    
    private static final Logger LOG = Logger.getLogger(ChangeRoleResource.class.getName());
    private final Datastore datastore = DatastoreOptions.newBuilder()
			.setProjectId("ind-project-456918")
			.build()
			.getService();


    public ListUsersResource() {
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doListUsers(ListData data) {
        LOG.fine("List users attempt by user: " + data.username);

        Transaction txn = datastore.newTransaction();

        try {

            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            //Verificar se user existe
            if (user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User performing operation not found.").build();
            }

            Role userRole = Role.valueOf(user.getString("user_role"));

            if(userRole == Role.PARTNER) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("User does not have permission to list users.").build();
            }

            Query query = Query.newEntityQueryBuilder().setKind("User").build();
            QueryResults<Entity> results = datastore.run(query);

            List<Map<String, Object>> users = new ArrayList<>();

            while(results.hasNext()) {
                Entity e = results.next();
                Role targetUserRole = Role.valueOf(e.getString("user_role"));
                Estado targetUserState = Estado.valueOf(e.getString("user_estado"));
                ProfileType targetUserType = ProfileType.valueOf(e.getString("user_profileType"));
                Map<String, Object> userDetails = new HashMap<>();

                //ENDUSER - Contas ENDUSER, com estado ATIVADA e perfil PUBLICO
                if(userRole == Role.ENDUSER && targetUserRole == Role.ENDUSER && targetUserState == Estado.ATIVADA && targetUserType == ProfileType.PUBLIC) {
                    userDetails.put("username", e.getString("user_username"));
                    userDetails.put("email", e.getString("user_email"));
                    userDetails.put("fullname", e.getString("user_fullname"));
                }
                
                //BACKOFFICE - Contas ENDUSER, independente do estado e perfil
                 else if(userRole == Role.BACKOFFICE && targetUserRole == Role.ENDUSER) {
                    userDetails.put("username", e.getString("user_username"));
                    userDetails.put("email", e.getString("user_email"));
                    userDetails.put("fullname", e.getString("user_fullname"));
                    userDetails.put("phoneNumber", e.getString("user_phoneNumber"));
                    userDetails.put("profileType", e.getString("user_profileType"));
                    userDetails.put("role", e.getString("user_role"));
                    userDetails.put("estado", e.getString("user_estado"));
                    userDetails.put("idNumber", e.getString("user_idNumber") != "" ? e.getString("user_idNumber") : NOT_DEFINED);
                    userDetails.put("workplace", e.getString("user_workplace") != "" ? e.getString("user_workplace") : NOT_DEFINED);
                    userDetails.put("occupation", e.getString("user_occupation") != "" ? e.getString("user_occupation") : NOT_DEFINED);
                    userDetails.put("address", e.getString("user_address") != "" ? e.getString("user_address") : NOT_DEFINED);
                    userDetails.put("nif", e.getString("user_nif") != "" ? e.getString("user_nif") : NOT_DEFINED);

                }
                
                //ADMIN - Qualquer conta, independente do estado e perfil
                else if(userRole == Role.ADMIN) {
                    userDetails.put("username", e.getString("user_username"));
                    userDetails.put("email", e.getString("user_email"));
                    userDetails.put("fullname", e.getString("user_fullname"));
                    userDetails.put("phoneNumber", e.getString("user_phoneNumber"));
                    userDetails.put("profileType", e.getString("user_profileType"));
                    userDetails.put("role", e.getString("user_role"));
                    userDetails.put("estado", e.getString("user_estado"));
                    userDetails.put("idNumber", e.getString("user_idNumber") != "" ? e.getString("user_idNumber") : NOT_DEFINED);
                    userDetails.put("workplace", e.getString("user_workplace") != "" ? e.getString("user_workplace") : NOT_DEFINED);
                    userDetails.put("occupation", e.getString("user_occupation") != "" ? e.getString("user_occupation") : NOT_DEFINED);
                    userDetails.put("address", e.getString("user_address") != "" ? e.getString("user_address") : NOT_DEFINED);
                    userDetails.put("nif", e.getString("user_nif") != "" ? e.getString("user_nif") : NOT_DEFINED);
                }
                
                
                users.add(userDetails);
            }

            txn.commit();
            return Response.ok(users).build();

        } catch (Exception e) {
            LOG.warning("Error listing users: " + e.getMessage());
            return Response.serverError().entity("Error listing users.").build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
        
    }
}
