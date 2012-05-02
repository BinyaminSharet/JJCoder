package jsoncoder;

import java.util.Iterator;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonCoder 
{
	private final Stack<ParsePoint> pstack = new Stack<ParsePoint>();
	private String key;
	
	
	private boolean parse(JSONObject jobj) throws JSONException
	{
		@SuppressWarnings("unchecked")
		Iterator<String> keys = (Iterator<String>)jobj.keys();
		Iterator<String> iter = keys;
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

	public String generateCode(boolean methodWrap)
	{
		return new CodeGen(pstack).generateCode(methodWrap);
	}
	private String qoute(String s) {
		return "\"" + s + "\"";
	}
	
}
