package pt.unl.fct.di.apdc.firstwebapp.util;

public class RemoveAccountData {

    public String username;
    public String targetUsername;
    public String targetEmail;
    
    public RemoveAccountData() {
    }

    public RemoveAccountData(String username, String targetUsername, String targetEmail) {
        this.username = username;
        this.targetUsername = targetUsername;
        this.targetEmail = targetEmail;
    }
}
