package r01f.bootstrap.persistence;

import com.google.inject.TypeLiteral;

import r01f.inject.GuiceGenericBinding;
import r01f.model.IndexableModelObject;
import r01f.persistence.index.IndexerProvider;

public class IndexerProviderBinding<M extends IndexableModelObject>
	 extends GuiceGenericBinding<IndexerProvider<M>> {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public IndexerProviderBinding(final TypeLiteral<IndexerProvider<M>> genericType,
								  final Class<? extends IndexerProvider<M>> implementingType) {
		super(genericType,
			  implementingType);
	}
	public static <M extends IndexableModelObject> IndexerProviderBinding<M> of(final TypeLiteral<IndexerProvider<M>> typeLiteral,
																				final Class<? extends IndexerProvider<M>> implementingType) {
		return new IndexerProviderBinding<M>(typeLiteral,
											 implementingType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static class IndexerProviderType<M extends IndexableModelObject>
				extends TypeLiteral<IndexerProvider<M>> {
	}
}
