package com.surrealdb.refactor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;



public class ConvertJavatoJson {

	public static JsonElement convertObject(Object object) {
		  GsonBuilder builder = new GsonBuilder(); 
	      builder.setPrettyPrinting(); 
	      
	      Gson gson = builder.create(); 
	      
	      String jsonString = gson.toJson(object);
	      return JsonParser.parseString(jsonString);
	      
	}
}
