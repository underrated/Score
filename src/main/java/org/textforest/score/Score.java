package org.textforest.score;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

class Score {

	private static final Logger log = Logger.getLogger(Score.class.getName());

	private static void help(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Score -i input.hpp -o output", options);		
	}

	public static void main(String[] args) throws IOException {

		Options options = new Options();
		options.addOption("i", true, "Path to input file.");
		options.addOption("o", true,
				"Path to generated files in the form <folder>/<name_root>. Two files will be created, one with the hpp extension added to the name root and one with the cpp extensions.");
		options.addOption("oh", true, "Path to the generated header file.");
		options.addOption("os", true, "Path to the generated source file.");
		options.addOption("hp", true, "Relative path to the included header used in the #include statement. e.g #include \"<header_path>/header.h\" ");
		options.addOption("h", false, "Print help message.");
		
		CommandLineParser clp = new DefaultParser();
		
		String inFile = "";
		String outHeaderFile = ""; //TODO : check that outHeaderFile is different than inFile
		String outSrcFile = ""; // TODO : check that outSrcFile is different than inFile
		String headerIncludePath = "";
		String headerIncludeStatement = "";
		boolean hasHeaderIncludePathOption = false;
		
		try {
			CommandLine cmd = clp.parse(options, args);
			boolean hasOptions = false;
			boolean hasInputs = false;
			boolean hasOutputs = false;
			
			if(cmd.hasOption("h")) {
				help(options);
				System.exit(0);
			}
			
			if(cmd.hasOption("i")) {
				inFile = cmd.getOptionValue("i");
				hasOptions = true;
				hasInputs = true;
			}
				
			if( cmd.hasOption("o") ) {
				hasOptions = true;
				if(hasInputs) {
					outHeaderFile = cmd.getOptionValue("o") + ".hpp";
					outSrcFile = cmd.getOptionValue("o") + ".cpp";
					hasOutputs = true;
				}
			} else if( cmd.hasOption("oh") && cmd.hasOption("os") ) {
				hasOptions = true;
				if(hasInputs) {
					outHeaderFile = cmd.getOptionValue("oh");
					outSrcFile = cmd.getOptionValue("os");
					hasOutputs = true;
				}
			} else if(!hasInputs && hasOptions) {
				System.err.println("No input file provided.");
				help(options);
				System.exit(1);
			} else if (!hasOutputs && hasOptions) {
				System.err.println("No output files provided.");
				help(options);
				System.exit(1);
			}
			
			if( cmd.hasOption("hp")) {
				headerIncludePath = cmd.getOptionValue("hp");
				headerIncludePath = headerIncludePath + (headerIncludePath.lastIndexOf('/') == headerIncludePath.length()-1 ? "" : "/") + outHeaderFile;
				hasHeaderIncludePathOption = true;
			}
			
			if(!hasOptions) {
				help(options);
				System.exit(0);
			}
			
		} catch (ParseException e) {
			log.log(Level.SEVERE, "Failed to parse command line properties", e);
			help(options);
		}
		
		FileOutputStream header = new FileOutputStream(outHeaderFile);
		FileOutputStream src = new FileOutputStream(outSrcFile);
		
		// Intro comments
		String introComments = "/* This code was generated automatically by Score */\n\n";
		header.write(introComments.getBytes());
		src.write(introComments.getBytes());
		
		// Include header in source
		if(!hasHeaderIncludePathOption) {
			File hdr = new File(outHeaderFile);
			headerIncludePath = hdr.getName();
		}
		headerIncludeStatement = "#include \"" + headerIncludePath + "\"\n\n";
		src.write(headerIncludeStatement.getBytes());

		CPP14Lexer lexer = new CPP14Lexer(new ANTLRInputStream(new FileInputStream(inFile)));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CPP14Parser parser = new CPP14Parser(tokens);
		ScoreListener listener = new ScoreListener(tokens, header, src);

		CPP14Parser.TranslationunitContext context = parser.translationunit();

		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(listener, context);
		
		header.close();
		src.close();
	}

}
