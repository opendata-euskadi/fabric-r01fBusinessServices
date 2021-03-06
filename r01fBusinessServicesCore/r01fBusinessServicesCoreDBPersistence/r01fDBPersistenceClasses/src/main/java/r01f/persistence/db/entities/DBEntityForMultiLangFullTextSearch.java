package r01f.persistence.db.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.google.common.collect.FluentIterable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.locale.Language;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObjectImpl;
import r01f.util.types.Strings;

/**
 * Utility [db entity] used to create a "bunch" of lang-dependent full-text search indexes
 * Given a [model object] like:
 * <pre class='brush:java'>
 * 		@Accessors(prefix="_")
 * 		public class MyLangDependentModelObj 
 * 	      implements ModelObject {
 * 			@Getter @Setter private MyOID _oid;
 * 			@Getter @Setter private LanguageTexts _name;	// <-- The name is MULTI-LINGUAL
 * 		} 
 * </pre>
 * Usually this [model object] is stored in a [db entity] like:
 * <pre class='brush:java'>
 * 		@Entity @Cacheable(false)
 *		@Table(name = "MYTABLE") 
 *			@IdClass(DBPrimaryKeyForVersionableModelObjectImpl.class) 
 * 		public class MyDBLangDependentObj 
 * 		     extends DBEntityForModelObject<DBPrimaryKeyForModelObject>
 * 		  implemenst DBEntityHasModelObjectDescriptor  {
 * 
 *				@Id @Column(name="OID",length=OID.OID_LENGTH) @Basic
 *   			@Getter @Setter protected String _oid;
 *   
 *				@Column(name="DESCRIPTOR") @Lob @Basic(fetch=FetchType.EAGER) 
 *				@Getter @Setter protected String _descriptor;		// <--- this colum stores the serialized [model object] (as xml or json)
 * 		}
 * </pre>
 * If full-text search by name is a requisite, the name in different languages must be available to be full-text search indexed
 * ... Oracle's XML column type supports full text search over specific XML tags: https://docs.oracle.com/cd/E11882_01/appdev.112/e23094/xdb09sea.htm#ADXDB4775 or https://www.oracle.com/technetwork/database/12coracletexttwp-1961244.pdf
 * 	   ... but this is a vendor-specific solution
 * 
 * In order to achieve a vendor-neutral solution, an aux table that contains "indexes" for every language is used:
 * <pre>
 * 		+-----+----+----+----+------+
 * 		+ oid | es | eu | en | .... |
 * 		+-----+----+----+----+------+
 * 		|     |    |    |    |      |
 * 		|     |    |    |    |      |
 * 		|     |    |    |    |      |
 * 		|     |    |    |    |      |
 * 		+-----+----+----+----+------+
 * </pre>
 * Each language-col contains text in that language that is full-text indexed
 * The only "disadvantage" is that this table must be updated every-time the "main" entity is updated
 * ... this is usually done by a background event
 * 
 * By default, the "index" columns length is 1000; this value can be overriden like:
 * <pre class='brush:java'>
 * 		@Entity @Cacheable(false)
 *		@Table(name = "MYTABLE") 
 *			@IdClass(DBPrimaryKeyForVersionableModelObjectImpl.class) 
 * 		
 * 		@AttributeOverrides({
 *	    	@AttributeOverride(name="_es",
 *	                       	   column=@Column(length = 500)),
 *	    	@AttributeOverride(name="_eu",
 *	                       	   column=@Column(length = 500))
 *	        ...
 *		})
 * 		public class MyDBMultiLangFullTextSearch 
 * 		     extends DBEntityForMultiLangFullTextSearch<DBPrimaryKeyForModelObject>  {
 * 
 *				@Id @Column(name="OID",length=OID.OID_LENGTH) @Basic
 *   			@Getter @Setter protected String _oid;
 * 		}
 * </pre>
 */
@MappedSuperclass

@Accessors(prefix="_")
@NoArgsConstructor
public abstract class DBEntityForMultiLangFullTextSearch
	 	   implements DBEntityForModelObject<DBPrimaryKeyForModelObject> {
	
	private static final long serialVersionUID = -5353374432025435871L;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static final int DEFAULT_LANG_TEXT_COL_LENGTH = 1000;
	public static final Language[] INDEXED_LANGS = new Language[] { Language.SPANISH,Language.BASQUE,
																	Language.ENGLISH,Language.FRENCH,Language.DEUTCH,Language.ITALIAN,Language.PORTUGUESE };
/////////////////////////////////////////////////////////////////////////////////////////
//	PRIMARY KEY
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Oid
	 */
	@Id @Column(name="OID",length=OID.OID_LENGTH * 2) @Basic
	@Getter @Setter protected String _oid;
/////////////////////////////////////////////////////////////////////////////////////////
//	OBJECT TYPE (ie: bag, structure, label...)
/////////////////////////////////////////////////////////////////////////////////////////
	@Column(name="OBJ_TYPE") @Basic
	@Getter @Setter protected String _objType;
/////////////////////////////////////////////////////////////////////////////////////////
//	LANGUAGE-COLUMS
/////////////////////////////////////////////////////////////////////////////////////////
	@Column(name="ES",length=DBEntityForMultiLangFullTextSearch.DEFAULT_LANG_TEXT_COL_LENGTH) @Basic
	@Getter @Setter protected String _es;
	
	@Column(name="EU",length=DBEntityForMultiLangFullTextSearch.DEFAULT_LANG_TEXT_COL_LENGTH) @Basic
	@Getter @Setter protected String _eu;
	
	@Column(name="EN",length=DBEntityForMultiLangFullTextSearch.DEFAULT_LANG_TEXT_COL_LENGTH) @Basic
	@Getter @Setter protected String _en;
	
	@Column(name="FR",length=DBEntityForMultiLangFullTextSearch.DEFAULT_LANG_TEXT_COL_LENGTH) @Basic
	@Getter @Setter protected String _fr;
	
	@Column(name="DE",length=DBEntityForMultiLangFullTextSearch.DEFAULT_LANG_TEXT_COL_LENGTH) @Basic
	@Getter @Setter protected String _de;
	
	@Column(name="IT",length=DBEntityForMultiLangFullTextSearch.DEFAULT_LANG_TEXT_COL_LENGTH) @Basic
	@Getter @Setter protected String _it;
	
	@Column(name="PT",length=DBEntityForMultiLangFullTextSearch.DEFAULT_LANG_TEXT_COL_LENGTH) @Basic
	@Getter @Setter protected String _pt;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public void setTextInLang(final Language lang,final String text) {
		String theText = Strings.isNOTNullOrEmpty(text) ? text : null;
		
		switch(lang) {
		case SPANISH:
			_es = theText;
			break;
		case BASQUE:
			_eu = theText;
			break;
		case ENGLISH:
			_en = theText;
			break;
		case FRENCH:
			_fr = theText;
			break;
		case DEUTCH:
			_de = theText;
			break;
		case ITALIAN:
			_it = theText;
			break;
		case PORTUGUESE:
			_pt = theText;
			break;
		case HUNGARIAN:
		case POLISH:
		case CZECH:
		case ROMANIAN:
		case SWEDISH:
		case RUSSIAN:
		case JAPANESE:
		case KOREAN:
		case ANY:
		default:
			throw new IllegalArgumentException(lang + " is NOT a db full-text search enabled lang!");
		}
	}
	public static boolean isSupportedLang(final Language lang) {
		return FluentIterable.from(INDEXED_LANGS)
							 .contains(lang);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PRIMARY KEY
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public DBPrimaryKeyForModelObjectImpl getDBEntityPrimaryKey() {
		return DBPrimaryKeyForModelObjectImpl.from(_oid);
	}
}
