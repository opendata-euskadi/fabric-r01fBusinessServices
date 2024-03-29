package r01f.services.interfaces;

import r01f.guids.PersistableObjectOID;
import r01f.locale.Language;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.FindOIDsResult;
import r01f.model.persistence.FindResult;
import r01f.model.persistence.FindSummariesResult;
import r01f.securitycontext.SecurityContext;

/**
 * Finding
 * @param <O>
 * @param <M>
 */
public interface FindServicesForDependentModelObject<O extends PersistableObjectOID,M extends PersistableModelObject<O>,
													 PO extends PersistableObjectOID>
		 extends ServiceInterfaceForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FINDING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all child objects of the given parent returning the child objects oids
	 * @param securityContext
	 * @param parentOid
	 * @return
	 */
	FindOIDsResult<O> findOidsOfDependentsOf(final SecurityContext securityContext,
						      				 final PO parentOid);
	/**
	 * Finds all child objects of the given parent returning the full child object
	 * @param securityContext
	 * @param parentOid
	 * @return
	 */
	FindResult<M> findDependentsOf(final SecurityContext securityContext,
								   final PO parentOid);
	/**
	 * Finds all child objects of the given parent
	 * @param securityContext
	 * @param parentOid
	 * @return
	 */
	FindSummariesResult<M> findSummariesOfDependentsOf(final SecurityContext securityContext,
													   final PO parentOid,
													   final Language lang);
}
