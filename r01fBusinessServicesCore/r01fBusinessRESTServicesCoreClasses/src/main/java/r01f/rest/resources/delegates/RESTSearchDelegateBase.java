package r01f.rest.resources.delegates;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.experimental.Accessors;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.model.search.query.SearchResultsOrdering;
import r01f.rest.RESTOperationsResponseBuilder;
import r01f.securitycontext.SecurityContext;
import r01f.services.interfaces.SearchServices;

/**
 * Base type for REST services that encapsulates the common search index ops: indexing, searching
 */
@Accessors(prefix="_")
public abstract class RESTSearchDelegateBase<F extends SearchFilter,I extends SearchResultItem> 
           implements RESTDelegate {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final SearchServices<F,I> _searchServices;
	private final MediaType _mediaType;	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <T extends SearchServices<F,I>> T searchServicesAs(@SuppressWarnings("unused") final Class<T> type) {
		return (T)_searchServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTSearchDelegateBase(final SearchServices<F,I> searchServices) {
		_searchServices = searchServices;
		_mediaType = MediaType.APPLICATION_XML_TYPE;
		
	}
	public RESTSearchDelegateBase(final SearchServices<F,I> searchServices,
			                      final MediaType mediaType) {
		_searchServices = searchServices;
		_mediaType = mediaType;
		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Search using the provided filter
	 * @param securityContext 
	 * @param resourcePath
	 * @param filter
	 * @param startingRow
	 * @param itemsToReturn
	 * @return
	 */
	public Response search(final SecurityContext securityContext,final String resourcePath,
						   final F filter,final Collection<SearchResultsOrdering> ordering,
						   final int startingRow,final int itemsToReturn) {
		SearchResults<F,I> searchResults = null;
		searchResults = _searchServices.filterRecords(securityContext,
									   		    	  filter,ordering,
									   		    	  startingRow,itemsToReturn);
		Response outResponse = RESTOperationsResponseBuilder.searchIndex()
															.at(URI.create(resourcePath))
															.mediaType(_mediaType)
															.build(searchResults);
		return outResponse;
	}
}
