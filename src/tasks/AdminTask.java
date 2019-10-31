package tasks;

import static base.Config.*;
import static base.LogManager.*;

import java.io.*;
import java.nio.channels.*;

import base.InputController;
import processors.CommandLineProcessor;

public class AdminTask extends Task {

	public AdminTask(SocketChannel clientConnection, String category) throws IOException {
		super(clientConnection, category);
	}
	
	private void lineBreak() throws IOException {
		clientOutput.write("\n".getBytes());
	}

	@Override
	public void execute() {

		int bufferSize = tcpConfig.getBufferSize(CATEGORY_ADMIN);
		byte buffer[] = new byte[bufferSize];
		int length;
		CommandLineProcessor processor = new CommandLineProcessor(bufferSize);
		
		InputController controller = new InputController(clientInput);
		
		try {
			
			while( (length = controller.read(buffer)) > 0 ) {
				processor.process(buffer, length);
				while( processor.hasMoreContent() ) {
					String command = new String(processor.pullContent().getBytes());
					if( command.equalsIgnoreCase("shutdown") ) {
						clientOutput.write("bye".getBytes());
						lineBreak();
						//c TODO: リソースとかの解放を真面目にやる;
						//c どちらかというと中途半端な状態で close してまずいものがないようにしたい;
						System.exit(0);
					}
					else if( command.equalsIgnoreCase("stat") ) {
						clientOutput.write(pool.getStatus().getBytes());
						lineBreak();
					}
					else if( command.equalsIgnoreCase("clean") ) {
						pool.clean();
						clientOutput.write("task cleaned".getBytes());
						lineBreak();
					}
					else {
						clientOutput.write(("unknown command: " + command).getBytes());
						lineBreak();
					}
				}
			}
		}
		
		catch( IOException e ) {
			trace(e);
		}
	
	}
	
	@Override
	public String getTaskName() {
		return "Admin";
	}
	
	@Override
	public void terminate() {
		// これは Pipe を使っていないので何もしない;
	}

}
