package base;

import static base.Config.*;
import static base.LogManager.*;

import exceptions.*;
import tasks.AdminTask;
import tasks.Task;

import java.util.*;

public class TaskPool {
	
	private static int taskId;

	private Map<Integer, Task> tasks;
	
	private static TaskPool self = new TaskPool();
	public static TaskPool getInstance() { return self; }

	private TaskPool() { }
	
	void setup() {
		taskId = 1;
		init();
	}
	
	private void init() {
		tasks = new HashMap<Integer, Task>();
	}
	
	public Task getTask(int taskId) {
		return tasks.get(taskId);
	}
	
	synchronized public boolean addTask(Task task) {
		if( tasks.size() >= tcpConfig.getIntProperty("MaximumTasks") ) {
			return false;
		}
		task.setTaskId(taskId++);
		tasks.put(task.getTaskId(), task);
		task.setPool(this);
		output("Task(" + task.getTaskId() + ") assigned", LOG_TASK);
		return true;
	}
	
	synchronized public void finishTask(Task task) {
		Task myTask = tasks.get(task.getTaskId());
		if( myTask == null ) {
			throw new ImplementationException("Task is not assigned");
		}
		if( myTask != task ) {
			throw new ImplementationException("finished unknown Task");
		}
		tasks.remove(task.getTaskId());
		output("Task(" + task.getTaskId() + ") finished", LOG_TASK);
	}
	
	// 一応 tasks を参照するので synchronized にしておく;
	synchronized public String getStatus() {
		StringBuilder status = new StringBuilder();
		status.append(tasks.size() + " tasks are assigned\n");
		for( Task task : tasks.values() ) {
			status.append(task.toString() + "\n");
			status.append(task.getStatus() + "\n");
		}
		return status.toString();
	}
	
	// tasks を参照するので...;
	synchronized public void clean() {
		for( Task task : tasks.values() ) {
			// clean の呼び出し元なので何もしない;
			if( task instanceof AdminTask ) { continue; }
			task.terminate();
		}
	}

}
