package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
		
		public String username;
		public String password;
		public String newPassword;
		public String confirmNewPassword;
		
		public ChangePasswordData() {
		}

		public ChangePasswordData(String username, String password, String newPassword,String confirmNewPassword) {
			this.username = username;
			this.password = password;
			this.newPassword = newPassword;
			this.confirmNewPassword = confirmNewPassword;
			
		}


	}
