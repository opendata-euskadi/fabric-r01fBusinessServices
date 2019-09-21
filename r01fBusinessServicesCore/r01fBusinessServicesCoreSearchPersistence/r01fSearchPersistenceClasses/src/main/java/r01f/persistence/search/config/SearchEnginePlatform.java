package r01f.persistence.search.config;

import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;

public enum SearchEnginePlatform
 implements EnumExtended<SearchEnginePlatform> {
	LUCENE,
	DB;
	
	private final static transient EnumExtendedWrapper<SearchEnginePlatform> DELEGATE = EnumExtendedWrapper.wrapEnumExtended(SearchEnginePlatform.class);
/////////////////////////////////////////////////////////////////////////////////////////
//	                                                                          
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isIn(final SearchEnginePlatform... els) {
		return DELEGATE.isIn(this,els);
	}
	@Override
	public boolean is(final SearchEnginePlatform el) {
		return DELEGATE.is(this,el);
	}
	public boolean isNOTIn(final SearchEnginePlatform... els) {
		return DELEGATE.isNOTIn(this,els);
	}
	public boolean isNOT(final SearchEnginePlatform el) {
		return DELEGATE.isNOT(this,el);
	}
}
