package r01f.persistence.db.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.eclipse.persistence.annotations.Multitenant;
import org.eclipse.persistence.annotations.MultitenantType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.persistence.db.DBEntityHasOID;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObjectImpl;

@MappedSuperclass
@Multitenant(MultitenantType.SINGLE_TABLE)
@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityBaseWithOID
			  extends DBEntityBase
		   implements DBEntityForModelObject<DBPrimaryKeyForModelObject>,
		   			  DBEntityHasOID {

	private static final long serialVersionUID = -1001565203286537182L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////

	@Id @Column(name="OID",length=OID.OID_LENGTH,nullable=false) @Basic
	@Getter @Setter protected String  _oid;
//////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public DBPrimaryKeyForModelObject getDBEntityPrimaryKey() {
		return DBPrimaryKeyForModelObjectImpl.from(_oid);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////

}
