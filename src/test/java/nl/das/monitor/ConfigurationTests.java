package nl.das.monitor;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;

public class ConfigurationTests {
  
  @Test
  public void testToJson() throws FileNotFoundException, IOException {
    String propFilePath = "webui.properties";
    Configuration cfg = new Configuration();
    cfg.load(new FileInputStream(propFilePath));
    JsonObject json = cfg.convertPropertiesToJson();
    System.out.println(json.toString());
    JsonObject jsonObject = Json.createReader(new StringReader(json.toString())).readObject();
    Configuration cfg1= new Configuration();
    cfg1.convertJsonToProperties(jsonObject);
    assertEquals("", cfg.toString(), cfg1.toString());
  }

  @Test
  public void testToProperties() {
    Configuration cfg = new Configuration();
    String json = "{\"host\":\"localhost\", \"port\":4500, \"tcu\":[{\"name\":\"tcu-kweek\",\"ipaddress\":\"192.168.178.162\"},{\"name\":\"tcu-test\",\"ipaddress\":\"192.168.178.125\"}]}";
    JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
    cfg.convertJsonToProperties(jsonObject);
    System.out.println(cfg.toString());
  }
}
