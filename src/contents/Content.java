package contents;

import java.util.*;

import util.Util;

public class Content {
	
	protected byte content[];
	// TODO: 整理する (拡張としてはかなり雑...);
	protected boolean RUDPOnly;
	
	// 新規にカスタム Content を作るのもアレなので、追加情報を持てるようにしておく;
	private Map<String, String> additionalInfo;
	
	public Content(byte content[]) {
		this(content, 0, content.length);
	}
	
	public boolean isRUDPOnly() {
		return RUDPOnly;
	}
	
	public void setRUDPOnly(boolean RUDPOnly) {
		this.RUDPOnly = RUDPOnly;
	}

	public Content(byte content[], int offset, int length) {
		// 呼び出し元でデータが変更されると困るので複製しておく;
		this.content = Util.copyByteArray(content, offset, length);
		additionalInfo = new HashMap<String, String>();
	}

	public int length() {
		if( content == null ) { return 0; }
		return content.length;
	}

	public byte[] getBytes() {
		// TODO: content を返してしまうと改変されてしまうリスクがあるので、コピーを作ることを検討する;
		return content;
	}
	
	public void putAdditionalInfo(String key, String value) {
		additionalInfo.put(key, value);
	}
	
	public String getAdditionalInfo(String key) {
		return additionalInfo.get(key);
	}
	
}
