package r01f.bootstrap.persistence;


import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

import lombok.extern.slf4j.Slf4j;
import r01f.bootstrap.services.core.SearchEnginePersistenceGuiceModule;
import r01f.inject.HasMoreBindings;
import r01f.model.IndexableModelObject;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.persistence.index.Indexer;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.persistence.search.Searcher;
import r01f.persistence.search.config.SearchModuleConfig;
import r01f.util.types.collections.CollectionUtils;

/**
 * Base {@link Guice} module for search engine (index / search) bindings
 */
@Slf4j
public abstract class SearchGuiceModuleBase 
           implements SearchEnginePersistenceGuiceModule {
/////////////////////////////////////////////////////////////////////////////////////////
// 	CONFIG
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Search engine config 
	 */
	protected final SearchModuleConfig _searchEngineConfig;
	/**
	 * Factories for indexers 
	 */
	protected final Collection<IndexerProviderBinding<?>> _indexerProviderBindings;
	/**
	 * Factories for searchers
	 */
	protected final Collection<SearcherProviderBinding<?,?>> _searcherProviderBindings;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected SearchGuiceModuleBase(final SearchModuleConfig searchEngineConfig,
									final Collection<IndexerProviderBinding<?>> indexerProviderBindings,
									final Collection<SearcherProviderBinding<?,?>> searcherProviderBindings) {
		_searchEngineConfig = searchEngineConfig;
		_indexerProviderBindings = indexerProviderBindings;
		_searcherProviderBindings = searcherProviderBindings;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override 
	public void configure(final Binder binder) {	
		// Bind searchers
		log.warn("\t\t...binding searchers: {} providers",
				 _searcherProviderBindings != null ? _searcherProviderBindings.size() : 0);
		if (CollectionUtils.hasData(_searcherProviderBindings)) {
			for (SearcherProviderBinding<?,?> searcherProviderBinding : _searcherProviderBindings) {
				try {
					_bind(binder,
						  searcherProviderBinding);
				} catch (Throwable th) {
					log.error("searcher provider binding error: {}",th.getMessage(),th);
				}
			}
		}
		// Bind indexers
		log.warn("\t\t...binding indexers: {} providers",
				 _indexerProviderBindings != null ? _indexerProviderBindings.size() : 0);
		if (CollectionUtils.hasData(_indexerProviderBindings)) {
			for (final IndexerProviderBinding<?> indexerProviderBinding : _indexerProviderBindings) {
				try {
					_bind(binder,
						  indexerProviderBinding);
				} catch (Throwable th) {
					log.error("indexer provider binding error: {}",th.getMessage(),th);
				}
			}
		}
		// Give chance to sub-types to do more bindings
		if (this instanceof HasMoreBindings) {
			((HasMoreBindings)this).configureMoreBindings(binder);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	GENERICS TRICK TO BIND (uses generics capture to bind)
/////////////////////////////////////////////////////////////////////////////////////////
	private static <F extends SearchFilter,I extends SearchResultItem> void _bind(final Binder binder,
																				  final SearcherProviderBinding<F,I> binding) {
		log.warn("\t\t\t-searcher/indexer provider type {}",
				 binding.getImplementingType());
		
		binder.bind(binding.getGenericType())
			  .to(binding.getImplementingType());
		binder.bind(binding.getImplementingType()) 
			  .in(Singleton.class);
	}
	private static <M extends IndexableModelObject> void _bind(final Binder binder,
															   final IndexerProviderBinding<M> binding) {
		log.warn("\t\t\t-indexer provider type {}",
				 binding.getImplementingType());
		
		binder.bind(binding.getGenericType())
			  .to(binding.getImplementingType());
		binder.bind(binding.getImplementingType()) 
			  .in(Singleton.class);
	}
	
	
	
//	public Collection<TypeLiteral<?>> getServicesToExpose() {
//		Collection<TypeLiteral<?>> outServicesToExpose = Lists.newArrayList();
//		// indexers
//		if (CollectionUtils.hasData(_searchEngineConfig.getIndexers())) {
//			for (IndexerConfig<? extends IndexableModelObject> indexer : _searchEngineConfig.getIndexers()) {
//				outServicesToExpose.add(_indexerGuiceKey(indexer.getModelObjType()));
//			}
//		}
//		// searchers
//		if (CollectionUtils.hasData(_searchEngineConfig.getSearchers())) {
//			for (SearcherConfig<? extends SearchFilter,? extends SearchResultItem> searcher : _searchEngineConfig.getSearchers()) {
//				outServicesToExpose.add(_searcherGuiceKeyFor(searcher.getSearchFilterType(),
//															 searcher.getSearchResultItemType()));
//			}
//		}
//		return outServicesToExpose;
//	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	@SuppressWarnings("unchecked")
	private static <F extends SearchFilter,I extends SearchResultItem> TypeLiteral<Searcher<?,?>> _searcherGuiceKeyFor(final Class<F> filterType,final Class<I> resultItemType) {
		ParameterizedType pt = Types.newParameterizedType(Searcher.class, 
														  filterType,resultItemType);
		return (TypeLiteral<Searcher<?,?>>)TypeLiteral.get(pt);
	}
	@SuppressWarnings("unchecked")
	public <M extends IndexableModelObject> TypeLiteral<Indexer<?>> _indexerGuiceKey(final Class<M> modelObjType) {
		ParameterizedType pt = Types.newParameterizedType(Indexer.class, 
										    			  modelObjType);
		return (TypeLiteral<Indexer<?>>)TypeLiteral.get(pt);
	}
	@SuppressWarnings("unchecked")
	public <M extends IndexableModelObject> TypeLiteral<IndexDocumentFieldConfigSet<?>> _indexDocumentFieldsConfigGuiceKey(final Class<M> modelObjType) {
		ParameterizedType pt = Types.newParameterizedType(IndexDocumentFieldConfigSet.class, 
										    			  modelObjType);
		return (TypeLiteral<IndexDocumentFieldConfigSet<?>>)TypeLiteral.get(pt);
	}
}
