/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 04 Oct 2022.
 */

package nl.das.monitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;

/**
 *
 */
@Command(name = "tcu-monitor", version = "(c) 2023, Dutch Arrow Software")
public class Application {
	@Parameters(paramLabel = "<properties filepath>", defaultValue = "webui.properties", description = "Path of the properties file")
	static String configFilePath;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main (String[] args) throws IOException {
		Application app = new Application();
	     try {
	         ParseResult parseResult = new CommandLine(app).parseArgs(args);
	         if (!CommandLine.printHelpIfRequested(parseResult)) {
	 			if (!Files.exists(Paths.get(configFilePath))) {
	 				System.err.println("Properties file '" + configFilePath + "' not found");
				} else {
					app.runProgram();
				}
	         }
	     } catch (ParameterException ex) { // command line arguments could not be parsed
	         System.err.println(ex.getMessage());
	         ex.getCommandLine().usage(System.err);
		}
	}

	public void runProgram() throws IOException {
		try {
			Configuration cfg = new Configuration();
			cfg.load(new FileInputStream(configFilePath));
			Webserver srv = Webserver.getInstance(configFilePath, cfg);
			srv.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
