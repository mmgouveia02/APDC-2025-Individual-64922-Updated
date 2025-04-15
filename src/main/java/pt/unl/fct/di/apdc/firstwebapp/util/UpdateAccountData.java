package pt.unl.fct.di.apdc.firstwebapp.util;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.ProfileType;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

public class UpdateAccountData {

    public String user;      
    public String password;      
    public String targetUsername; 

    public String username;
    public String email;    
    public String fullname;
    public Estado estado;   
    public Role role;       

    public ProfileType profileType;   
    public String phoneNumber;   
    public String idNumber;        
    public String occupation;
    public String workplace;
    public String address;
    public String nif;
    public String photo;


    public UpdateAccountData() {
    }

    
    public UpdateAccountData(String user, String targetUsername) {
        this.user = user;
        this.targetUsername = targetUsername;
    }
}
