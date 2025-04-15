package pt.unl.fct.di.apdc.firstwebapp.resources;


import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;
import com.google.cloud.Timestamp;

import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.ProfileType;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;


@Path("/changePassword")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ChangePasswordResource {

	private final Datastore datastore = DatastoreOptions.newBuilder()
			.setProjectId("ind-project-456918")
			.build()
			.getService();

	private static final Logger LOG = Logger.getLogger(ChangePasswordResource.class.getName());

	public ChangePasswordResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(ChangePasswordData data) {
		LOG.fine("Attempt to change password for user: " + data.username);

		if (!data.newPassword.equals(data.confirmNewPassword)) {
			return Response.status(Status.BAD_REQUEST).entity("New passwords do not match.").build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			if (user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
			}

			Estado estado = Estado.valueOf(user.getString("user_estado"));

            if (estado == Estado.DESATIVADA) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Inactive user cannot proceed.").build();
            }
            
			
			String storedPassword = user.getString("user_pwd");
			if (!DigestUtils.sha512Hex(data.password).equals(storedPassword)) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Incorrect current password.").build();
			}

			Entity updatedUser = Entity.newBuilder(user).set("user_pwd", DigestUtils.sha512Hex(data.newPassword))
														.set("user_confirmPwd", data.confirmNewPassword)
														.build();

			txn.put(updatedUser);
			LOG.info("Password changed for user: " + data.username);
			txn.commit();
			return Response.ok("Password changed").build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
