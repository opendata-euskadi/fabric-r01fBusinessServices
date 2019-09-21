package r01f.persistence.search;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.TypeMetaData;
import r01f.model.metadata.TypeMetaDataInspector;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;

@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
public abstract class SearcherProviderBase<F extends SearchFilter,I extends SearchResultItem> 
	       implements SearcherProvider<F,I> {
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	protected final Class<? extends IndexableModelObject> _indexableObjectType;
	protected final Map<Class<? extends IndexableModelObject>,IndexDocumentFieldConfigSet<? extends IndexableModelObject>> _fieldsConfigSetByIndexableObjType = Maps.newHashMap();		
	
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	@SuppressWarnings({ "rawtypes","unchecked" })
	protected IndexDocumentFieldConfigSet<? extends IndexableModelObject> getIndexableObjTypeFieldsConfigSet() {
		IndexDocumentFieldConfigSet<? extends IndexableModelObject> outFieldsConfigSet = _fieldsConfigSetByIndexableObjType.get(_indexableObjectType);
		if (outFieldsConfigSet == null) {
			TypeMetaData<? extends IndexableModelObject> indexableObjTypeMetadata = TypeMetaDataInspector.singleton()
													  													 .getTypeMetaDataFor(_indexableObjectType);
			outFieldsConfigSet = new IndexDocumentFieldConfigSet(_indexableObjectType,
															     indexableObjTypeMetadata);
			_fieldsConfigSetByIndexableObjType.put(_indexableObjectType, 
												   outFieldsConfigSet);
		}
		return outFieldsConfigSet;
	}
}
