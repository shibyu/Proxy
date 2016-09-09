package pipes;

import java.io.IOException;
import java.io.InputStream;

import tasks.Task;

public class VacantPipe extends Pipe {
	
	public VacantPipe(Task owner, InputStream input, int bufferSize) {
		super(owner, input, null, bufferSize);
	}

	@Override
	public void transfer(byte[] buffer, int length) throws IOException {
		// いわゆる /dev/null なので入力は捨てる;
		return;
	}

}
