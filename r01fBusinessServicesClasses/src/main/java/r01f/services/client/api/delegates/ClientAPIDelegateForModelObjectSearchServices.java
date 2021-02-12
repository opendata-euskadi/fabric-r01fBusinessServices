package r01f.services.client.api.delegates;

import java.util.Collection;

import javax.inject.Provider;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.model.search.SearchResultsProvider;
import r01f.model.search.query.SearchResultsOrdering;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.search.SearchResultsLoader;
import r01f.securitycontext.SecurityContext;
import r01f.services.interfaces.SearchServices;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

/**
 * Adapts Search API method invocations to the service proxy that performs the core method invocations
 * @param <F>
 * @param <I>
 */
@Accessors(prefix="_")
public abstract class ClientAPIDelegateForModelObjectSearchServices<F extends SearchFilter,I extends SearchResultItem> 
	 		  extends ClientAPIServiceDelegateBase<SearchServices<F,I>> {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final int SEARCH_RESULT_PAGE_SIZE = 10;		// TODO parameterize the search result page size
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Deprecated
	@Getter private final Class<F> _filterType;
	
	@Deprecated
	@Getter private final Class<I> _resultItemType;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	public ClientAPIDelegateForModelObjectSearchServices(final Provider<SecurityContext> securityContextProvider,
														 final Marshaller modelObjectsMarshaller,
														 final SearchServices<F,I> services) {
		super(securityContextProvider,
			  modelObjectsMarshaller,
			  services);
		_filterType = null;
		_resultItemType = null;
	}
	@Deprecated
	public ClientAPIDelegateForModelObjectSearchServices(final Provider<SecurityContext> securityContextProvider,
														 final Marshaller modelObjectsMarshaller,
														 final SearchServices<F,I> services,
														 final Class<F> filterType,final Class<I> resultItemType) {
		super(securityContextProvider,
			  modelObjectsMarshaller,
			  services);
		_filterType = filterType;
		_resultItemType = resultItemType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  COUNT
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Counts the records that matches the given filter
	 * @param filter
	 * @return
	 */
	public long count(final F filter) {
		return this.getServiceProxy()
				   .countRecords(this.getSecurityContext(),
						   		 filter);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	SEARCH
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Searches returning only the first page results
	 * @param filter
	 * @param ordering
	 * @return
	 */
	public ClientAPIDelegateForModelObjectSearchServicesPageStep1 search(final F filter,final SearchResultsOrdering... ordering) {
		Collection<SearchResultsOrdering> theOrdering = CollectionUtils.hasData(ordering) ? FluentIterable.from(ordering)
																								  .filter(Predicates.notNull())
																								  .toList()
																				  		  : null; 
		return new ClientAPIDelegateForModelObjectSearchServicesPageStep1(filter,
																		  theOrdering);
	}
	/**
	 * Searches returning only the first page results
	 * @param filter
	 * @param ordering
	 * @return
	 */
	public ClientAPIDelegateForModelObjectSearchServicesPageStep1 search(final F filter,final Collection<SearchResultsOrdering> ordering) {
		return new ClientAPIDelegateForModelObjectSearchServicesPageStep1(filter,
																		  ordering);
	}
	/**
	 * Searches returning only the first page results
	 * @param filter
	 * @return
	 */
	public ClientAPIDelegateForModelObjectSearchServicesPageStep1 search(final F filter) {
		return new ClientAPIDelegateForModelObjectSearchServicesPageStep1(filter,
																		  null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ClientAPIDelegateForModelObjectSearchServicesPageStep1 {
		private final F _filter;
		private final Collection<SearchResultsOrdering> _ordering;
		
		public ClientAPIDelegateForModelObjectSearchServicesPageStep2 fromItemAt(final int firstItemNum) {
			return new ClientAPIDelegateForModelObjectSearchServicesPageStep2(_filter,_ordering,
																			  firstItemNum);
		}
		public SearchResults<F,I> firstPageOfSize(final int numberOfItems) {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getSecurityContext(),
																			 		_filter,_ordering,
																			 		0,numberOfItems);
		}
		public SearchResults<F,I> firstPage() {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getSecurityContext(),
																			 		_filter,_ordering,
																			 		0,SEARCH_RESULT_PAGE_SIZE);
		}
		public Collection<I> allItems() {
			SearchResultsLoader<F,I> loader = SearchResultsLoader.create(new SearchResultsProvider<F,I>(_filter,
																										SEARCH_RESULT_PAGE_SIZE) {
																				@Override
																				public SearchResults<F, I> provide(final int startPosition) {
																					return ClientAPIDelegateForModelObjectSearchServicesPageStep1.this.fromItemAt(startPosition)
																																					  .returning(this.getPageSize());
																				}
																		 });
			return loader.collectAll();
		}
		public <T> Collection<T> allItemsTransformed(final Function<I,T> transformingFunction) {
			if (this.allItems() == null) return Lists.newArrayList();
			return FluentIterable.from(this.allItems())
								 .transform(transformingFunction)
								 .toList();
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ClientAPIDelegateForModelObjectSearchServicesPageStep2 {		
		private final F _filter;
		private final Collection<SearchResultsOrdering> _ordering;
		private final int _firstItemNum;
		
		public SearchResults<F,I> returning(final int numberOfItems) {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getSecurityContext(),
																			 		_filter,_ordering,
																			 		_firstItemNum,numberOfItems);
		}
		public SearchResults<F,I> returningTheDefaultNumberOfItems() {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getSecurityContext(),
																			 		_filter,_ordering,
																			 		_firstItemNum,SEARCH_RESULT_PAGE_SIZE);
		}
	}
}
