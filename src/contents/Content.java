package contents;

import util.Util;

public class Content {
	
	protected byte content[];
	
	public Content(byte content[]) {
		this(content, 0, content.length);
	}

	public Content(byte content[], int offset, int length) {
		// 呼び出し元でデータが変更されると困るので複製しておく;
		this.content = Util.copyByteArray(content, offset, length);
	}

	public int length() {
		if( content == null ) { return 0; }
		return content.length;
	}

	public byte[] getBytes() {
		// TODO: content を返してしまうと改変されてしまうリスクがあるので、コピーを作ることを検討する;
		return content;
	}
	
}
