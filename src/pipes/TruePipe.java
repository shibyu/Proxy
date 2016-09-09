package pipes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tasks.Task;

public class TruePipe extends Pipe {

	public TruePipe(Task owner, InputStream input, OutputStream output, int bufferSize) {
		super(owner, input, output, bufferSize);
	}

	@Override
	public void transfer(byte[] buffer, int length) throws IOException {
		// 純粋なパイプなので、中身は書き換えない;
		output.write(buffer, 0, length);
		output.flush();
	}

}
