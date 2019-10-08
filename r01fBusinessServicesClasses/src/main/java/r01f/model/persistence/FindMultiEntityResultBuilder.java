package r01f.model.persistence;

import java.util.Collection;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.model.ModelObject;
import r01f.model.persistence.FindResultBuilder.FindResultBuilderForError;
import r01f.patterns.IsBuilder;
import r01f.persistence.db.DBEntity;
import r01f.securitycontext.SecurityContext;
import r01f.util.types.collections.CollectionUtils;

/**
 * Builder type for {@link FindResult}-implementing types:
 * <ul>
 * 		<li>A successful FIND operation result: {@link FindOK}</li>
 * 		<li>An error on a FIND operation execution: {@link FindError}</li>
 * </ul>
 * If the find operation execution was successful and entities are returned:
 * <pre class='brush:java'>
 * 		FindOK<MyEntity> opOK = FindResultBuilder.using(securityContext)
 * 											     .on(MyEntity.class)
 * 												  	   .foundEntities(myEntityInstances);
 * </pre>
 * If an error is raised while executing an entity find operation:
 * <pre class='brush:java'>
 * 		FindError<MyEntity> opError = FindResultBuilder.using(securityContext)
 * 													   .on(MyEntity.class)
 * 														   	.errorFindingEntities()
 * 																.causedBy(error);
 * </pre>
 */
@GwtIncompatible
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class FindMultiEntityResultBuilder 
  implements IsBuilder {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static FindResultBuilderEntityStep using(final SecurityContext securityContext) {
		return new FindMultiEntityResultBuilder() {/* nothing */}
						.new FindResultBuilderEntityStep(securityContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class FindResultBuilderEntityStep {
		private final SecurityContext _securityContext;
		
		public <T> FindResultBuilderOperationStep<T> on(final Class<T> entityType) {
			return new FindResultBuilderOperationStep<T>(_securityContext,
														 entityType);
		}
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class FindResultBuilderOperationStep<T> {
		protected final SecurityContext _securityContext;
		protected final Class<T> _entityType;
		
		//  --------- ERROR
		public FindResultBuilderForError<T> errorFindingEntities() {
			return (new FindResultBuilder()).new FindResultBuilderForError<T>(_securityContext,
																			  _entityType);	
		}
		// ---------- SUCCESS FINDING
		public <DB extends DBEntity> FindResultBuilderDBEntityTransformerStep<DB,T> foundDBMultiEntities(final Collection<Object> dbEntities) {
			return new FindResultBuilderDBEntityTransformerStep<DB,T>(_securityContext,
																	  _entityType,
																	  dbEntities);
		}
		
		public FindOK<T> noEntityFound() {
			FindOK<T> outFoundEntities = new FindOK<T>();
			outFoundEntities.setFoundObjectType(_entityType);
			outFoundEntities.setRequestedOperation(PersistenceRequestedOperation.FIND);
			outFoundEntities.setPerformedOperation(PersistencePerformedOperation.FOUND);
			outFoundEntities.setMethodExecResult(Lists.<T>newArrayList());	// no data found
			return outFoundEntities;
		}
	}	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public final class FindResultBuilderDBEntityTransformerStep<DB extends DBEntity,
																T> {
		protected final SecurityContext _securityContext;
		protected final Class<T> _entityType;
		protected final Collection<Object> _dbEntities;
		
		@SuppressWarnings("unchecked")
		public <M extends ModelObject> FindOK<M> transformedToModelObjectsUsing(final Function<Object,M> transformer) {
			Collection<M> entities = null;
			if (CollectionUtils.hasData(_dbEntities)) {
				entities = FluentIterable.from(_dbEntities)
										 .transform(transformer)
										  .filter(Predicates.notNull())
										  	.toList();
			} else {
				entities = Sets.newHashSet();
			}
			return FindResultBuilder._buildFoundEntitiesCollection(entities,
												 (Class<M>)_entityType);
		}
		
	}

}
