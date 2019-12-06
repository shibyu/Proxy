package base;

import static base.Config.*;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class LogManager {
	
	private int outputLevel;
	
	public static final int OUTPUT_TRACE = 0;
	public static final int OUTPUT_INFO = 10;
	public static final int OUTPUT_WARNING = 20;
	public static final int OUTPUT_ERROR = 30;
	public static final int OUTPUT_FATAL = 40;
	//c debug の場合は何でもかんでも出力してしまおう;
	public static final int OUTPUT_DEBUG = 50;
	//c 常に出力したいものもあってもいいかな？;
	public static final int OUTPUT_ALWAYS = 100;

	public static final String LOG_LISTEN = "Listen";
	public static final String LOG_TASK = "Task";
	public static final String LOG_CONNECT = "Connection";
	public static final String LOG_RAW_DATA = "RawData";
	public static final String LOG_DNS = "DNS";
	
	// singleton;
	private static final LogManager self = new LogManager();
	
	private PrintStream stdout;
	
	private LogManager() {
		try {
			stdout = new PrintStream(System.out, true, "UTF-8");
		}
		catch( UnsupportedEncodingException e ) {
			//c ここでお亡くなりになられるとどうしようもないので、エラー出力を使う;
			e.printStackTrace(System.err);
		}
	}
	
	public static void setup() {
		self.outputLevel = tcpConfig.getLogLevel();
	}
	
	public static void trace(String message) {
		output(message, OUTPUT_TRACE);		
	}
	
	public static void info(String message) {
		output(message, OUTPUT_INFO);
	}
	
	public static void warn(String message) {
		output(message, OUTPUT_WARNING);
	}
	
	public static void error(String message) {
		output(message, OUTPUT_ERROR);
	}
	
	public static void fatal(String message) {
		output(message, OUTPUT_FATAL);
	}
	
	public static void output(String message, String key) {
		output(message, getLogLevel(key));
	}
	
	synchronized public static void output(String message, int level) {
		if( level < self.outputLevel ) { return; }
		self.stdout.println(message);
	}
	
	public static void trace(Exception e) {
		output(e, OUTPUT_TRACE);
	}

	public static void info(Exception e) {
		output(e, OUTPUT_INFO);
	}
	
	public static void warn(Exception e) {
		output(e, OUTPUT_WARNING);
	}
	
	public static void error(Exception e) {
		output(e, OUTPUT_ERROR);
	}
	
	public static void fatal(Exception e) {
		output(e, OUTPUT_FATAL);
	}

	synchronized public static void output(Exception e, int level) {
		if( level < self.outputLevel ) { return; }
		e.printStackTrace(self.stdout);
	}
	
	private static int getLogLevel(String key) {
		return tcpConfig.getIntProperty(CATEGOLY_LOG, key, true, OUTPUT_FATAL);
	}

}
