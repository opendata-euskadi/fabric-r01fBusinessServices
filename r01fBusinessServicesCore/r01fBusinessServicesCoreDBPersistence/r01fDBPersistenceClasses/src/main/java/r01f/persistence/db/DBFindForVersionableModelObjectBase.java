package r01f.persistence.db;

import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OIDForVersionableModelObject;
import r01f.guids.PersistableObjectOID;
import r01f.guids.VersionIndependentOID;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.model.persistence.FindResult;
import r01f.model.persistence.FindResultBuilder;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.db.config.DBModuleConfig;
import r01f.persistence.db.entities.DBEntityForVersionableModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForVersionableModelObject;
import r01f.securitycontext.SecurityContext;
import r01f.services.interfaces.FindServicesForVersionableModelObject;
import r01f.util.types.Strings;

/**
 * Base type for every persistence layer type
 * @param <O>
 * @param <M>
 * @param <PK>
 * @param <DB>
 */
@Slf4j
@Accessors(prefix="_")
public abstract class DBFindForVersionableModelObjectBase<O extends OIDForVersionableModelObject & PersistableObjectOID,M extends PersistableModelObject<O> & HasVersionableFacet,
							     						  PK extends DBPrimaryKeyForVersionableModelObject,DB extends DBEntity & DBEntityForVersionableModelObject<PK>>
			  extends DBFindForModelObjectBase<O,M,
			  				 				   PK,DB>
	       implements FindServicesForVersionableModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Deprecated
	public DBFindForVersionableModelObjectBase(final DBModuleConfig dbCfg,
											   final Class<M> modelObjectType,final Class<DB> dbEntityType,
											   final EntityManager entityManager,
											   final Marshaller marshaller) {
		super(dbCfg,
			  modelObjectType,dbEntityType,
			  entityManager,
			  marshaller);
	}
	public DBFindForVersionableModelObjectBase(final Class<M> modelObjectType,final Class<DB> dbEntityType,
											   final DBModuleConfig dbCfg,
											   final EntityManager entityManager,
											   final Marshaller marshaller) {
		super(modelObjectType,dbEntityType,
			  dbCfg,
			  entityManager,
			  marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
    @Override @SuppressWarnings("unchecked")
    public FindResult<M> findAllVersionsOf(final SecurityContext securityContext,
                                           final VersionIndependentOID versionIndependentOid) {
        log.debug("> loading all {} versions with version independent oid={}",
        		  _modelObjectType.getSimpleName(),
        		  versionIndependentOid);
        String jpql = Strings.customized("SELECT e " +
        		  						   "FROM {} e " +
        		  						  "WHERE e._oid = :versionIndependentOid",
        		  						 _DBEntityType.getSimpleName());
		List<DB> dbEntities = _entityManager.createQuery(jpql)
        									.setParameter("versionIndependentOid",versionIndependentOid.asString())
                                            .setHint(QueryHints.READ_ONLY,HintValues.TRUE)
                                            .getResultList();
        return FindResultBuilder.using(securityContext)
        						.on(_modelObjectType)
        						.foundDBEntities(dbEntities)
        						.transformedToModelObjectsUsing(this);
    }
}
