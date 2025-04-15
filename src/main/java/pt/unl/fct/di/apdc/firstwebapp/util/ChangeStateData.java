package pt.unl.fct.di.apdc.firstwebapp.util;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData.Estado;

public class ChangeStateData {
    
    public String username;
    public String targetUsername;
    public Estado newState;
    
    public ChangeStateData() {
    }

    public ChangeStateData(String username, String targetUsername, Estado newState) {
        this.username = username;
        this.targetUsername = targetUsername;
        this.newState = newState;
    }
    
    
}
