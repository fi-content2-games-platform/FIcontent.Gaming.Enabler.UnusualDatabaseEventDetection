// mysql
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Blob;

// logging
import java.util.logging.Level;

public class DatabaseQueryMySQL extends DatabaseQuery {
	String address;
	String username;
	String password;

	Connection con;
	Statement st;
	ResultSet rs;

	public DatabaseQueryMySQL(String address, String username, String password) {
		this.address = address;
		this.username = username;
		this.password = password;
		con = null;
		st = null;
		rs = null;
	}

	public boolean connect() {
		try {
			con = DriverManager.getConnection(address, username, password);
		} catch (SQLException ex) {
			Uded.logger.log(Level.SEVERE, ex.getMessage(), ex);
			return false;
		}
		return true;
	}

	public float getValue(String queryString) {
		try {
			st = con.createStatement();
			//String qustr = "SELECT MAX(highscore) FROM mygame.game1"; // AVG MIN MAX SUM, COUNT (anzahl der Einträge)
			rs = st.executeQuery(queryString);
			ResultSetMetaData rsmd = rs.getMetaData();
			if (rs.next()) { // only first position
				return Float.parseFloat(rs.getString(1)); // first (and only) column is number 1
			}
		} catch (SQLException ex) {
			Uded.logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
		return Float.NaN;
	}

	public boolean close() {
		try {
			if (st != null) {
				st.close();
				st = null;
			}
			if (con != null) {
				con.close();
				con = null;
			}
			rs = null;
			return true;
		} catch (SQLException ex) {
			Uded.logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
		return false;
	}
}