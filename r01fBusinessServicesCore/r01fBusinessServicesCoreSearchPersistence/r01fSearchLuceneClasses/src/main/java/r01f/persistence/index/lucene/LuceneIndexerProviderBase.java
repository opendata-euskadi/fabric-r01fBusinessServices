package r01f.persistence.index.lucene;

import lombok.experimental.Accessors;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.TypeMetaData;
import r01f.model.metadata.TypeMetaDataInspector;
import r01f.objectstreamer.Marshaller;
import r01f.persistence.index.IndexerProviderBase;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.persistence.lucene.LuceneIndex;

@Accessors(prefix="_")
public abstract class LuceneIndexerProviderBase<M extends IndexableModelObject>
  			  extends IndexerProviderBase<M> {

/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS                                                                          
/////////////////////////////////////////////////////////////////////////////////////////	
	protected final LuceneIndex _luceneIndex;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR                                                                          
/////////////////////////////////////////////////////////////////////////////////////////
	protected LuceneIndexerProviderBase(final Class<M> indexableObjectType,
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