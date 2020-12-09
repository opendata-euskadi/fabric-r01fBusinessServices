package r01f.persistence.db.entities;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.eclipse.persistence.annotations.OptimisticLocking;
import org.eclipse.persistence.annotations.OptimisticLockingType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.HasTrackingInfo;
import r01f.model.ModelObjectTracking;
import r01f.model.facets.HasEntityVersion;
import r01f.persistence.db.DBEntity;
import r01f.securitycontext.SecurityContext;
import r01f.securitycontext.SecurityIDS.LoginID;
import r01f.securitycontext.SecurityOIDs.UserOID;
import r01f.util.types.Dates;
import r01f.util.types.Strings;


/**
 * An entity
 * @param <R>
 */
@MappedSuperclass
@OptimisticLocking(type=OptimisticLockingType.VERSION_COLUMN,
				   cascade=false)
@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityBase
		   implements DBEntity,
		   			  HasEntityVersion,
		   			  HasTrackingInfo {

	private static final long serialVersionUID = -2168679594935612325L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create date
	 */
	@Column(name="CREATED_AT",
			insertable=true,updatable=false) @Temporal(TemporalType.TIMESTAMP)
	@Getter @Setter protected Calendar _createDate; 			// http://www.developerscrappad.com/228/java/java-ee/ejb3-jpa-dealing-with-date-time-and-timestamp/
	/**
	 * Last update date
	 */
	@Column(name="LASTMODIFIED_AT",
			insertable=false,updatable=true) @Temporal(TemporalType.TIMESTAMP)
	@Getter @Setter protected Calendar _lastUpdateDate;		// http://www.developerscrappad.com/228/java/java-ee/ejb3-jpa-dealing-with-date-time-and-timestamp/
	/**
	 * The user code / application code of the creator
	 */
	@Column(name="CREATED_BY",length=OID.OID_LENGTH)
	@Getter @Setter protected String _creator;
	/**
	 * The user oid of the creator
	 */
	@Column(name="CREATED_BY_OID",length=OID.OID_LENGTH)
	@Getter @Setter protected String _creatorOid;
	/**
	 * The user code / application code of the last updator
	 */
	@Column(name="UPDATED_BY",length=OID.OID_LENGTH)
	@Getter @Setter protected String _lastUpdator;
	/**
	 * The user oid of the last updator
	 */
	@Column(name="UPDATED_BY_OID",length=OID.OID_LENGTH)
	@Getter @Setter protected String _lastUpdatorOid;
	/**
	 * Entity Version to prevent conflicts
	 * See:
	 * 		http://www.eclipse.org/eclipselink/documentation/2.5/concepts/descriptors002.htm#CHEEEIEA
	 * 		http://en.wikibooks.org/wiki/Java_Persistence/Locking
	 *
	 * Optimistic locking is used (assumed that conflicts are unlike to happen)
	 * i.e. There are two web processes running in parallel, both processing the
	 * 		stock of an store item
	 * 		... let's say that initially we have stock=100
	 * 			----------[100]----------
	 * 			|						|
	 * 		  Load                    Load
	 *          |-1						|-1
	 *        [99]                     [99]
	 *          |						|
	 *        Save                    Save
	 *          |---------[99]			|
	 *          		  [99]----------| <---WTF!! the stock should have been 98
	 *          									but it ends being 99: WRONG!!
	 * To prevent this situation a last update timestamp or an incrementing version is used
	 * Every time a process want to update an entity it MUST tell us what the version is so
	 * if a conflict occurs it could be detected:
	 *
	 * 			----------[100]----------
	 * 			|	   (version=1)		|
	 * 			|						|
	 * 	      Load 				       Load
	 * 	   (version=1) 		       (version=1)
	 *          |-1						|-1
	 *        [99]                     [99]
	 *          |						|
	 *        Save                      |
	 *     (version=1)                  |
	 *          |---------[99]			|
	 *          	   (version=2)		|
	 *          			|		   Save
	 *          		CONFLICT!<--(Version=1)
	 *
	 * As seen, to be able to detect conflicts:
	 * 		- A version number (a timestamp) MUST be stored with the record
	 * 		- The version number MUST be loaded alongside the record and stored at the processing client
	 * 		- The version number MUST be send alongside the record in any update operation
	 * 		  so the received version could be compared with the provided one
	 * IMPORTANT!!
	 * 		Using ECLIPSELINK, set @OptimisticLocking(type=OptimisticLockingType.VERSION_COLUMN,
	 *			   									  cascade=true)
	 *	    at the entity type
	 */
	@Column(name="ENTITY_VERSION") @Version
	@Getter @Setter protected long _entityVersion;

	/**
	 * Holds info about tracking info
	 */
	private final transient ModelObjectTracking _modelObjectTracking = new ModelObjectTracking();

/////////////////////////////////////////////////////////////////////////////////////////
//  AUTO-UPDATE CREATE_DATE AND LAST_UPDATE_DATE
/////////////////////////////////////////////////////////////////////////////////////////
	@PrePersist
	void createdAt() {
		_createDate = Calendar.getInstance();
		_lastUpdateDate = _createDate;
		_preCreate();
	}
	@PreUpdate
	void updatedAt() {
		_lastUpdateDate = Calendar.getInstance();
		_preUpdate();
	}
	protected abstract void _preCreate();
	protected abstract void _preUpdate();

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Date getCreateTimeStamp() {
		return Dates.fromCalendar(_createDate);
	}
	@Override
	public void setCreateTimeStamp(final Date newTS) {
		throw new UnsupportedOperationException("Do not call setCreateTimeStamp at the persistence layer!");
	}
	@Override
	public Date getLastUpdateTimeStamp() {
		return Dates.fromCalendar(_lastUpdateDate);
	}
	@Override
	public void setLastUpdateTimeStamp(final Date newTS) {
		throw new UnsupportedOperationException("Do not call setLastUpdateTimeStamp at the persistence layer!");
	}
	@Override
	public LoginID getCreatorUserCode() {
		return Strings.isNOTNullOrEmpty(_creator) ? LoginID.forId(_creator) : null;
	}

	@Override
	public void setCreatorUserCode(final LoginID creator) {
		if (creator != null) _creator = creator.asString();
	}
	@Override
	public UserOID getCreatorUserOid() {
		return Strings.isNOTNullOrEmpty(_creatorOid) ? UserOID.forId(_creatorOid) : null;
	}
	@Override
	public void setCreatorUserOid(final UserOID creatorOid) {
		if (creatorOid != null) _creatorOid = creatorOid.asString();
	}
	public void setCreatorFrom(final SecurityContext securityContext) {
		this.setCreatorUserCode(securityContext.getLoginId());
		if (securityContext.isForUser()) this.setCreatorUserOid(securityContext.asForUser()
																			   .getUserOid());
	}
	@Override
	public LoginID getLastUpdatorUserCode() {
		return Strings.isNOTNullOrEmpty(_lastUpdator) ? LoginID.forId(_lastUpdator) : null;
	}
	@Override
	public void setLastUpdatorUserCode(final LoginID lastUpdator) {
		if (lastUpdator != null) _lastUpdator = lastUpdator.asString();
	}
	@Override
	public UserOID getLastUpdatorUserOid() {
		return Strings.isNOTNullOrEmpty(_lastUpdatorOid) ? UserOID.forId(_lastUpdatorOid) : null;
	}
	@Override
	public void setLastUpdatorUserOid(final UserOID lastUpdatorOid) {
		if (lastUpdatorOid != null) _lastUpdatorOid = lastUpdatorOid.asString();
	}
	public void setLastUpdatorFrom(final SecurityContext securityContext) {
		this.setLastUpdatorUserCode(securityContext.getLoginId());
		if (securityContext.isForUser()) this.setLastUpdatorUserOid(securityContext.asForUser()
																				   .getUserOid());
	}
	@Override
	public ModelObjectTracking getTrackingInfo() {
		if (_createDate != null) _modelObjectTracking.setCreateDate(_createDate.getTime());
		if (_lastUpdateDate != null) _modelObjectTracking.setLastUpdateDate(_lastUpdateDate.getTime());
		if (_creatorOid != null) _modelObjectTracking.setCreatorUserOid(UserOID.forId(_creatorOid));
		if (_creator != null) _modelObjectTracking.setCreatorUserCode(LoginID.forId(_creator));
		if (_lastUpdatorOid != null) _modelObjectTracking.setLastUpdatorUserOid(UserOID.forId(_lastUpdatorOid));
		if (_lastUpdator != null) _modelObjectTracking.setLastUpdatorUserCode(LoginID.forId(_lastUpdator));
		return _modelObjectTracking;
	}
}
