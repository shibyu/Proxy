package pipes;

import java.io.*;

import processors.BinaryProcessor;
import processors.Processor;
import tasks.Task;

public class ProcessorPipe extends Pipe {
	
	private Processor processor;
	
	public ProcessorPipe(Task owner, InputStream input, OutputStream output, int bufferSize, Processor processor) {
		super(owner, input, output, bufferSize);
		this.processor = processor;
	}

	@Override
	public void transfer(byte[] buffer, int length) throws IOException {
		processor.process(buffer, length);
		boolean showContent = false;
		while( processor.hasMoreContent() ) {
			++contentId;
			byte result[] = processor.pullContent().getBytes();
			int offset = 0;
			if( processor instanceof BinaryProcessor ) {
				offset = ((BinaryProcessor)(processor)).getHeaderSize();
			}
			// 空のメッセージは無視することとする;
			if( result.length - offset == 0 ) { continue; }
			if( showContent == false ) {
				output.write("-- message received --\n".getBytes());
				showContent = true;
			}
			output.write(result, offset, result.length - offset);
			output.write("\n".getBytes());
		}
		if( showContent ) {
			output.write("input> ".getBytes());
		}
	}

}
