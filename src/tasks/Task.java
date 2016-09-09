package tasks;

import static base.Config.*;
import static base.LogManager.*;

import base.SocketManager;
import base.TaskPool;
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
		this.clientConnection = clientConnection;
		// time out の指定は必須;
		clientConnection.socket().setSoTimeout(tcpConfig.getTimeout());
		clientConnection.socket().setKeepAlive(true);
		clientInput = clientConnection.socket().getInputStream();
		clientOutput = clientConnection.socket().getOutputStream();
		this.category = category;
		setBufferSize(category);
	}
	
	protected void setBufferSize(String category) {
		bufferSize = tcpConfig.getBufferSize(category);
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
