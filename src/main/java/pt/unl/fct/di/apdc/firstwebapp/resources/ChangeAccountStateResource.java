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

import pt.unl.fct.di.apdc.firstwebapp.util.ChangeStateData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

@Path("/changeState")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeAccountStateResource {

    private static final Logger LOG = Logger.getLogger(ChangeAccountStateResource.class.getName());
    private final Datastore datastore = DatastoreOptions.newBuilder()
			.setProjectId("ind-project-456918")
			.build()
			.getService();

    public ChangeAccountStateResource() {
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeState(ChangeStateData data) {
        LOG.fine("Attempt to change state of user: " + data.targetUsername + " by " + data.username);

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
            Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.targetUsername);
            Entity targetUser = txn.get(targetKey);

            //Verificar se o user alvo existe
            if (targetUser == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Target user not found.")
                               .build();
            }

            
            Estado newState = data.newState;

            if(!(newState == Estado.ATIVADA || newState == Estado.SUSPENSA || newState == Estado.DESATIVADA)) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Invalid state.")
                               .build();
            }

            if(targetUser.getString("user_estado").equals(newState.toString())) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User already in the requested state.")
                               .build();
            }

            if (userRole == Role.ENDUSER || userRole == Role.PARTNER) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN)
                               .entity("No permission to change account state.")
                               .build();
            }
            if (userRole == Role.BACKOFFICE) {
                // Se o newState não for ATIVADA ou DESATIVADA, não pode
                if (!(newState == Estado.ATIVADA || newState == Estado.DESATIVADA)) {
                    txn.rollback();
                    return Response.status(Status.FORBIDDEN)
                                   .entity("BACKOFFICE can only change state between ATIVADA and DESATIVADA.")
                                   .build();
                }
            }
            
            Entity updatedUser = Entity.newBuilder(targetUser)
                                       .set("user_estado", newState.toString())
                                       .build();

            txn.update(updatedUser);
            txn.commit();
            LOG.info("Account state changed for user: " + data.targetUsername + " to " + newState);
            return Response.ok("Account state changed successfully.").build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
