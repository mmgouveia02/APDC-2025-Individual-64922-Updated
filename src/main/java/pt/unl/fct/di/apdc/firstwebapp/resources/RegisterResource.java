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


import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.ProfileType;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;


@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	
	private final Datastore datastore = DatastoreOptions.newBuilder()
				.setProjectId("ind-project-456918")
				.build()
				.getService();

    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
    

    public RegisterResource() {	
    }

    @POST
    @Path("/") 
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doRegistration(RegisterData data) {
        LOG.fine("Attempt to register user: " + data.username);


		if (!data.isValidRegistration()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}

        if (!data.password.equals(data.confirmPwd)) {
            return Response.status(Status.BAD_REQUEST).entity("Passwords do not match.").build();
        }
        
    
        Transaction txn = datastore.newTransaction(); 

        try {

            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            //Verifica se o utilizador já existe
            if (user != null) {
                txn.rollback();
                LOG.warning("User " + data.username + " already exists.");
                return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
            } else {
            	
            	user = Entity.newBuilder(userKey)
            		    .set("user_email", data.email)
            		    .set("user_username", data.username)
            		    .set("user_fullname", data.fullname)
            		    .set("user_phoneNumber", data.phoneNumber)
            		    .set("user_pwd", DigestUtils.sha512Hex(data.password))
            		    .set("user_confirmPwd", data.confirmPwd)
            		    .set("user_profileType", data.profileType != null ? data.profileType.toString() : ProfileType.PUBLIC.toString())
            		    .set("user_role", data.role != null ? data.role.toString() : Role.ENDUSER.toString())
            		    .set("user_estado", data.estado != null ? data.estado.toString() : Estado.DESATIVADA.toString())
            		    .set("user_creation_time", Timestamp.now())
            		    // Opcionais
            		    .set("user_idNumber", data.idNumber != null ? data.idNumber : "")
            		    .set("user_workplace", data.workplace != null ? data.workplace : "")
            		    .set("user_occupation", data.occupation != null ? data.occupation : "")
            		    .set("user_address", data.address != null ? data.address : "")
            		    .set("user_nif", data.nif != null ? data.nif : "")
            		    .set("user_photo", data.photo != null ? data.photo : "")
            		    .build();

                        
                txn.put(user);
                txn.commit(); 

                LOG.info("User " + data.username + " registered successfully.");
                return Response.ok("User registered successfully.").build();
            }
            
            } catch(Exception e) {
                LOG.severe("Registration error: " + e.getMessage());
                e.printStackTrace(); // útil para ver a stack no terminal
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                               .entity("Something went wrong during registration.").build();
            
        } finally {
            if (txn.isActive()) {
                txn.rollback(); 
            }
    }
}

}
