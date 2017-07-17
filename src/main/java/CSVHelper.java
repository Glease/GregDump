import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.List;
import java.util.Objects;

/**
 * @author glease
 * @since 1.0
 */
public class CSVHelper {
	private final Writer out;



	public CSVHelper(@Nullable String file, String... headers) throws IOException {
		out = file == null ? new VoidWriter() : new BufferedWriter(new FileWriter(file));
		writeHeader(headers);
	}

	private void writeHeader(Object[] values) throws IOException {
		out.write(Objects.toString(values[0]));
		for (int i = 1; i < values.length; i++) {
			out.write(',');
			out.write(Objects.toString(values[i]));
		}
		out.write('\n');
		out.flush();
	}

	public void writeColumn(List<String> values) throws IOException {
		out.write(values.get(0));
		for (int i = 1; i < values.size(); i++) {
			out.write(',');
			out.write(values.get(i));
		}
		out.write('\n');
		out.flush();
	}
}

class VoidWriter extends Writer{
	VoidWriter() {
		super();
	}
	@Override
	public void write(int c) throws IOException {
	}

	@Override
	public void write(@NotNull char[] cbuf) throws IOException {
	}

	@Override
	public void write(@NotNull String str) throws IOException {
	}

	@Override
	public void write(@NotNull String str, int off, int len) throws IOException {
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		return this;
	}

	@Override
	public Writer append(char c) throws IOException {
		return this;
	}

	@Override
	public void write(@NotNull char[] cbuf, int off, int len) throws IOException {

	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void close() throws IOException {

	}
}