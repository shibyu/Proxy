package processors;

import java.util.*;

import contents.Content;

import java.io.IOException;

import exceptions.ImplementationException;
import util.Util;

// TODO: 内部バッファがあふれた場合の処理を実装する必要がある...;

abstract public class Processor {
	
	protected Queue<Content> contents;

	private int bufferSize;
	
	protected byte innerBuffer[];
	protected int innerPointer;
	
	private int contentLength;
	
	protected Processor(int bufferSize) {
		this.bufferSize = bufferSize;
		contents = new LinkedList<Content>();
		innerBuffer = new byte[bufferSize];
		reset();
	}

	private void expandInnerBuffer() {
		// TODO: 取り敢えず 2倍の領域を確保する実装に変更してみたが、メモリを無駄に使うようであれば考え直す;
		byte buffer[] = new byte[bufferSize * 2];
		Util.writeByteArray(buffer, 0, innerBuffer, 0, bufferSize);
		innerBuffer = buffer;
		bufferSize = bufferSize * 2;
	}
	
	protected void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
	
	public int getContentLength() {
		return contentLength;
	}
	
	protected boolean reachContentEnd() {
		return innerPointer >= getContentLength();
	}
	
	protected void reset() {
		innerPointer = 0;
	}
	
	protected void queueContent(Content content) {
		contents.add(content);
		reset();
	}

	public boolean hasMoreContent() {
		return contents.size() > 0;
	}

	public Content getContent() {
		try {
			return contents.remove();
		}
		catch(NoSuchElementException e) {
			// hasMoreContent を呼び出してから実行しているはずなので、ここにくることはないはずなのだが...;
			throw new ImplementationException("no more content: " + e.getMessage());
		}
	}
	
	protected void append(byte data) {
		if( innerPointer >= bufferSize ) {
			// バッファが足りないので拡張する;
			expandInnerBuffer();
		}
		innerBuffer[innerPointer++] = data;
	}
	
	public void process(byte buffer[], int length) throws IOException {
		process(buffer, 0, length);
	}
	
	abstract public void process(byte buffer[], int offset, int length) throws IOException;
	
}
