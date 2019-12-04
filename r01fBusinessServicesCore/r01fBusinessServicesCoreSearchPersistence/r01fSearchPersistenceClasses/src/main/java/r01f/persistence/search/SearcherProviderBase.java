package r01f.persistence.search;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.model.IndexableModelObject;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;

@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
public abstract class SearcherProviderBase<F extends SearchFilter,I extends SearchResultItem> 
	       implements SearcherProvider<F,I> {
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	protected final Map<Class<? extends IndexableModelObject>,IndexDocumentFieldConfigSet<? extends IndexableModelObject>> _fieldsConfigSetByIndexableObjType = Maps.newHashMap();
	protected final Marshaller _marshaller;
}
