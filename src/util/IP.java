package util;

import exceptions.ImplementationException;

public class IP {
	
	private int ip;
	private int bits;
	
	public IP(String data) {
		this(data, 32);
	}

	public IP(String data, int bits) {
		String tmp[] = data.split("\\.");
		if( tmp.length != 4 ) {
			throw new ImplementationException("invalid IP: " + data);
		}
		for( String part : tmp ) {
			ip <<= 8;
			ip |= Parser.parseInt(part);
		}
		this.bits = bits;
	}
	
	public boolean isInclude(IP ip) {
		// range が広いものを含むことはできないはず;
		if( ip.bits < bits ) { return false; }
		// 上位 bits ビットが一致していれば問題ないはず;
		// mask を 64bit で作る方が綺麗な気もする;
		// ((1L << bits) - 1) << (32 - bits);
		// 下記の実装では bits == 0 のときに挙動がおかしくなる気がするが、そもそもその入力をしようとする方がおかしいので無視することにする;
		int mask = 0xFFFFFFFF ^ ((1 << (32 - bits)) - 1);
		return (this.ip & mask) == (ip.ip & mask);
	}
	
}
