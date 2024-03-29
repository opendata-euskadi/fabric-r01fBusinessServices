package r01f.persistence.db.entities;

import r01f.model.PersistableModelObject;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;

/**
 * Marker interface for JPA Entity
 * @param <R> the {@link PersistableModelObject} that is represented by this entity
 */
public interface DBEntityForModelObject<PK extends DBPrimaryKeyForModelObject> 
	     extends DBEntity {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the oid
	 */
	public String getOid();
	/**
	 * @param oid the oid
	 */
	public void setOid(String oid);
/////////////////////////////////////////////////////////////////////////////////////////
//  DESCRIPTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * @return the primary key for the dbEntity
	 */
	public PK getDBEntityPrimaryKey();
}
