package formatters;

import java.io.IOException;
import java.io.OutputStream;

import contents.Content;

abstract public class Formatter {
	
	protected Content content;
	
	public void setContent(byte content[]) {
		this.setContent(new Content(content));
	}

	public void setContent(Content content) {
		this.content = content;
	}
	
	// 本来は byte[] を返却すべきな気がするが、動作効率を考えると単一の array にはならなさそう;
	// 複数の byte[] を返却できる実装も考えられるが、それはそれで煩雑なので、stream を渡してしまうことにする;
	// TODO: 複数の byte[] を wrap した中間 Object の作成を検討する;
	// とはいえ、データの wrap と出力くらいしかすることがないので、フィルタをかけたい需要でもない限りは不要だと思う;
	abstract public void outputBytes(OutputStream output) throws IOException;
	
	protected int getContentLength() {
		if( hasContent() == false ) { return 0; }
		return content.length();
	}
	
	protected boolean hasContent() {
		return content != null;
	}
	
}
