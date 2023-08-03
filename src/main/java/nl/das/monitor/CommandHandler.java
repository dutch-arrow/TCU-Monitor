/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 14 Oct 2022.
 */


package nl.das.monitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.bind.JsonbBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 *
 */
public class CommandHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(CommandHandler.class);

//  public static HttpClient client;
  private static String configFilePath;

	public CommandHandler(String configFile) {
    configFilePath = configFile;
	}

	@Override
	public void handleRequest (HttpServerExchange exchange) {

		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		if (exchange.getRequestMethod().toString().equalsIgnoreCase("POST")) {
			try {
				String upjson = new String(exchange.getInputStream().readAllBytes());
				JsonObject request = Json.createReader(new StringReader(upjson)).readObject();
				try {
					JsonObject response = (JsonObject) CommandHandler.class
							.getDeclaredMethod(request.getString("command"), JsonObject.class)
							.invoke(this, request.getJsonObject("data"));
					exchange.getResponseSender().send(response.toString());
				} catch (NoSuchMethodException e) {
					exchange.getResponseSender().send("{\"error\": \"Command '" + request.getString("command") + "' not implemented\"}");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			exchange.getResponseSender().send("{\"error\": \"Only POST commands are supported\"}");
		}
	}

  public JsonObject getProperties(JsonObject data) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create("http://" + data.getString("ipaddress") + "/properties"))
      .build();
    HttpResponse<String> response = client.send(request,  BodyHandlers.ofString());
    JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
    JsonObject retval = jsonReader.readObject();
		return retval;
  }

  public JsonObject getTimers(JsonObject data) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    JsonObject props = getProperties(data);
    JsonArray devices = props.getJsonArray("devices");
    List<Timer> timers = new ArrayList<>();
    for (int i = 0, size = devices.size(); i < size; i++)
    {
      String device = devices.getJsonObject(i).getString("device");
      HttpRequest request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create("http://" + data.getString("ipaddress") + "/timers/" + device))
        .build();
      HttpResponse<String> response = client.send(request,  BodyHandlers.ofString());
      JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
      JsonArray jar = jsonReader.readObject().getJsonArray("timers");
      List<Timer> tmrs = new ArrayList<>();
      for (int x=0; x<jar.size(); x++) {
        Timer t = JsonbBuilder.create().fromJson(jar.get(x).toString(), Timer.class);
        tmrs.add(t);
      }
      timers.addAll(tmrs);
    }

    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
    for(Timer tmr : timers) {
        jsonArrayBuilder.add(Json.createObjectBuilder()
            .add("device", tmr.getDevice())
            .add("index", tmr.getIndex())
            .add("hour_on", tmr.getHour_on())
            .add("minute_on", tmr.getMinute_on())
            .add("hour_off", tmr.getHour_off())
            .add("minute_off", tmr.getMinute_off())
            .add("period", tmr.getPeriod())
            .add("repeat", tmr.getRepeat())
        );
    }
    JsonObject job = Json.createObjectBuilder()
      .add("timers", jsonArrayBuilder)
      .build();

		return job;
  }

	public JsonObject getHistDataFiles(JsonObject data) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create("http://" + data.getString("ipaddress") + "/history/temperature"))
      .build();
    HttpResponse<String> response = client.send(request,  BodyHandlers.ofString());
    JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
    JsonObject retval = jsonReader.readObject();
		return retval;
	}

	public JsonObject getTempDataFile(JsonObject data) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create("http://" + data.getString("ipaddress") + "/history/temperature/" + data.getString("file")))
      .build();
    HttpResponse<String> response = client.send(request,  BodyHandlers.ofString());
    JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
    JsonObject retval = jsonReader.readObject();
		return retval;
	}

	public JsonObject getStateDataFile(JsonObject data) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create("http://" + data.getString("ipaddress") + "/history/state/" + data.getString("file")))
      .build();
    HttpResponse<String> response = client.send(request,  BodyHandlers.ofString());
    JsonReader jsonReader = Json.createReader(new StringReader(response.body()));
    JsonObject retval = jsonReader.readObject();
		return retval;
	}

	public JsonObject saveConfig(JsonObject data) throws IOException, InterruptedException {
    Files.delete(Paths.get(configFilePath));
    Files.createFile(Paths.get(configFilePath));
    Configuration cfg = new Configuration();
    cfg.convertJsonToProperties(data);
    cfg.store(new FileOutputStream(configFilePath),"Last saved on :");
		return data;
	}

	public JsonObject getConfig(JsonObject data) throws IOException, InterruptedException {
    Configuration cfg = new Configuration();
    cfg.load(new FileInputStream(configFilePath));
    return cfg.convertPropertiesToJson();
	}
  
}
