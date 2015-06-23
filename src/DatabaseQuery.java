/** abstract class for database access
 */

public abstract class DatabaseQuery {
	/** connect to database */
	public DatabaseQuery() {}
	abstract public boolean connect();
	/** execute queryString and return numeric value */
	abstract public float getValue(String queryString);
	/** close connection to database */
	abstract public boolean close();
}
