package at.porscheinformatik.sonarqube.licensecheck.maven;

import org.apache.maven.shared.invoker.InvokerLogger;
import org.slf4j.Logger;

public class Slf4jInvokerLogger implements InvokerLogger {

	private Logger logger = null;

	public Slf4jInvokerLogger(Logger loggerToWrap) {
		this.logger = loggerToWrap;
	}

	@Override
	public void debug(String message) {
		logger.debug(message);
	}

	@Override
	public void debug(String message, Throwable throwable) {
		logger.debug(message, throwable);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public void info(String message) {
		logger.info(message);
	}

	@Override
	public void info(String message, Throwable throwable) {
		logger.info(message, throwable);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public void warn(String message) {
		logger.warn(message);
	}

	@Override
	public void warn(String message, Throwable throwable) {
		logger.warn(message, throwable);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	@Override
	public void error(String message) {
		logger.error(message);
	}

	@Override
	public void error(String message, Throwable throwable) {
		logger.error(message, throwable);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	@Override
	public void fatalError(String message) {
		logger.error(message);
	}

	@Override
	public void fatalError(String message, Throwable throwable) {
		logger.error(message, throwable);
	}

	@Override
	public boolean isFatalErrorEnabled() {
		return logger.isErrorEnabled();
	}

	@Override
	public void setThreshold(int threshold) {
		// ignore
	}

	@Override
	public int getThreshold() {
		if (isDebugEnabled()) {
			return InvokerLogger.DEBUG;
		}
		if (isInfoEnabled()) {
			return InvokerLogger.INFO;
		}
		if (isWarnEnabled()) {
			return InvokerLogger.WARN;
		}
		if (isErrorEnabled()) {
			return InvokerLogger.ERROR;
		}
		return InvokerLogger.FATAL;
	}

}
