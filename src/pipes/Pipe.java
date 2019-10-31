package pipes;

import static base.LogManager.*;

import base.*;
import exceptions.*;
import tasks.Task;

import java.io.*;

abstract public class Pipe extends Thread {

	// input => output のデータリレーをするのが目的だが、間に入っている以上はデータの改変もできる;
	// request と response で勝手が違うところもあるので、共通部分はここに記述して、後は個別に継承して使うこと;
	
	protected Task owner;
	
	private InputController controller;
	protected OutputStream output;
	
	protected int bufferSize;
	
	// Debug 用に使用する request の連番 ID;
	protected int contentId;

	public Pipe(Task owner, InputStream input, OutputStream output, int bufferSize) {
		this.owner = owner;
		if( bufferSize == 0 ) {
			// 設定だけの問題ではない可能性があるので、実装エラーにしておく;
			throw new ImplementationException("bufferSize should not be 0");
		}
		this.bufferSize = bufferSize;
		setInputStream(input);
		setOutputStream(output);
	}
	
	protected void setInputStream(InputStream input) {
		controller = new InputController(input);
	}
	
	protected void setOutputStream(OutputStream output) {
		this.output = output;
	}

	// output に流すところまでやらないとどうしようもないことが判明したので修正;
	abstract public void transfer(byte buffer[], int length) throws IOException;

	@Override	
	public void run() {
		
		int length;
		byte buffer[] = new byte[bufferSize];
		
		try {
			while( (length = controller.read(buffer)) > 0 ) {
				transfer(buffer, length);
			}
		}
		catch( IOException e ) {
			// Connection reset はこちらの都合ではない気がするので trace しないでおく (後でやっかいなことになるかも...？);
			if( e.getMessage() != null && e.getMessage().indexOf("Connection reset") >= 0 ) { return; }
			debug(e);
		}
		
	}
	
	protected void debug(Exception e) {
		//c まとめて出力しないと割り込まれてしまうので...;
		StringBuilder message = new StringBuilder();
		message.append("---- error ----\n");
		if( owner != null ) {
			message.append("IOException @ owner task " + owner + "\n");
			message.append(owner.getStatus() + "\n");
		}
		message.append(e.getMessage() + "\n");
		message.append("processed " + contentId + " contents\n");
		message.append("---- ----- ----");
		output(message.toString(), 10);
	}
	
	// input の読み込みを停止させる;
	public void terminate() {
		//c Socket の読み込みに timeout が設定されている場合に限られるが、そのタイミングで終了するようにする;
		//c なのでちょっと待てば (join すれば) そのうち終わるはず;
		controller.disable();
	}
	
}
