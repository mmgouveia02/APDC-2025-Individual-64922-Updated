package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import java.util.Map;
import java.sql.Time;
import java.util.HashMap;

import jakarta.ws.rs.Consumes;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;


import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;
import com.google.cloud.Timestamp;


import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	/*
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore ds = DatastoreOptions.newBuilder()
		      .setProjectId("ind-project-456918")
		      .build()
		      .getService();
	
	private final Gson g = new Gson();
	
	public LoginResource() {	
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);

		if (data.username == null || data.password == null) {
			return Response.status(Status.BAD_REQUEST).entity(g.toJson("Missing or wrong parameter.")).build();
		}

		Transaction txn = ds.newTransaction(); //Inicio transação
		
		try {
			
			Key userKey = ds.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = ds.get(userKey);

			if(user != null) {
				String hashedPWD = (String) user.getString("user_pwd");

				 if(hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
					
					Estado userEstado = Estado.valueOf(user.getString("user_estado"));
					if (userEstado == Estado.DESATIVADA) {

						AuthToken token = new AuthToken(data.username, user.getString("user_role"));

					    user = Entity.newBuilder(user).set("user_token", token.tokenID)
							.set("user_estado", Estado.ATIVADA.toString())
							.set("user_token_expiration", token.expirationData)
							.set("user_login_time", Timestamp.now())
							.build();
						ds.update(user);

						LOG.info("User " + data.username + " logged in successfully.");

						Key tokenKey = ds.newKeyFactory().setKind("AuthToken").newKey(token.username);
						Entity tokenEntity = Entity.newBuilder(tokenKey)
								.set("username", token.username)
								.set("role", token.role)
								.set("tokenID", token.tokenID)
								.set("creationData", token.creationData)
								.set("expirationData", token.expirationData)
								.build();

						


						ds.put(tokenEntity);
						txn.commit();


						
						return Response.ok().entity(g.toJson(token)).build();

					 } else {
						LOG.warning("Account already activated for user: " + data.username);
						txn.rollback();
						return Response.status(Status.FORBIDDEN).entity(g.toJson("User account is already activated.")).build();
					 }
				} else {
					LOG.warning("Wrong password for username: " + data.username);
					txn.rollback();
					return Response.status(Status.FORBIDDEN).entity(g.toJson("Wrong password.")).build();
				}

			} else {
				LOG.warning("Failed login attempt for username: " + data.username);
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity(g.toJson("Username does not exist.")).build();
			}

		} catch (Exception e) {
			LOG.severe(e.getMessage());
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}	
	}		

}
