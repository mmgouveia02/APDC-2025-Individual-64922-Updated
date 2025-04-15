package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterData {

	public String email;
    public String username;
    public String fullname;
    public String phoneNumber;
    public String password;
    public String confirmPwd;
    public ProfileType profileType;

	// Opcionais
    public String idNumber;
	public Role role;
    public String workplace;
    public String occupation;
    public String address;
    public String nif;
    public Estado estado;
    public String photo;

    //Enum de roles
	public enum Role {
		ENDUSER, BACKOFFICE, ADMIN, PARTNER;
	}

    //Enum de estados
	public enum Estado {
		ATIVADA, SUSPENSA, DESATIVADA;
	}

    //Enum de tipos de perfil
	public enum ProfileType {
		PUBLIC, PRIVATE
	}

	public RegisterData() {
	}

	public RegisterData(String email, String username, String fullname, String phoneNumber, String password, String confirmPwd, ProfileType profileType) {
		this.email = email;
        this.username = username;
        this.fullname = fullname;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.confirmPwd = confirmPwd;
        this.profileType = profileType;
		this.role = Role.ENDUSER; // default role
		this.estado = Estado.DESATIVADA; // default estado

	}

	public boolean isValidRegistration() {
		boolean isValidEmail = validateEmail(email);
        boolean isValidPhoneNumber = validatePhoneNumber(phoneNumber);
        boolean isValidPassword = validatePassword(password);

        System.out.println("Email valid: " + isValidEmail);
        System.out.println("Phone valid: " + isValidPhoneNumber);
        System.out.println("Password valid: " + isValidPassword);
        
		return isValidEmail && isValidPhoneNumber && isValidPassword; 
	}

	private boolean validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        String phoneRegex = "^\\+?[0-9]{9,15}$"; 
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    private boolean validatePassword(String password) {
    
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
