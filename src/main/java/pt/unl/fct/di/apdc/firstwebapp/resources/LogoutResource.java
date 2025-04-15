package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.LogoutData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
	private final Datastore datastore = DatastoreOptions.newBuilder()
			.setProjectId("ind-project-456918")
			.build()
			.getService();

	public LogoutResource() {
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")

	public Response doLogout(LogoutData data) {

		Transaction txn = datastore.newTransaction();
		try {

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = datastore.get(userKey);

			if (user == null) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("User not found .").build();
			}
			Estado userStatus = Estado.valueOf(user.getString("user_estado"));

			if (userStatus == Estado.DESATIVADA) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User was not logged in.").build();
			}

			Entity updatedUser = Entity.newBuilder(user).set("user_estado", Estado.DESATIVADA.toString())
					.remove("user_token_expiration").remove("user_token").build();

			txn.put(updatedUser);

			txn.commit();

			LOG.info("User " + " logged out successfully.");
			return Response.ok("Logout successful.").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();

			}
		}
	}
}
