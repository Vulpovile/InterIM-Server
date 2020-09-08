package ca.vulpovile.interim;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
	SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd, kk:mm:ss] ");
	@Override
	public String format(LogRecord record) {
		// TODO Auto-generated method stub
		return format.format(record.getMillis()) + "[" + record.getLevel().getName() + "] "+ record.getMessage() + System.getProperty("line.separator");
	}

}
