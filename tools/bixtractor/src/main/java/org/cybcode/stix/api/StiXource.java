package org.cybcode.stix.api;

import java.util.List;

import org.cybcode.stix.core.xecutors.XecutorFinal;

public abstract class StiXource<S, C, D, InnerFieldType> implements StiXtractor<InnerFieldType>
{
	public interface FieldTransformer {}
	
	private abstract class Xecutor implements StiXourceXecutor<D> 
	{
		@Override public boolean isPushOrFinal()
		{
			return true;
		}

		@Override public D getFieldDetails()
		{
			return StiXource.this.getFieldDetails();
		}
	};

	private final Parameter<InnerFieldType> p0;
	private final boolean allowRepeatedParamValue;
	
	public StiXource(StiXtractor<? extends S> p0, boolean repeatable)
	{
		@SuppressWarnings("unchecked") StiXtractor<? extends InnerFieldType> px0 = (StiXtractor<? extends InnerFieldType>) (StiXtractor<?>) p0;
		this.p0 = new PushParameter<InnerFieldType>(px0) 
		{
			@Override public InnerFieldType getValue(StiXParamContext context)
			{
				@SuppressWarnings("unchecked") S value = (S) context.getParamValue(getParamIndex());
				InnerFieldType result = prepareValue(value);
				return result;
			}
		};
		this.allowRepeatedParamValue = repeatable;
	}

	public StiXource(StiXource<?, ?, D, InnerFieldType> p0, boolean allowRepeatedParamValue)
	{
		this.p0 = new PushParameter<InnerFieldType>(p0);
		this.allowRepeatedParamValue = allowRepeatedParamValue;
	}
	
	@Override public boolean isRepeatable()
	{
		return true; //source can ALWAYS produce repeated values
	}

	protected abstract InnerFieldType prepareValue(S value);

	@Override public StiXourceXecutor<D> createXecutor(StiXecutorConstructionContext context)
	{
		List<? extends StiXourceNestedXecutor<?>> fields0 = context.getNestedXources();
		@SuppressWarnings("unchecked") List<StiXourceNestedXecutor<D>> fields = (List<StiXourceNestedXecutor<D>>) fields0;
		
		final C container = createFieldContainer(fields);
		if (allowRepeatedParamValue) {
			return new Xecutor() {
				@Override public StiXecutor push(StiXecutorContext context, StiXtractor.Parameter<?> pushedParameter, Object pushedValue)
				{
					@SuppressWarnings("unchecked") InnerFieldType value = (InnerFieldType) pushedValue;
					process(context, container, value);
					return this;
				}
			};
		} else {
			return new Xecutor() {
				@Override public StiXecutor push(StiXecutorContext context, StiXtractor.Parameter<?> pushedParameter, Object pushedValue)
				{
					@SuppressWarnings("unchecked") InnerFieldType value = (InnerFieldType) pushedValue;
					process(context, container, value);
					return XecutorFinal.getInstance();
				}
			};
		}
	}

	protected abstract void process(StiXecutorContext context, C container, InnerFieldType value);
	protected abstract C createFieldContainer(List<StiXourceNestedXecutor<D>> fields);
	protected abstract D getFieldDetails();

	@Override public final InnerFieldType evaluate(StiXecutorContext context)
	{
		return null;
	}

	@Override public final void visit(StiXtractor.Visitor visitor)
	{
		visitor.visitParameter(p0);
	}

	@Override public Object getOperationToken()
	{
		return getFieldDetails();
	}

	@Override public int paramCount()
	{
		return 1;
	}

	@Override public StiXtractor<InnerFieldType> curry(int parameterIndex, Object value)
	{
		return null;
	}
	
	@Override public int getOperationComplexity(StiXComplexityHelper helper)
	{
		return helper.getComplexityOf(this, 1000);
	}
}
