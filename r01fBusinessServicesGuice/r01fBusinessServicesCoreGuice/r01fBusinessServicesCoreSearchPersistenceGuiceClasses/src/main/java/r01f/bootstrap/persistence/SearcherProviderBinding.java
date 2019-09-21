package r01f.bootstrap.persistence;

import com.google.inject.TypeLiteral;

import r01f.inject.GuiceGenericBinding;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.persistence.search.SearcherProvider;

public class SearcherProviderBinding<F extends SearchFilter,I extends SearchResultItem> 
	 extends GuiceGenericBinding<SearcherProvider<F,I>> {
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	public SearcherProviderBinding(final TypeLiteral<SearcherProvider<F,I>> genericType,
								   final Class<? extends SearcherProvider<F,I>> implementingType) {
		super(genericType,
			  implementingType);
	}
	public static <F extends SearchFilter,I extends SearchResultItem> SearcherProviderBinding<F,I> of(final TypeLiteral<SearcherProvider<F,I>> typeLiteral,
																									  final Class<? extends SearcherProvider<F,I>> implementingType) {
		return new SearcherProviderBinding<F,I>(typeLiteral,
												implementingType);
	}	
}
