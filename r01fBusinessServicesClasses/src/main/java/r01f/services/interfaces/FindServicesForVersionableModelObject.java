package r01f.services.interfaces;

import r01f.guids.OIDForVersionableModelObject;
import r01f.guids.PersistableObjectOID;
import r01f.guids.VersionIndependentOID;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.model.persistence.FindResult;
import r01f.securitycontext.SecurityContext;

/**
 * Finding for versionable model objects
 * @param <O>
 * @param <M>
 */
public interface FindServicesForVersionableModelObject<O extends OIDForVersionableModelObject & PersistableObjectOID,M extends PersistableModelObject<O> & HasVersionableFacet>
		 extends FindServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FINDING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all model object's versions
	 * @param securityContext the user auth data & context info
	 * @param versionIndependentOid
	 * @return a {@link PersistenceOperationOK} that encapsulates the oids
	 */
	public FindResult<M> findAllVersionsOf(final SecurityContext securityContext,
										   final VersionIndependentOID versionIndependentOid);
}
