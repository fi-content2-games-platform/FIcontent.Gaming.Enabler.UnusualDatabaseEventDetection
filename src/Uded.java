/** \mainpage Unusual Database Event Detection (Uded).

The Unusual Database Event Detection (Uded) is a server monitoring software with email alerts.
*/

/*
further ideas

initialization (slow to not overload server):
- collect stats from existing data
- auto estimate paramters

then: in fixed intervals: check new entries:
- update statistics
- update alarm level
  - if necessary send email 

web server or REST server? provides monitor:
- alarm level (over time)
- statistics (over time)

temporal buffer: (variable: (min,max,avg,stddev), meta data)
- every 10 minutes (1 day)
- every 1 hour (5 days)
- every 6 hours (4 weeks)

meta data: user ID, time of event, IP address


leaderboard example:
- highscore -->max
- highscore -->sum of all points -->over time-->
- time for event with username -->difference--> time since last post -->min
- time for event with IP -->difference--> time since last post -->min


required format in mysql table: event log:
timestamp user-id[](name or ip) message/points/numbers[]

user interface:
- show data: numbers[x] within each (-/user-id[y]) (absolute/dt over time/dt dt over time over time)


search log messages

log message format:
- timestamp
- user id and ip
- key value pair

configuration entry format:
- string: value description
- string: mysql query --> returns a value (for now: single value!) plus message id
  (store min/max/avg/sum per time interval)

configuration
- time interval
*/

import java.io.IOException;
import java.util.Vector;


// web server
import static spark.Spark.*;
import spark.*;


// logging
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
// read info from identity mangager through html/url
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

// json
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;

// base64 conversion
//import sun.misc.BASE64Decoder;
import org.apache.commons.codec.binary.Base64;
import java.util.Arrays;
*/

/** The Unusual Database Event Detection (Uded) is a server monitoring software with email alerts.

In specified intervals, for each specified value in a rule file:
- execute sql query to retrieve a number
- send email if no connection or resulting value outside range

The rule file is a text format with 4 lines per entry:
- description
- query string (e.g. a MySQL command returning a number)
- minimum allowed result value
- maximum allowed result value

Config entries in key = value format:
- key: string, value description
- value: string, mysql query --> returns a value (for now: single value!)
it contains settings such as:
- time interval
- email address
- server settings

*/

public class Uded {
	public static final boolean DEBUG = true; // for verbose debug messages

	static ServerSettings settings;
	static String configFileName = "config.properties";

	static Logger logger;
	static Vector<Rule> rules;

	static DatabaseQuery db;
	static boolean dbProblem; // if there was a problem with the db at the last check

	///////////////////////////////////////////////////////////////////////////////////////

	/** Convert string to integer. In case this fails, the defaultValue is used. */
	public static int toIntDef(String v, int defaultValue) {
		int result = defaultValue;
		try {
			result = Integer.parseInt(v);
		} catch (Exception e) {
		}
		return result;
	}

	/** Convert string to boolean. In case this fails, the defaultValue is used. */
	public static boolean toBoolDef(String v, boolean defaultValue) {
		boolean result = defaultValue;
		try {
			result = Boolean.parseBoolean(v);
		} catch (Exception e) {
		}
		return result;
	}

	/** This should process and return a string that is safe to use in sql inputs, preventing sql injection. */
	public static String toSafeString(String s, int maxLength) {
		if (s == null) return null;

		try {
			s = java.net.URLDecoder.decode(s, "UTF-8"); // to enable spaces
		} catch (Exception e) {return null;};

		int len = s.length();
		if (len > maxLength) len = maxLength;
		StringBuilder res = new StringBuilder(len);
		for (int i=0; i<len; i++) {
			char ch = s.charAt(i);
			if (   ((ch >= 'a') && (ch <= 'z')) // be very careful when changing this! besides ',\,; there are also comment charaters etc.
				|| ((ch >= 'A') && (ch <= 'Z'))
				|| ((ch >= '0') && (ch <= '9'))
				|| (ch == '.') || (ch == ' ')
				|| (ch == '@') || (ch == '_')
				|| (ch == '-') )
			res.append(ch);
		}

		return res.toString();
	}

	/** Perform the check of each entry. */
	public static void performCheck() {
		if (DEBUG) System.out.println("### starting check");
		String alertMessage = "";
		boolean alertsHaveChanged = false;
		boolean lastCheckDbProblem = dbProblem;
		dbProblem = false;

		if (db.connect()) {
			if (DEBUG) System.out.println("connected to database");
		} else {
			if (DEBUG) System.out.println("Connection to database failed");
			dbProblem = true;
			alertMessage += "Connection to database failed.\n";
		}

		// read values from database
		for (Rule rule : rules) { // for all checked values
			if (DEBUG) System.out.print(rule.mysqlQuery + " --> ");
			float v = Float.NaN;
			if (!dbProblem) v = db.getValue(rule.mysqlQuery);

			HistoryBufferEntry h = new HistoryBufferEntry();
			h.v = v;

			if (!Float.isNaN(v)) {
				if (DEBUG) System.out.println("result value: " + v + "  (should be within " + rule.minValue + ".." + rule.maxValue + ")");

				// check value for anomalies
				// unusual value or state, send notification by email
				boolean previousCheck = rule.lastCheckAlert;
				rule.lastCheckAlert = false;
				h.isInRange = true;
				if (v < rule.minValue) {
					alertMessage += (rule.description + " --> " + v + "   triggered alert\n  " + rule.mysqlQuery);
					rule.lastCheckAlert = true;
					h.isInRange = false;
				}
				if (v > rule.maxValue) {
					alertMessage += (rule.description + " --> " + v + "   triggered alert\n  " + rule.mysqlQuery);
					rule.lastCheckAlert = true;
					h.isInRange = false;
				}
				if (rule.lastCheckAlert != previousCheck) alertsHaveChanged = true;
			} else {
				// problem getting value
				if (DEBUG) System.out.println("reading value from database failed");
				alertMessage += "Connection to database failed.\n";
				dbProblem = true;
			}

			rule.histBuff.add(h);
		}

		if (alertMessage.length() == 0) {
			alertMessage = "all back to normal";
		}

		if (!db.close()) dbProblem = true;

		if (dbProblem != lastCheckDbProblem) alertsHaveChanged = true;

		if (alertsHaveChanged) {
			SendEmail.send(settings.emailSender, settings.emailPassword, "marcel.lancelle@inf.ethz.ch", "uded alert", alertMessage);
		}

		if (DEBUG) System.out.println("### end check");
	}

///////////////////////////////////////////////////////////////////////////////////////

	/** Main loop: read config file and rules file, execute checks in regular intervals. */
	public static void main(String[] args) {
		settings = new ServerSettings(configFileName);
		///////////////////

		// init logger (to file)
		logger = Logger.getLogger("Uded");
		FileHandler fh;  

		if (settings.logfile != null) try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler(settings.logfile, 100000, 1, true);
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			// the following statement is used to log any messages
			logger.info("My first log");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("//////////// Unusual Database Event Detection (uded) server started /////////////");  

		rules = new Vector<Rule>();
		if (!Rule.readFile(settings.ruleFileName, rules)) {
			logger.info("rules file could not be read: " + settings.ruleFileName);
		} else {
			if (DEBUG) System.out.println("rules file read\n");
		}

		db = new DatabaseQueryMySQL(settings.url, settings.user, settings.password);

		/////////////////////////////////
		get(new Route("/") {
			@Override
			public Object handle(Request request, Response response) {
				StringBuilder result = new StringBuilder(40);
				result.append("<!DOCTYPE html>\n<html><body>\n<h1>Unusual Database-Event Detection SE - Monitor</h1>\n");

				// nice text output of duration
				String intervalString;
				if (settings.interval < 180) { // if less than 3 minutes
					intervalString = (settings.interval) + " seconds";
				} else {
					if (settings.interval/60 < 180) { // if less than 3 hours
						intervalString = (settings.interval/60) + " minutes";
					} else {
						intervalString = (settings.interval/3600) + " hours";
					}
				}

				result.append("<p>Each bar below represents about " + intervalString + ".</p>");
				for (Rule rule : rules) {
					result.append(rule.statusHTML());
				}
				result.append("</body></html>");

				response.status(200); // 200 ok
				return result.toString();
			}
		});
		/////////////////////////////////

		// main loop
		while (true) {
			// check db state
			performCheck();

			try {
				if (DEBUG) System.out.println("Thread going to sleep...");
				Thread.sleep(settings.interval * 1000); // in milliseconds
				if (DEBUG) System.out.println("\n...thread weaking up");
			} catch(InterruptedException ex) {
				logger.info("sleep was interrupted prematurely"); // sleep was interrupted prematurely (e.g. due to shutdown request), rethrow
				System.out.println("sleep was interrupted prematurely");
				System.exit(0);
			}
		}

	} // main

}
