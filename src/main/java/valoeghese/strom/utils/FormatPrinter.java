package valoeghese.strom.utils;

@FunctionalInterface
public interface FormatPrinter {
	void printf(String format, Object... args);
}
