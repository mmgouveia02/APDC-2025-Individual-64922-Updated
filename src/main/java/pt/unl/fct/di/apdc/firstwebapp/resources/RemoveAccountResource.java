package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import org.checkerframework.checker.units.qual.t;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.firstwebapp.util.RemoveAccountData;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;


@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class RemoveAccountResource {
	
	/*
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.newBuilder()
			.setProjectId("ind-project-456918")
			.build()
			.getService();


	public RemoveAccountResource() {
	}

    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(RemoveAccountData data) {
        LOG.fine("Attempt to remove user: " + data.targetUsername);

        Transaction txn = datastore.newTransaction();

        try {

            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            //Verificar se user existe
            if (user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
            }
            Role role = Role.valueOf(user.getString("user_role"));

            //Verificar se o utilizador tem permissões para remover outro utilizador
            if (role == Role.PARTNER || role == Role.ENDUSER) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("User can not remove an account.").build();
            }

            Key targetUserKey = datastore.newKeyFactory().setKind("User").newKey(data.targetUsername);
            Entity targetUser = txn.get(targetUserKey);

            //Verificar se o utilizador a remover existe
            if (targetUser == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Target user not found.").build();
            }

            Role targetUserRole = Role.valueOf(targetUser.getString("user_role"));

            //BACKOFFICE nao pode remover - ADMIN ou BACKOFFICE
            if (role == Role.BACKOFFICE && targetUserRole == Role.ADMIN) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("Backoffice account cannot remove an admin account.").build();
            } 
            if (role == Role.BACKOFFICE && targetUserRole == Role.BACKOFFICE) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("Backoffice account cannot remove a backoffice account.").build();
            } 

            //Sobra ADMIN (pode remover tudo) ou BACKOFFICE (pode remover ENDUSER ou PARTNER)
            txn.delete(targetUserKey);
            LOG.info("User removed: " + data.targetUsername);
            txn.commit();
            return Response.ok("User removed").build();

        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
	

