package jsoncoder;

import org.json.JSONException;

public class Tester 
{

	public static void main(String[] args) 
	{
		//String json = "[{\"temp\":\"hello\", \"a\":{\"c\":1}}, {\"b\":[5, 7, {\"a\":1}, {\"d\":123}]}]";
		String json = "{\"results\" : ["+
		"1,"+
		"{" +
		     "\"value1\" : {" +
		     	"\"sub-value1\" : {" +
		     		"\"sub-sub-value11\" : \"This is one value\"," +
		     		"\"sub-sub-value12\" : \"This is one more..\"" +
		     	"},"+
		        "\"sub-value2\" : {" +
		           "\"sub-sub-value21\" : \"This is one value\"," +
		           "\"sub-sub-value22\" : \"This is one more..\"" +
		        "}"+
		     "},"+
		  "}"+
		"]}";
		String[] keys = {"results", "value1", "sub-value2", "sub-sub-value22"};
		JsonCoder coder = new JsonCoder();
		for (String key : keys)
		{
			System.out.println("\n******************************************\n");
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
				System.out.println("----------   Failed to parse JSON.   ----------\nException:\n" + ex);
			}
		}
		
	}

}
