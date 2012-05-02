package jsoncoder;

public class ParsePoint {
	final public Class<?> returnType;
	final public Class<?> callerType;
	final public String param;
	public ParsePoint(Class<?> returnType, Class<?> callerType, String param)
	{
		this.param = param;
		this.callerType = callerType;
		this.returnType = returnType;
	}
}
