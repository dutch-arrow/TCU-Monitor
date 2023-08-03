package nl.das.monitor;
import java.io.StringReader;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import pl.jalokim.propertiestojson.util.PropertiesToJsonConverter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

public class Configuration extends Properties {

  public void convertJsonToProperties(JsonValue json) {
      addJson("", json);
      return;
  }

  public void addJson(String root, JsonValue json) {

    // recursion for objects
    if (json instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) json;
      if (!root.equals("")) root += ".";
      for ( Entry<String, JsonValue> e : jsonObject.entrySet() ) {
        addJson(root + e.getKey(), e.getValue());
      }           
      return;
    }
    // recursion for arrays
    if (json instanceof JsonArray) {
        JsonArray jsonArray = (JsonArray) json;
        if (!root.equals("")) root += "[";
        int count = 0;
        for(final JsonValue e : jsonArray) {
            addJson(root+count+"]", e);
            count++;
        }
        return;
    }
    // leaves: add property
    this.setProperty(root, json.toString().replaceAll("\"",""));
  }

  // The overidden methods make the properties sorted
  @Override
  public Set<Object> keySet() {
      return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
  }

  @Override
  public Set<Map.Entry<Object, Object>> entrySet() {

      Set<Map.Entry<Object, Object>> set1 = super.entrySet();
      Set<Map.Entry<Object, Object>> set2 = new LinkedHashSet<Map.Entry<Object, Object>>(set1.size());

      Iterator<Map.Entry<Object, Object>> iterator = set1.stream().sorted(new Comparator<Map.Entry<Object, Object>>() {

          @Override
          public int compare(java.util.Map.Entry<Object, Object> o1, java.util.Map.Entry<Object, Object> o2) {
              return o1.getKey().toString().compareTo(o2.getKey().toString());
          }
      }).iterator();

      while (iterator.hasNext())
          set2.add(iterator.next());

      return set2;
  }

  @Override
  public synchronized Enumeration<Object> keys() {
      return Collections.enumeration(new TreeSet<Object>(super.keySet()));
  }

  public JsonObject convertPropertiesToJson() {
    String json = new PropertiesToJsonConverter().convertToJson(this);
    JsonObject retval = Json.createReader(new StringReader(json)).readObject();
    return retval;
  }
    
}