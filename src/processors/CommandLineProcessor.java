package processors;

import java.io.IOException;

import contents.Content;

public class CommandLineProcessor extends Processor {
	
	public CommandLineProcessor(int bufferSize) {
		super(bufferSize);
	}

	@Override
	public void process(byte[] buffer, int offset, int length) throws IOException {
		for( int position = offset; position < length; ++position ) {
			if( buffer[position] == '\n' ) {
				queueContent(new Content(innerBuffer, 0, innerPointer));
				continue;
			}
			append(buffer[position]);
		}
	}

}
