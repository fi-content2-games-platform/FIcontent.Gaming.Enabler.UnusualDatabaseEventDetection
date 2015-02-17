// abstract class (use one of the derived classes)

public abstract class DatabaseQuery {
	public DatabaseQuery() {}
	abstract public boolean connect();
	abstract public float getValue(String queryString);
	abstract public boolean close();
}
