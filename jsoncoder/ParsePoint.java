package jsoncoder;

import org.json.JSONObject;

public class ParsePoint {
	final private Class<?> returnType;
	final private Class<?> callerType;
	final private String param;
	public ParsePoint(Class<?> returnType, Class<?> callerType, String param)
	{
		this.param = param;
		this.callerType = callerType;
		this.returnType = returnType;
	}
	
	public String getParamForMethodCall()
	{
		if (callerType == JSONObject.class)
		{
			return "\"" + param + "\"";
		}
		return param;
	}
	
	public String getParam()
	{
		return param;
	}

	public Class<?> getReturnType()
	{
		return returnType;
	}

	public Class<?> getCallerType()
	{
		return callerType;
	}
}
