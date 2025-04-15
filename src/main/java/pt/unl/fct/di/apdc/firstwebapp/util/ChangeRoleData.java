package pt.unl.fct.di.apdc.firstwebapp.util;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Role;

public class ChangeRoleData {
    
    public String username;
    public String targetUsername;
    public Role newRole;
    
    public ChangeRoleData() {
    }

    public ChangeRoleData(String targetUsername, String username, Role newRole) {
        this.username = username;
        this.targetUsername = targetUsername;
        this.newRole = newRole;
    }
    
}
