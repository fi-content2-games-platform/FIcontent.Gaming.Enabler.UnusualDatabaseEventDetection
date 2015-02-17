// configuration file
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/***************** example configuration file *************************

# escape the characters #, !, =, and : with a preceding backslash

# (local) address of database
url = jdbc\:mysql\://localhost\:3306/mygame

# database user/password
user = root
password = abc123

# logfile is optional
logfile = log.txt

# optional: social network url and database name
#socialnetwork = http\://couchdb.simple-url.com\:5984/pixelpark/

***********************************************************************/


class ServerSettings {
	final String url; // of mysql database, e.g. jdbc:mysql://localhost:3306/mygame
	final String user;
	final String password;
	final String logfile; // name of logfile

	final int interval; // of checking database in seconds

	final String emailSender;
	final String emailPassword;

	final String ruleFileName;

	public ServerSettings(String configFileName) {
		Properties prop = new Properties();
		//load a properties file
		try {
			prop.load(new FileInputStream(configFileName));
		} catch (IOException ex) {
			System.out.println("error reading config file: " + configFileName);
			System.exit(1);
		}

		//get the property values and print them
		url = prop.getProperty("url"); // jdbc:mysql://localhost:3306/mygame";
		user = prop.getProperty("user");
		password = prop.getProperty("password");
		logfile = prop.getProperty("logfile");
		interval = Integer.parseInt(prop.getProperty("interval"));
		emailSender = prop.getProperty("emailSender");
		emailPassword = prop.getProperty("emailPassword");
		ruleFileName = prop.getProperty("ruleFileName");

		if (Uded.DEBUG) {
			System.out.println("read these config values:");
			System.out.println("url: " + url);
			System.out.println("user: " + user);
			System.out.println("password: " + password);
			System.out.println("logfile: " + logfile);
			System.out.println("interval: " + interval);
			System.out.println("emailSender: " + emailSender);
			System.out.println("emailPassword: " + emailPassword);
			System.out.println("ruleFileName: " + ruleFileName);
		}
	}
}