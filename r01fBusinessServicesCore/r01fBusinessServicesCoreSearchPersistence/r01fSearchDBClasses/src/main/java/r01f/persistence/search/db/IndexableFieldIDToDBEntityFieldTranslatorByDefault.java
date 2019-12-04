package r01f.persistence.search.db;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.model.metadata.FieldID;
import r01f.model.search.SearchFilter;

/**
 * Default indexable field id to DB entity field name
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public class IndexableFieldIDToDBEntityFieldTranslatorByDefault<F extends SearchFilter>  
  implements TranslatesIndexableFieldIDToDBEntityField {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS                                                                          
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final F _filter;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String dbEntityFieldNameFor(final FieldID fieldId) {
		String outFieldName = fieldId.getId();
		if (!outFieldName.startsWith("_")) outFieldName = "_" + outFieldName;
		return outFieldName;
	}

}
