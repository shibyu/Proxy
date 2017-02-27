package tasks;

import static base.Config.*;
import static base.LogManager.*;

import base.*;
import pipes.Pipe;

import java.nio.channels.SocketChannel;
import java.io.*;

public abstract class Task extends Thread {
	
	protected TaskPool pool;
	private int taskId;
	
	protected SocketChannel clientConnection;
	protected InputStream clientInput;
	protected OutputStream clientOutput;
	
	protected String category;
	protected int bufferSize;
	
	public Task(SocketChannel clientConnection, String category) throws IOException {
		this(clientConnection, category, false);
	}
	
	// 基本的に Task は TCP でしか作らないので TCP の設定を見れば良かったのだが、UDP を wrap し始めたので、設定が混在している;
	// そのフラグ管理が SlaveTask では必要なのだが、先にこちらのインスタンスが作られてしまうため、そのフラグに綺麗にアクセスできない;
	// ということで、設定に関する部分をスキップできるようにフラグを導入した...;
	// TODO: 明らかにひどい実装なので、そのうち見直したい...;
	public Task(SocketChannel clientConnection, String category, boolean skipSetBufferSize) throws IOException {
		this.clientConnection = clientConnection;
		// time out の指定は必須;
		clientConnection.socket().setSoTimeout(tcpConfig.getTimeout());
		clientConnection.socket().setKeepAlive(true);
		clientInput = clientConnection.socket().getInputStream();
		clientOutput = clientConnection.socket().getOutputStream();
		this.category = category;
		if( skipSetBufferSize == false ) {
			setBufferSize(category);
		}
	}
	
	protected Config getConfig() {
		return tcpConfig;
	}
	
	protected void setBufferSize(String category) {
		bufferSize = getConfig().getBufferSize(category);
	}
	
	public void finish() {
		SocketManager.close(clientConnection);
		clientConnection = null;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	
	public int getTaskId() { return taskId; }
	
	public void setPool(TaskPool pool) {
		this.pool = pool;
	}
	
	abstract public void execute();
	
	@Override
	public void run() {
		output("Task(" + getTaskId() + ") is ready", LOG_TASK);
		try {
			execute();
		}
		finally {
			pool.finishTask(this);
			finish();
		}
	}
	
	abstract public String getTaskName();

	@Override
	public String toString() {
		return getTaskName() + "Task#" + getTaskId();
	}
	
	public String getStatus() {
		return "no additional information";
	}
	
	abstract public void terminate();
	
	protected void terminate(Pipe pipe) {
		if( pipe != null ) { pipe.terminate(); }
	}
	
}
