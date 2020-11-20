package r01f.persistence.db;

public interface DBEntityFactory<DB extends DBEntity> {
	/**
	 * @return a new {@link DBEntity} factory
	 */
	public DB create();
}
