import java.net.*;
import java.io.*;
import java.util.*;


public class Client {
	
	private Socket socket;
	private ObjectInputStream socketInput;	
	private ObjectOutputStream socketOutput;
	private ClientGUI clientGUI;
	private String server,username;
	private int port;
	
	
	//construct function
	public Client(String server, int port, String username, ClientGUI clientGUI) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.clientGUI = clientGUI;
	}
	
	//start the link
	public boolean startLink() {
		try {
			socket = new Socket(server,port);
		}catch (Exception e) {
			show("连接到服务器异常!");
			return false;
		}
		String successMsg = "连接到" + socket.getInetAddress() + ":" +socket.getPort()+"成功!";
		show(successMsg);
		
		// initialize two Stream
		try {
			socketInput = new ObjectInputStream(socket.getInputStream());
			socketOutput = new ObjectOutputStream(socket.getOutputStream());
		}catch (Exception e) {
			show("创建I/O流发生异常了:"+ e);
			return false;
		}
		
		//create a thread to listen the message from Server
		new ListenMsgFromServer().start();
		
		try {
			socketOutput.writeObject(username);
		}catch (IOException e) {
			show("连接时发生错误："+e);
			disconnect();
			return false;
		}	
		return true;
	}
	
	//listen the message from server
	class ListenMsgFromServer extends Thread{
		@Override
		public void run() {
			while(true) {
				try {
					String msg = (String) socketInput.readObject();
					show(msg);
				} catch (Exception e) {
					show("你已经处于离线状态，与服务器断开连接了");
					clientGUI.connectionFailed();
					break;
				}
			}
		}
	}
	
	/*
	 * send message to the Server
	 */
	public void sendMessage(ChatMessage chMsg) {
		try {
			socketOutput.writeObject(chMsg);
		} catch (Exception e) {
			show("发送信息异常:" + e);
		}
	}
	
	/*
	 * disconnect
	 */
	private void disconnect() {
		//disconnect inputStream
		try {
			if(socketInput != null)
				socketInput.close();
		} catch (Exception e) {}
		
		//disconnect outputStream
		try {
			if(socketOutput != null)
				socketOutput.close();
		} catch (Exception e) {}
		
		//disconnect socket
		try {
			if(socket != null)
				socket.close();
		} catch (Exception e) {}
		
		//disconnect clientGUI
		if(clientGUI != null)
			clientGUI.connectionFailed();
	}
	
	/*
	 * show information on GUI
	 */
	private void show(String msg) {
		clientGUI.append(msg+"\n");
	}
}
