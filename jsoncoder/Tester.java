package jsoncoder;

import org.json.JSONException;

public class Tester 
{

	public static void main(String[] args) 
	{
		String json = "[{\"temp\":\"hello\", \"a\":{\"c\":1}}, {\"b\":[5, 7, {\"a\":1}, {\"d\":123}]}]";
		String key = "a";
		JsonCoder coder = new JsonCoder();
		System.out.println("Generating code to access the key \"" + key + "\" in json string:\n" + json + "\n");
		try
		{
			if (coder.parse(json, key))
			{
				System.out.println("***********  Code generated  *************\n");
				System.out.println(coder.generateCode(true));
			}
			else 
			{
				System.out.println("!!!!!!!!!!!  Failed to find key  !!!!!!!!!!!!!\n");
			}
		}
		catch(JSONException ex)
		{
			System.out.println("Failed to parse JSON. Exception:\n" + ex);
		}

		
	}

}
