package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

@Path("/changeRole")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ChangeRoleResource {

    private static final Logger LOG = Logger.getLogger(ChangeRoleResource.class.getName());
    private final Datastore datastore = DatastoreOptions.newBuilder()
			.setProjectId("ind-project-456918")
			.build()
			.getService();

    public ChangeRoleResource() {
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(ChangeRoleData data) {
        LOG.fine("Attempt to change role of user: " + data.targetUsername + " by " + data.username);

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
           

            if (userRole == Role.ENDUSER || userRole == Role.PARTNER) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("User does not have permission to change roles.").build();
            }

            Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.targetUsername);
            Entity targetUser = txn.get(targetKey);

            //Verificar se o user alvo existe
            if (targetUser == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Target user not found.").build();
            }

            Role newRole = data.newRole;

            // Verificar se o novo role é válido
            if(!(newRole == Role.ENDUSER || newRole == Role.PARTNER || newRole == Role.BACKOFFICE || newRole == Role.ADMIN)) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Invalid role.").build();
            }

            
            
            if (userRole == Role.BACKOFFICE) {
                //BACKOFFICE só pode atribuir ENDUSER ou PARTNER
                if (!(newRole == Role.ENDUSER || newRole == Role.PARTNER)) {
                    txn.rollback();
                    return Response.status(Status.FORBIDDEN)
                                   .entity("BACKOFFICE can only assign ENDUSER or PARTNER.")
                                   .build();
                }
                // Verificar se o utilizador alvo é ENDUSER ou PARTNER
                Role targetCurrentRole = Role.valueOf(targetUser.getString("user_role"));
                if (!(targetCurrentRole == Role.ENDUSER || targetCurrentRole == Role.PARTNER)) {
                    txn.rollback();
                    return Response.status(Status.FORBIDDEN)
                                   .entity("BACKOFFICE can only change roles of ENDUSER/PARTNER accounts.")
                                   .build();
                }
                
                if(newRole == targetCurrentRole) {
                	txn.rollback();
                	 return Response.status(Status.BAD_REQUEST)
                             .entity("User " + data.targetUsername + " is already a " + data.newRole)
                             .build(); 
                }
            }

            // Se for ADMIN, pode atribuir livremente qualquer role

            Entity updatedUser = Entity.newBuilder(targetUser)
                                       .set("user_role", newRole.toString())
                                       .build();

            txn.update(updatedUser);
            txn.commit();
            LOG.info("Role changed for user: " + data.targetUsername + " to " + newRole);
            return Response.ok("User role changed successfully.").build();

        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

}
