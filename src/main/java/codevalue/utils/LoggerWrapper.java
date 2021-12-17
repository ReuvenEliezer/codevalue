package codevalue.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerWrapper {

    private Logger logger;

    public LoggerWrapper(String name) {
        this.logger = LogManager.getLogger(name);
    }

    public void debugFormat(String methodName, String format, Object... args) {
        if (logger.isDebugEnabled()) {
            String newFormat = String.format("[%s] %s() %s", Thread.currentThread().getId(), methodName, format);
            logger.debug(String.format(newFormat, args));
        }
    }

    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public void errorFormat(String methodName, String format, Object... args) {
        //logger.error(String.format(methodName + "() " + format, args));
        String newFormat = String.format("[%s] %s() %s", Thread.currentThread().getId(), methodName, format);
        logger.error(String.format(newFormat, args));
    }
}
