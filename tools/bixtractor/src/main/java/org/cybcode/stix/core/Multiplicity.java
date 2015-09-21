package org.cybcode.stix.core;

import org.cybcode.stix.api.OutputMode;
import org.cybcode.stix.api.StiXFunction;
import org.cybcode.stix.api.StiXecutorPushContext;
import org.cybcode.stix.api.StiXtractor;
import org.cybcode.stix.core.xecutors.XecutorFail;

public enum Multiplicity { 
	ONLY {
		@Override public <P0, T> MultiplicityParameter<P0, T> createParameter(StiXtractor<? extends P0> p0, StiXFunction<P0, ? extends T> fn)
		{
			return new MultiplicityParameter<P0, T>(p0, fn) 
			{
				@Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue) 
				{
					if (pushedValue == null) return null;
					context.setNextState(XecutorFail.getInstance());
					context.setInterimValue(pushedValue);
					return null;
				}
				@Override public boolean isRepeatable() { return false; }
			};
		}
		public boolean usesInterim() { return true; }
	},
	FIRST {
		@Override public <P0, T> MultiplicityParameter<P0, T> createParameter(StiXtractor<? extends P0> p0, StiXFunction<P0, ? extends T> fn)
		{
			return new MultiplicityParameter<P0, T>(p0, fn) 
			{
				@Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue) 
				{
					if (pushedValue == null) return null;
					context.setFinalState();
					return convert(pushedValue);
				}
				@Override public boolean isRepeatable() { return false; }
			};
		}
		@Override public int getOperationComplexity() { return 10; }
	},
	LAST {
		@Override public <P0, T> MultiplicityParameter<P0, T> createParameter(StiXtractor<? extends P0> p0, StiXFunction<P0, ? extends T> fn)
		{
			return new MultiplicityParameter<P0, T>(p0, fn) 
			{
				@Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue) 
				{
					if (pushedValue == null) return null;
					context.setInterimValue(pushedValue);
					return null;
				}
				@Override public boolean isRepeatable() { return false; }
			};
		}
		public boolean usesInterim() { return true; }
	}, 
	ALL {
		@Override public <P0, T> MultiplicityParameter<P0, T> createParameter(StiXtractor<? extends P0> p0, StiXFunction<P0, ? extends T> fn)
		{
			return new MultiplicityParameter<P0, T>(p0, fn)
			{
				@Override public T evaluatePush(StiXecutorPushContext context, Object pushedValue) 
				{ 
					return pushedValue == null ? null : convert(pushedValue); 
				}
			}; 
		}
		@Override public OutputMode getOutputMode(boolean multiple) { return OutputMode.pushMode(multiple); } 
	};
	
	public abstract <P0, T> MultiplicityParameter<P0, T> createParameter(StiXtractor<? extends P0> p0, StiXFunction<P0, ? extends T> fn);
	public int getOperationComplexity() { return 1000; }
	public boolean usesInterim() { return false; }
	
	public OutputMode getOutputMode(boolean repeatable) 
	{
		if (repeatable) throw new IllegalArgumentException();
		return OutputMode.REGULAR; 
	};
}