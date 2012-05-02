package jsoncoder;
/*
 * Get the return value
 * Get the object which is called
 * jObj = jArr.getJSONObject()
 * 
 * 
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonCoder 
{
	private static Map<Class<?>, String> vars = new HashMap<Class<?>, String>();
	private static Map<Class<?>, String> methods = new HashMap<Class<?>, String>();
	static
	{
		vars.put(JSONObject.class, 		"jObj");
		vars.put(JSONArray.class, 		"jArr");
		methods.put(JSONObject.class, 	"JSONObject");
		methods.put(JSONArray.class, 	"JSONArray");
	}
	private final Stack<ParsePoint> pstack = new Stack<ParsePoint>();  
	private String key;
	private final Set<Class<?>> objectsSet = new HashSet<Class<?>>();
	
	private boolean parse(JSONObject jobj) throws JSONException
	{
		Iterator<String> iter = jobj.keys();
		while (iter.hasNext()) {
			String currentKey = iter.next();
			if (currentKey.equals(key) ) 
			{
				pstack.push(new ParsePoint(jobj.get(currentKey).getClass(), JSONObject.class, qoute(currentKey)));
				return true;
			}
			Object current = jobj.get(currentKey); 
			if ( current instanceof JSONObject) 
			{
				if (parse((JSONObject) current)) 
				{
					pstack.push(new ParsePoint(JSONObject.class, JSONObject.class, qoute(currentKey)));
					return true;
				}
			}
			else if (current instanceof JSONArray) 
			{
				if (parse((JSONArray) current)) 
				{
					pstack.push(new ParsePoint(JSONArray.class, JSONObject.class, qoute(currentKey)));
					return true;
				}
			}

		}
		return false;
	}

	private boolean parse(JSONArray jarr) throws JSONException
	{
		for (int i = 0; i < jarr.length(); ++i) 
		{
			Object current = jarr.get(i);
			if (current instanceof JSONObject) 
			{
				if (parse((JSONObject)current)) 
				{
					pstack.push(new ParsePoint(JSONObject.class, JSONArray.class, ""+i));
					return true;
				}
			}
			else if (current instanceof JSONArray) 
			{
				if (parse((JSONArray) current)) 
				{
					pstack.push(new ParsePoint(JSONArray.class, JSONArray.class, ""+i));
					return true;
				}
			}
		}
		return false;
	}

	synchronized public boolean parse(String jsonString, String key) throws JSONException 
	{
		this.key = key;
		pstack.clear();
		try 
		{
			if (parse(new JSONObject(jsonString))) 
			{
				return true;
			}
		} 
		catch (JSONException ex)
		{
			// Empty exception - it might be JSONArray
		}
		if (parse(new JSONArray(jsonString))) 
		{
			return true;
		}
		return false;
	}

	private String buildAssignmentStatement(ParsePoint point, String align)
	{
		String retObj = vars.get(point.returnType);
		retObj = (retObj == null) ? "targetObj" : retObj;
		String callObj = vars.get(point.callerType);
		String getMethod = methods.get(point.returnType);
		getMethod = (getMethod == null) ? "" : getMethod; 
		String res = String.format("%s%s = %s.get%s(%s);\n", align, retObj, callObj, getMethod, point.param);
		objectsSet.add(point.returnType);
		return res;
	}

	private String buildReturnStatement(ParsePoint point)
	{
		String retObj = vars.get(point.returnType);
		retObj = (retObj == null) ? "targetObj" : retObj;
		return "\treturn " + retObj + ";\n}";
	}
	
	private String buildPrototype()
	{
		ParsePoint first = pstack.firstElement();
		String returnType = methods.get(first.returnType);
		if (returnType  == null)
		{
			returnType = "Object";
		}
		return "public static " + returnType + 
				" get" + returnType + "ForKey_" + key + "_FromJsonString(String theJsonString)" + 
				" throws JSONException" + "\n{\n";
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
		objectsSet.add(pstack.lastElement().callerType);
		if (pstack.lastElement().callerType == JSONObject.class) 
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

	private String qoute(String s) {
		return "\"" + s + "\"";
	}

	private static class ParsePoint {
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
	
}
