import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Client extends JFrame {
    private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String username;

    private JTextArea chatArea;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            closeAll(socket, in, out);
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 2022);
        String username = JOptionPane.showInputDialog("Enter your username.");
        if (username == null) {
            socket.close();
            System.exit(0);
        }

        Client client = new Client(socket, username);
        client.initGUI();
        client.msgListen();
        client.msgSend(username);
    }

    private void initGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setSize(480, 640);
        setTitle("TCP Chat - Client " + username);
        setResizable(false);
        setLayout(new BorderLayout());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);

        JScrollPane topPanel = new JScrollPane(chatArea);
        topPanel.setBackground(Color.LIGHT_GRAY);
        chatArea.setBackground(topPanel.getBackground());
        chatArea.setForeground(Color.BLACK);

        JLabel sendMsgLabel = new JLabel("Use /exit to leave the chat.");
        sendMsgLabel.setForeground(Color.BLACK);

        JTextArea sendMsgArea = new JTextArea(5, 35);
        sendMsgArea.setLineWrap(true);
        sendMsgArea.setWrapStyleWord(true);

        JButton sendMsgBtn = new JButton("Send");

        sendMsgBtn.addActionListener(e -> {
            String msgToSend = sendMsgArea.getText();
            try {
                if (msgToSend.contains("/exit")) {
                    socket.close();
                    System.exit(0);
                } else if (!msgToSend.isBlank()) {
                    msgSend(username + ": " + msgToSend.replaceAll("\\s+", " "));
                    chatArea.append(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + "\tYou: " + msgToSend + "\n");
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            sendMsgArea.setText("");
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

        bottomPanel.add(sendMsgLabel);
        bottomPanel.add(new JScrollPane(sendMsgArea));
        bottomPanel.add(sendMsgBtn);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);

        splitPane.setDividerLocation(screenSize.height / 3 + 150);
        splitPane.setEnabled(false);

        add(splitPane, BorderLayout.CENTER);
        setVisible(true);
    }

    public void msgSend(String msgToSend) throws IOException {
        if (socket.isConnected()) {
            out.write(msgToSend);
            out.newLine();
            out.flush();
        }
    }

    public void msgListen() {
        new Thread(() -> {
            String msgFrom;
            while (socket.isConnected()) {
                try {
                    msgFrom = in.readLine();
                    chatArea.append(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + "\t" + msgFrom + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void closeAll(Socket socket, BufferedReader in, BufferedWriter out) {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
