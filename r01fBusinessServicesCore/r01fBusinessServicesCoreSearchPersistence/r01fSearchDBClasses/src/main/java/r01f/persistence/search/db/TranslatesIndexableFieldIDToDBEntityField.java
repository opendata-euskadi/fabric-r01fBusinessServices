package r01f.persistence.search.db;

import r01f.model.metadata.FieldID;

/**
 * Any type that translates an indexable field id to a DB entity field
 */
public interface TranslatesIndexableFieldIDToDBEntityField {
	/**
	 * Returns the db entity field name from the given indexable field id
	 * The filter may also be handed to the type implementing this interface
	 * since the db entity field might depend on other filter conditions 
	 * such as the filtering language
	 * @param fieldId
	 * @return
	 */
	public String dbEntityFieldNameFor(final FieldID fieldId);
}
