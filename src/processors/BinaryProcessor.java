package processors;

import static base.LogManager.*;

import java.io.IOException;

import contents.Content;
import exceptions.ImplementationException;
import rules.CustomRule;

public class BinaryProcessor extends Processor {
	
	private static final int BINARY_STATE_HEADER = 1;
	private static final int BINARY_STATE_CONTENT = 2;
	
	private int state;
	
	private CustomRule rule;
	
	public BinaryProcessor(int bufferSize, CustomRule rule) {
		super(bufferSize);
		this.rule = rule;
	}
	
	@Override
	protected void reset() {
		super.reset();
		state = BINARY_STATE_HEADER;
	}
	
	private void appendHeader(byte data) {
		if( innerPointer >= rule.getHeaderSize() ) {
			throw new ImplementationException("header size exceeded");
		}
		append(data);
		if( innerPointer >= rule.getHeaderSize() ) {
			parseHeader();
			state = BINARY_STATE_CONTENT;
		}
	}
	
	private void parseHeader() {
		if( rule.checkMagic(innerBuffer) == false ) {
			// TODO: magic word が想定と違う場合どうするか？;
			// 現状は何もしないことにしておく;
			trace(rule.emitMagicForDebug(innerBuffer));
		}
		setContentLength( rule.getContentLength(innerBuffer) );
	}
	
	public int getHeaderSize() {
		return rule.getHeaderSize();
	}
	
	private void queueContent() {
		int length = getContentLength();
		queueContent( new Content(innerBuffer, 0, length) );
	}
	
	@Override
	public void process(byte[] buffer, int offset, int length) throws IOException {
		
		while( offset < length ) {

			while( state == BINARY_STATE_HEADER && offset < length ) {
				appendHeader(buffer[offset++]);
			}
			
			if( state == BINARY_STATE_CONTENT ) {
				while( offset < length && reachContentEnd() == false ) {
					append(buffer[offset++]);
				}
				if( reachContentEnd() ) {
					output(innerPointer + " read", -1);
					// 中で reset が呼ばれるので state が header 読み状態に変更される;
					queueContent();
				}
			}
			
		}
		
	}

}
