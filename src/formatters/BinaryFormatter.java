package formatters;

import java.io.*;

import contents.Content;
import rules.Rule;
import util.Util;

public class BinaryFormatter extends Formatter {
	
	private Rule rule;
	
	public BinaryFormatter(Rule rule) {
		this.rule = rule;
	}

	@Override
	public void setContent(Content content) {
		// TODO: UDP の場合 rule が null になるが、packet ヘッダなどは外部にあるので、中身をいじる必要はない...という仮定;
		if( rule != null ) {
			rule.format( content.getBytes() );
		}
		super.setContent(content);
	}

	@Override
	public void outputBytes(OutputStream output) throws IOException{
		// setContent の中で format 済みなので、ここでは特に何もしない;
		output.write( content.getBytes() );
	}
	
	public void addHeader(byte content[]) {
		// byte[] なので親の関数が呼ばれるが、Content になって返ってくるので、CustomRule#format が実行される;
		// 2回実行しても問題ないはずなので、気が向いたら呼び出してもいいのかも知れない;
		setContent(Util.mergeByteArray(new byte[rule.getHeaderSize()], content));
	}
	
}
