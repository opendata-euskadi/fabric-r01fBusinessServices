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
	protected LuceneSearcherProviderBase(final Marshaller marshaller,
										 final LuceneIndex luceneIndex) {
		super(marshaller);
		_luceneIndex = luceneIndex;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked","rawtypes" })
	protected IndexDocumentFieldConfigSet<? extends IndexableModelObject> getIndexableObjTypeFieldsConfigSet(final Class<? extends IndexableModelObject> indexableObjectType) {
		IndexDocumentFieldConfigSet<? extends IndexableModelObject> outFieldsConfigSet = _fieldsConfigSetByIndexableObjType.get(indexableObjectType);
		if (outFieldsConfigSet == null) {
			TypeMetaData<? extends IndexableModelObject> indexableObjTypeMetadata = TypeMetaDataInspector.singleton()
													  													 .getTypeMetaDataFor(indexableObjectType);
			outFieldsConfigSet = new IndexDocumentFieldConfigSet(indexableObjectType,
															     indexableObjTypeMetadata);
			_fieldsConfigSetByIndexableObjType.put(indexableObjectType,
												   outFieldsConfigSet);
		}
		return outFieldsConfigSet;
	}
}