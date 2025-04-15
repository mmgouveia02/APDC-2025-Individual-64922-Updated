package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;
import pt.unl.fct.di.apdc.firstwebapp.util.UpdateAccountData;

import com.google.cloud.datastore.*;

@Path("/update")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UpdateAccountResource {

	private final Datastore datastore = DatastoreOptions.newBuilder()
			.setProjectId("ind-project-456918")
			.build()
			.getService();
	private static final Logger LOG = Logger.getLogger(UpdateAccountResource.class.getName());
    
    public UpdateAccountResource() {	
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(UpdateAccountData data) {
        LOG.fine("Attempt to update user: " + data.targetUsername);

        Transaction txn = datastore.newTransaction(); 

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.user);
            Entity user = txn.get(userKey);

            //Verificar se user existe
            if (user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
            }

            Role userRole = Role.valueOf(user.getString("user_role"));
            Estado userEstado = Estado.valueOf(user.getString("user_estado"));

            if(userRole == Role.PARTNER) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User does not have permission to modify another user.").build();
            }

            if(userEstado != Estado.ATIVADA) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User is not ACTIVADA.").build();
            }

            Key targetUserKey = datastore.newKeyFactory().setKind("User").newKey(data.targetUsername);
			Entity targetUser = txn.get(targetUserKey);

            if (targetUser == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Target user not found.").build();
            }

            Entity.Builder updatedUserBuilder = Entity.newBuilder(targetUser);
			Role targetUserRole = Role.valueOf(targetUser.getString("user_role"));

            boolean canUpdate = false;

            if(userRole == Role.ENDUSER && userEstado == Estado.ATIVADA && data.user.equals(data.targetUsername)) {
                canUpdate = true;
            } else if(userRole == Role.BACKOFFICE && userEstado == Estado.ATIVADA && (targetUserRole == Role.ENDUSER || targetUserRole == Role.PARTNER)) {
                canUpdate = true;

                if(data.fullname != null) {
                    updatedUserBuilder.set("user_fullname", data.fullname);
                }

                if(data.estado != null) {
                    updatedUserBuilder.set("user_estado", data.estado.toString());
                }
                if(data.role != null) {
                    updatedUserBuilder.set("user_role", data.role.toString());
                }
            } else if(userRole == Role.ADMIN) {
                canUpdate = true;

                if(data.username != null) {
                    updatedUserBuilder.set("user_username", data.username);
                }
                if(data.email != null) {
                    updatedUserBuilder.set("user_email", data.email);
                }
                if(data.fullname != null) {
                    updatedUserBuilder.set("user_fullname", data.fullname);
                }

                if(data.estado != null) {
                    updatedUserBuilder.set("user_estado", data.estado.toString());
                }
                if(data.role != null) {
                    updatedUserBuilder.set("user_role", data.role.toString());
                }
            }            

            if(canUpdate){

                if(data.phoneNumber != null) {
                    updatedUserBuilder.set("user_phoneNumber", data.phoneNumber);
                }
                if(data.profileType != null) {
                    updatedUserBuilder.set("user_profileType", data.profileType.toString());
                }
                if(data.idNumber != null) {
                    updatedUserBuilder.set("user_idNumber", data.idNumber);
                }
                if(data.workplace != null) {
                    updatedUserBuilder.set("user_workplace", data.workplace);
                }
                if(data.occupation != null) {
                    updatedUserBuilder.set("user_occupation", data.occupation);
                }
                if(data.address != null) {
                    updatedUserBuilder.set("user_address", data.address);
                }  
                if(data.nif != null) {
                    updatedUserBuilder.set("user_nif", data.nif);
                }
                if(data.photo != null) {
                    updatedUserBuilder.set("user_photo", data.photo);
                }
                
                Entity updatedUser = updatedUserBuilder.build();
				txn.put(updatedUser);
				txn.commit();
				return Response.ok("User modified").build();
			} else {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("You do not have permission to modify this user.")
						.build();
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}