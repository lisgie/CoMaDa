package de.tum.in.net.WSNDataFramework.NodeAction;

import java.lang.reflect.Field;
import java.util.Map;

abstract public class NodeAction {
	public NodeActionCallback callback;

	public interface NodeActionCallback {
		public void finished(NodeAction action, boolean success);
	}

	public static NodeAction instantiate(Class<? extends NodeAction> action, Map<String,Object> params) throws Exception {
		NodeAction instance=null;

		// instantiate
		try {
			instance = action.newInstance();
		} catch (InstantiationException e) {
			throw e;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// set params
		for (String p: params.keySet()) {
			Field f = action.getField(p);
			Object value = null;

			// try to convert param
			if (f.getType() == int.class) {
				value = Integer.valueOf(params.get(p).toString());
			} else if (f.getType() == String.class) {
				value = params.get(p).toString();
			} else {
				throw new Exception("parameter type conversion not implemented");
			}
			f.set(instance, value);
		}

		return instance;
	}
}
