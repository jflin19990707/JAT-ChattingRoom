import javax.swing.*;
import javax.swing.plaf.FontUIResource;


import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;


public class ClientGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JLabel label;
    private JLabel labelServer;
    private JLabel labelPort;
    
    private JTextField tfServer, tfPort,tfUser,tfInput;
    private JButton login, logout, submit,onlineb,cleanAll;

    private JTextArea ta;
    private JLabel online_num;

    private boolean connected;

    private Client client;
    private int defaultPort;
    private String defaultHost;


    ClientGUI(String host, int port) {

        super("万体钓鱼聊天室");
        defaultPort = port;
        defaultHost = host;

        // 左侧Server，port，user，连接，断开按钮以及图片布局      
        JPanel logoPanel = new JPanel(new GridLayout(1,1));
        JPanel northPanel = new JPanel(new GridLayout(2,1));
        JPanel leftside = new JPanel(new GridLayout(4,2));
        
        // Logo part
        JLabel logoLabel = new JLabel(new ImageIcon("img/logo.jpeg"));
        logoPanel.add(logoLabel);
        
        // Server part
        tfServer = new JTextField(host);
        tfServer.setHorizontalAlignment(SwingConstants.RIGHT);
        labelServer = new JLabel("<html><body><div style='color:#0066cc'>服务器地址:</div></body></html>", 
        		SwingConstants.CENTER);
        leftside.add(labelServer);
        leftside.add(tfServer);
        
        // Port part
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
        labelPort = new JLabel("<html><body><div style='color:#0066cc'>端口地址:</div></body></html>", 
        		SwingConstants.CENTER);
        leftside.add(labelPort);
        leftside.add(tfPort);
        
        // User part       
        label = new JLabel("<html><body><div style='color:#0066cc'>你的昵称:</div></body></html>", 
        		SwingConstants.CENTER);
        double d = Math.random();
        final int i = (int)(d*50);
        tfUser = new JTextField(""+i);
        tfUser.setHorizontalAlignment(SwingConstants.RIGHT);
        leftside.add(label);
        leftside.add(tfUser);
        
        // Connect and Disconnect part
        login = new JButton("<html><body><div style='color:#ff9966'>连接</div></body></html>");
        login.addActionListener(this);
        logout = new JButton("<html><body><div style='color:#ff9966'>断开</div></body></html>");
        logout.addActionListener(this);
        leftside.add(login);
        leftside.add(logout);
        
        // Add logoPanel and leftside to northPanel
        northPanel.add(logoPanel);
        northPanel.add(leftside);
        add(northPanel, BorderLayout.WEST);


        // Rightside:Chat window
        JPanel rightPanel = new JPanel(new GridLayout(1,1));
        ta = new JTextArea("", 80, 50);
        Color color = new Color(255,255,221);
        ta.setBackground(color);
        ta.setEditable(false);
        rightPanel.add(new JScrollPane(ta));
        add(rightPanel, BorderLayout.CENTER);

        // submit and online number part 
        JPanel userPanel=new JPanel(new GridLayout(1,2));
        submit = new JButton("<html><body><div style='color:#ff9966'>发送</div></body></html>");
        submit.addActionListener(this);
        cleanAll= new JButton("<html><body><div style='color:#ff9966'>清屏</div></body></html>");
        cleanAll.addActionListener(this); 
        onlineb= new JButton("<html><body><div style='color:#ff9966'>在线用户</div></body></html>");
        onlineb.addActionListener(this);
        
        userPanel.add(onlineb);
        userPanel.add(cleanAll);
        userPanel.add(submit);
        
        
        // disable some button before login
        onlineb.setEnabled(false);
        submit.setEnabled(false);
        logout.setEnabled(false);
        
        
        
        // message to input
        tfInput = new JTextField("输入昵称,连接后方可开始钓鱼...");
        tfInput.setEditable(false);

        // bottomside
        JPanel southPanel =new JPanel(new GridLayout(2,1));
        southPanel.add(tfInput);
        southPanel.add(userPanel);
        add(southPanel, BorderLayout.SOUTH);

        // window setting
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 750);
        setVisible(true);
        setLocationRelativeTo(null);
        tfUser.requestFocus();
    }

    // chat window to show message
    void append(String str) {
        ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);
    }

    void append2(int ol_num){
        online_num.setText(Integer.toString(ol_num));
    }

    /*
     * Change of unit when connection failed
     */
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        submit.setEnabled(false);
        onlineb.setEnabled(false);
        tfUser.setText("");
        tfInput.setEditable(false);

        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        tfServer.setEditable(true);
        tfPort.setEditable(true);

        tfUser.removeActionListener(this);
        connected = false;
    }


    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        // LOGOUT action
        if(o == logout) {
            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            tfInput.setEditable(false);
            tfUser.setEditable(true);
            tfServer.setEditable(true);
            submit.setEnabled(false);
            onlineb.setEnabled(false);
            tfInput.removeActionListener(this);
            tfInput.setText("输入昵称,连接后方可开始钓鱼...");
            return;
        }
        // CHECK online num action
        if(o == onlineb) {
            client.sendMessage(new ChatMessage(ChatMessage.ONLINE, ""));
            return;
        }
        // DEFAULT action
        if(connected && o == submit) {
            String tbt = tfInput.getText().trim();
            if(tbt.length()==0){
                ta.append("消息不能为空!\n");
                return;
            }
            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tfInput.getText()));
            tfInput.setText("");
            return;
        }
        // CLEAN all action
        if(o == cleanAll) {
        	ta.setText("");
        }
        // CONNECT action
        if(o == login) {
            String server = tfServer.getText().trim();
            if(server.length() == 0){
                ta.append("Server地址不能为空!\n");
                return;
            }
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0){
                ta.append("端口不能为空!\n");
                return;
            }
            String username = tfUser.getText().trim();
            if(username.length() == 0){
                ta.append("昵称不能为空!\n");
                return;
            }
            
            // port number check
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                ta.append("端口号不合法!\n");
                return;
            }

            // create a client connect
            client = new Client(server, port, username, this);
            if(!client.startLink())
                return;

            tfUser.setText(username);
            tfUser.setEditable(false);
            tfInput.setEditable(true);
            tfInput.setText("");
            connected = true;

            // disable login button
            login.setEnabled(false);
            
            // enable submit, logout and online button
            submit.setEnabled(true);
            logout.setEnabled(true);
            onlineb.setEnabled(true);
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            
            // listen the message
            tfInput.addActionListener(this);
        }

    }

    public static void main(String[] args) {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            InitGlobalFont(new Font("", Font.ROMAN_BASELINE, 13));

        }catch(Exception e) {

        }
        new ClientGUI("10.181.253.89", 6269);
    }
    
    private static void InitGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys();
             keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }
}

