package tasks;

import static base.LogManager.*;
import static base.Constant.*;

import java.util.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import base.Config;
import contents.DataConverter;
import exceptions.ImplementationException;
import pipes.*;
import rules.RuleFactory;

// proxy からリクエストを受けて、Master に返すための worker Task;
// 複数の Master を相手に仕事をする可能性があるため、特定の Master が死んでいたとしても、仕事は完了しない;
// proxy にリクエストがいかなければ、必然的に仕事もなくなるため、proxy 側から切断される;

public class SlaveTask extends Task {
	
	private int type;
	
	private Pipe mergePipe;
	private Set<Integer> masterTaskIds;
	
	private boolean isTCP;
	
	private DataConverter converter;
	
	public SlaveTask(SocketChannel clientConnection, String category, int type, boolean isTCP) throws IOException {
		super(clientConnection, category);
		this.type = type;
		this.isTCP = isTCP;
		// TreeSet を使っておくと、出力が sort される...;
		masterTaskIds = new TreeSet<Integer>();
		switch( type ) {
		case TYPE_REQUEST:
			converter = Config.getConfig(isTCP).getRequestReverseConverter(category);
			break;
		case TYPE_RESPONSE:
			converter = Config.getConfig(isTCP).getResponseReverseConverter(category);
			break;
		default:
			throw new ImplementationException("unknown type: " + type);
		}
	}
	
	@Override
	public void execute() {
		// ここに request が届いたということは対応する master がいるということになる;
		// ただし master は TCP と UDP で異なる;
		try {
			// http => binary 変換をして formatter を request Queue に入れる;
			mergePipe = new MergePipe(this, clientInput, clientOutput, bufferSize, type, RuleFactory.getRule(category, isTCP), pool, converter);
			mergePipe.start();
			mergePipe.join();
		}
		catch( InterruptedException e ) {
			trace(e);
		}
	}
	
	@Override
	public String getTaskName() {
		return "Slave";
	}
	
	@Override
	public void terminate() {
		terminate(mergePipe);
	}
	
	public void recordMaster(int masterTaskId) {
		masterTaskIds.add(masterTaskId);
	}
	
	@Override
	public String getStatus() {
		StringBuilder status = new StringBuilder();
		status.append("Master:");
		for( int masterTaskId : masterTaskIds ) {
			status.append(" ").append(masterTaskId);
		}
		return status.toString();
	}

}
