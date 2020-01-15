
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
    private static int uniqueId;
    private ArrayList<ClientThread> al;
    private int onum;
    private SimpleDateFormat sdf;
    private SimpleDateFormat sdf2;
    private int port;
    private boolean keepGoing;

    //Server construction
    public Server(int port) {
        this.port = port;
        sdf = new SimpleDateFormat("a hh:mm:ss");
        sdf2=new SimpleDateFormat("E ahh:mm");
        // Client list
        al = new ArrayList<ClientThread>();
    }

    public void start() {
        keepGoing = true;
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);
            show("服务端正在监听端口：" + port + ".");

            // 一直等待连接
            while(keepGoing)
            {

                Socket socket = serverSocket.accept();      
                ClientThread t = new ClientThread(socket);  
                // Add thread to ArrayList
                al.add(t);
                onum=al.size();
                t.start();
                broadcast(t.userName+" 进入了聊天室\n当前有"+onum+"人在线");

            }
        }

        catch (IOException e) {
            String msg = sdf.format(new Date()) + " 创建新的ServerSocket异常: " + e + "\n";
            show(msg);
        }
    }


    /*
     * show some message on CML
     */
    private void show(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);

    }

    /*
     *  Broadcast to all the user
     */
    private synchronized void broadcast(String message) {
        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        System.out.print(messageLf);


        for(int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            // send message to client, if fails then removed
            if(!ct.writeMsg(messageLf)) {
                al.remove(i);
                show("客户端 " + ct.userName + " 已下线，已从列表移除");
            }
        }
    }


    synchronized void remove(int id) {
        // scan the array to find the quit id
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);

            if(ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }

    public static void main(String[] args) {
        // 默认端口6269 除非新指定
        int portNumber = 6269;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("端口不合法！");
                    System.out.println("用法: > java Server [端口]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("用法: > java Server [端口]");
                return;

        }
        // start the server
        Server server = new Server(portNumber);
        server.start();
    }



    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream socketInput;
        ObjectOutputStream socketOutput;

        int id;
        String userName;
        ChatMessage cm;
        String date;

        ClientThread(Socket socket) {
            // unique thread ID
            id = ++uniqueId;
            this.socket = socket;
            System.out.println("尝试创建I/O流");
            try
            {
            	socketOutput = new ObjectOutputStream(socket.getOutputStream());
                socketInput  = new ObjectInputStream(socket.getInputStream());
                // get the user name
            	userName = (String) socketInput.readObject();
                show(userName + " 已连接");

            }
            catch (IOException e) {
            	show("创建I/O流异常: " + e);
                return;
            }
            catch (ClassNotFoundException e) {
            }
            date = sdf2.format(new Date()) + "\n";
        }

        public void run() {
            // loop until disconnect
            boolean keepGoing = true;
            while(keepGoing) {
                // read an object
                try {
                    cm = (ChatMessage) socketInput.readObject();

                }
                catch (IOException e) {
                	show(userName + " 读取输入流失败: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                String message = cm.getMessage();

                // Choose the message type
                switch(cm.getType()) {

                    case ChatMessage.MESSAGE:
                        broadcast(userName + " 说: \n" + message);
                        break;
                    case ChatMessage.LOGOUT:
                        keepGoing = false;
                        break;
                    case ChatMessage.ONLINE:
                        writeMsg(sdf.format(new Date()) +" 当前在线用户:" +  "\n");

                        for(int i = 0; i < al.size(); ++i) {
                            ClientThread ct = al.get(i);
                            writeMsg("("+(i+1) + ") " + ct.userName + " 连接时间:" + ct.date);
                        }
                        break;
                }
            }
            // remover from list
            remove(id);
            broadcast(userName+" 离开了聊天室\n当前"+al.size()+"人在线");
            close();
        }

        // Close
        private void close() {
            try {
                if(socketOutput != null) socketOutput.close();
            }
            catch(Exception e) {}
            try {
                if(socketInput != null) socketInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        /*
         * write message to outputStream
         */
        private boolean writeMsg(String msg) {
            if(!socket.isConnected()) {
                close();
                return false;
            }
            try {
                socketOutput.writeObject(msg);
            }
            catch(IOException e) {
            	show("发送消息给" + userName + "时产生异常");
            	show(e.toString());
            }
            return true;
        }
    }
}
