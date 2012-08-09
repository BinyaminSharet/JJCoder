package jsoncoder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

public class CodeGen {

	private final static Map<Class<?>, String> vars = new HashMap<Class<?>, String>();
	private final static Map<Class<?>, String> methods = new HashMap<Class<?>, String>();
	static
	{
		vars.put(JSONObject.class, 		"jObj");
		vars.put(JSONArray.class, 		"jArr");
		methods.put(JSONObject.class, 	"JSONObject");
		methods.put(JSONArray.class, 	"JSONArray");
	}
	private Stack<ParsePoint> pstack;
	private final Set<Class<?>> objectsSet = new HashSet<Class<?>>();
	
	private String buildAssignmentStatement(ParsePoint point, String align)
	{
		Class<?> returnType = point.getReturnType();
		String retObj = vars.get(returnType);
		retObj = (retObj == null) ? "targetObj" : retObj;
		String callObj = vars.get(point.getCallerType());
		String getMethod = methods.get(returnType);
		getMethod = (getMethod == null) ? "" : getMethod; 
		String res = String.format("%s%s = %s.get%s(%s);\n", align, retObj, callObj, getMethod, point.getParamForMethodCall());
		objectsSet.add(returnType);
		return res;
	}

	private String buildReturnStatement(ParsePoint point)
	{
		String retObj = vars.get(point.getReturnType());
		retObj = (retObj == null) ? "targetObj" : retObj;
		return "\treturn " + retObj + ";\n}";
	}
	
	private String buildPrototype()
	{
		ParsePoint first = pstack.firstElement();
		String returnType = methods.get(first.getReturnType());
		String key = first.getParam();
		if (returnType  == null)
		{
			returnType = "Object";
		}
		return "public static " + returnType + 
				" get" + returnType + "ForKey_" + synthesize(key) + "_FromJsonString(String theJsonString)" + 
				" throws JSONException" + "\n{\n";
	}
	
	private String synthesize(String str)
	{
		return str.replaceAll("[^A-Za-z0-9$]", "_");
	}
	private String buildDeclaration(String alignment)
	{
		String res = "";
		if (objectsSet.remove(JSONObject.class))
		{
			res += alignment + "JSONObject jObj;\n";
		}
		if (objectsSet.remove(JSONArray.class))
		{
			res += alignment + "JSONArray jArr;\n";
		}
		if (!objectsSet.isEmpty())
		{
			res += alignment + "Object targetObj;\n";
		}
		return res;
	}
	
	private String buildInstantiationStatement(String alignment)
	{
		String res;
		Class<?> lastElementCaller = pstack.lastElement().getCallerType();
		objectsSet.add(lastElementCaller);
		if (lastElementCaller == JSONObject.class) 
		{
			res = alignment + "jObj = new JSONObject(theJsonString);\n";
		}
		else 
		{
			res = alignment + "jArr = new JSONArray(theJsonString);\n";
		}
		return res;
	}
	
	public String generateCode(boolean methodWrap)
	{
		if (pstack.isEmpty())
		{
			throw new IllegalStateException("JSON not parsed, can't generate code");
		}
		StringBuilder sb = new StringBuilder();
		ParsePoint current;
		String alignment = methodWrap ? "\t" : "";
		String prototype = buildPrototype();
		sb.append(buildInstantiationStatement(alignment));
		do  
		{
			current = pstack.pop();
			sb.append(buildAssignmentStatement(current, alignment));
		}while (!pstack.empty());
		sb.insert(0, buildDeclaration(alignment));
		if (methodWrap)
		{
			sb.insert(0, prototype);
			sb.append(buildReturnStatement(current));
		}
		return sb.toString();
	}
	
	public CodeGen(Stack<ParsePoint> stack)
	{
		@SuppressWarnings("unchecked")
		Stack<ParsePoint> clone = (Stack<ParsePoint>)stack.clone();
		pstack = clone;
	}
}
