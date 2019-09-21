package r01f.persistence.index.db;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.TypeMetaData;
import r01f.persistence.index.IndexableFieldValuesExtractor;
import r01f.persistence.index.IndexerBase;

/**
 * Base type for DB indexers
 * @param <P>
 */
@Accessors(prefix="_")
public abstract class DBIndexerBase<P extends IndexableModelObject>
              extends IndexerBase<P> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS                                                                          
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The entity manager MUST be provided by the higher level layer because there is where the 
	 * transaction begins and that transaction could span more than one persistence types 
	 * (ie the CRUD persistence and the relations persistence)
	 */
	@Getter(AccessLevel.PROTECTED) protected final EntityManager _entityManager;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public DBIndexerBase(final Class<P> modelObjType,final TypeMetaData<P> modelObjectTypeMetaData,
						 final Provider<IndexableFieldValuesExtractor<P>> indexableFieldsValuesExtractorProvider,
						 final EntityManager entityManager) {
		super(modelObjType,modelObjectTypeMetaData,
			  indexableFieldsValuesExtractorProvider);
		_entityManager = entityManager;
	}
}
