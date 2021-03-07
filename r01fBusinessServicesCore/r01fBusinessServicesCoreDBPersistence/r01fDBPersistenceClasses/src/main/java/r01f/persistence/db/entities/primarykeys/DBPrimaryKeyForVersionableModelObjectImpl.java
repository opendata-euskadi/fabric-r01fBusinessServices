package r01f.persistence.db.entities.primarykeys;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.OIDForVersionableModelObject;

/**
 * PrimaryKey for any versionable object
 */
@Accessors(prefix="_")
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor 
public class DBPrimaryKeyForVersionableModelObjectImpl 
	 extends DBPrimaryKeyForModelObjectImpl
  implements DBPrimaryKeyForVersionableModelObject {

	private static final long serialVersionUID = -5356804843117150607L;
/////////////////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public DBPrimaryKeyForVersionableModelObjectImpl(final String oid) {
		super(oid);
	}
	public static DBPrimaryKeyForVersionableModelObjectImpl from(final String oid) {
		return new DBPrimaryKeyForVersionableModelObjectImpl(oid);
	}
	public static <O extends OIDForVersionableModelObject> DBPrimaryKeyForVersionableModelObjectImpl from(final O oid) {
		return new DBPrimaryKeyForVersionableModelObjectImpl(oid.asString());
	}
}
