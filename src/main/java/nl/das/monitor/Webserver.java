/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 12 Oct 2022.
 */


package nl.das.monitor;

import org.slf4j.LoggerFactory;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.Headers;

/**
 *
 */
public class Webserver {

  private static String configFilePath;
	private Undertow server;
	private static Webserver instance;

	private Webserver(String propFilePath, Configuration config) {
		this.server = Undertow.builder()
			.addHttpListener(Integer.parseInt(config.getProperty("port")), config.getProperty("host"))
			.setHandler(setRoot()).build();
	}

	public static Webserver getInstance(String configFile, Configuration config) {
    configFilePath = configFile;
		if (instance == null) {
			instance = new Webserver(configFilePath, config);
		} else {
			System.out.println("Instance is not NULL");
		}
		return instance;
	}

	public void start() {
		this.server.start();
	}

	private static HttpHandler setRoutes() {
		HttpHandler routes = Handlers.path()
			// Redirect root path to /static to serve the index.html by default
			.addExactPath("/", Handlers.redirect("/static/index.html"))
			// Serve all static files from a folder
			.addPrefixPath("/static", Handlers.resource(new ClassPathResourceManager(Webserver.class.getClassLoader(), "static/")))
	        // REST API path
	        .addPrefixPath("/api", Handlers.routing()
	        		.post("/command", new BlockingHandler(new CommandHandler(configFilePath)))
	        		.setFallbackHandler(Webserver::notFoundHandler))
	        ;
		 return routes;
	}

	private static  HttpHandler setRoot() {
		return new AccessLogHandler(setRoutes(),
			new Slf4jAccessLogReceiver(LoggerFactory.getLogger("nl.das.accesslog")), "common",
			Webserver.class.getClassLoader());
	}

	public static void notFoundHandler (HttpServerExchange exchange) {
		exchange.setStatusCode(404);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
		exchange.getRequestPath();
		exchange.getResponseSender().send("Page '" + exchange.getRequestPath() + "' Not Found!!");
	}

}
