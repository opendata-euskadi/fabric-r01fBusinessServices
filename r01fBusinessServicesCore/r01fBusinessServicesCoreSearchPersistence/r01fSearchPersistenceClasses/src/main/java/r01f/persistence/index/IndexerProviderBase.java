package r01f.persistence.index;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.TypeMetaData;
import r01f.model.metadata.TypeMetaDataInspector;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;

@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
public abstract class IndexerProviderBase<M extends IndexableModelObject> 
		   implements IndexerProvider<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	protected final Class<M> _indexableObjectType;
	protected final Map<Class<? extends IndexableModelObject>,IndexDocumentFieldConfigSet<? extends IndexableModelObject>> _fieldsConfigSetByIndexableObjType = Maps.newHashMap();		

/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	@SuppressWarnings("unchecked")
	protected IndexDocumentFieldConfigSet<M> getIndexableObjTypeFieldsConfigSet() {
		IndexDocumentFieldConfigSet<M> outFieldsConfigSet = (IndexDocumentFieldConfigSet<M>)_fieldsConfigSetByIndexableObjType.get(_indexableObjectType);
		if (outFieldsConfigSet == null) {
			TypeMetaData<M> indexableObjTypeMetadata = TypeMetaDataInspector.singleton()
													  			.getTypeMetaDataFor(_indexableObjectType);
			outFieldsConfigSet = new IndexDocumentFieldConfigSet<M>(_indexableObjectType,
															     	indexableObjTypeMetadata);
			_fieldsConfigSetByIndexableObjType.put(_indexableObjectType, 
												   outFieldsConfigSet);
		}
		return outFieldsConfigSet;
	}
}
