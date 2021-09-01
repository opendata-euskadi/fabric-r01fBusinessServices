package r01f.services.client.servicesproxy.rest;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.guids.PersistableObjectOID;
import r01f.httpclient.HttpResponse;
import r01f.model.PersistableModelObject;
import r01f.model.persistence.CRUDResult;
import r01f.model.persistence.PersistenceRequestedOperation;
import r01f.securitycontext.SecurityContext;
import r01f.types.url.Url;


@Accessors(prefix="_")
public interface RESTResponseToCRUDResultMapper<O extends PersistableObjectOID,M extends PersistableModelObject<O>> {


/////////////////////////////////////////////////////////////////////////////////////////
// METHODS TO IMPLEMENT
/////////////////////////////////////////////////////////////////////////////////////////

	public CRUDResult<M> mapHttpResponseForEntity(final SecurityContext securityContext,
												  final PersistenceRequestedOperation requestedOp,
												  final M targetEntity,
												  final Url restResourceUrl,final HttpResponse httpResponse) ;

	public CRUDResult<M> mapHttpResponseForEntity(final SecurityContext securityContext,
												  final PersistenceRequestedOperation requestedOp,
												  final OID targetEntityOid,
												  final Url restResourceUrl,final HttpResponse httpResponse);

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
