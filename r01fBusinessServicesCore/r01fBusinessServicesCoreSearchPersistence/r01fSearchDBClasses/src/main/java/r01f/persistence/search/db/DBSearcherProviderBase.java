package r01f.persistence.search.db;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.db.config.DBModuleConfig;
import r01f.persistence.search.SearcherProviderBase;

public abstract class DBSearcherProviderBase<F extends SearchFilter,I extends SearchResultItem>
		      extends SearcherProviderBase<F,I> {

/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	protected final DBModuleConfig _dbModuleConfig;
	protected final Provider<EntityManager> _entityManagerProvider;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	protected DBSearcherProviderBase(final Marshaller marshaller,
									 final DBModuleConfig dbModuleConfig,
									 final Provider<EntityManager> entityManagerProvider) {
		super(marshaller);
		_dbModuleConfig = dbModuleConfig;
		_entityManagerProvider = entityManagerProvider;
	}
}
