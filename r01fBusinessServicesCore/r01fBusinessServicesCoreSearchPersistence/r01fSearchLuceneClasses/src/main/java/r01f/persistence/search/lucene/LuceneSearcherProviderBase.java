package r01f.persistence.search.lucene;

import lombok.experimental.Accessors;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.TypeMetaData;
import r01f.model.metadata.TypeMetaDataInspector;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.persistence.lucene.LuceneIndex;
import r01f.persistence.search.SearcherProviderBase;

@Accessors(prefix="_")
public abstract class LuceneSearcherProviderBase<F extends SearchFilter,I extends SearchResultItem>
  			  extends SearcherProviderBase<F,I> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	protected final LuceneIndex _luceneIndex;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	protected LuceneSearcherProviderBase(final Class<? extends IndexableModelObject> indexableObjectType,
										 final Marshaller marshaller,
										 final LuceneIndex luceneIndex) {
		super(indexableObjectType,
			  marshaller);
		_luceneIndex = luceneIndex;
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	@SuppressWarnings({ "unchecked","rawtypes" })
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