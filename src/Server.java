import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server extends JFrame {
    private final ServerSocket serverSocket;
    private JTextArea chatArea;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2022);
        Server server = new Server(serverSocket);
        server.initGUI();
        server.initServer();
    }

    public void initServer() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            chatArea.append(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()) + ": A new client has connected!\n");
            ServerThread serverThread = new ServerThread(socket);
            Thread thread = new Thread(serverThread);
            thread.start();
        }
    }

    private void initGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setSize(480, 640);
        setTitle("TCP Chat - Server");
        setResizable(false);
        setLayout(new BorderLayout());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setForeground(Color.BLACK);

        JScrollPane panel = new JScrollPane(chatArea);
        panel.setBackground(Color.LIGHT_GRAY);
        chatArea.setBackground(panel.getBackground());

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }
}
