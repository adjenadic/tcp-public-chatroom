import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ServerThread implements Runnable {
    public static ArrayList<ServerThread> serverThreads = new ArrayList<>();
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final String clientUsername;

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        clientUsername = in.readLine();
        serverThreads.add(this);

        msgBroadcast("New user: " + clientUsername + ".");
    }

    @Override
    public void run() {
        String msgFrom;
        try {
            while (socket.isConnected()) {
                try {
                    msgFrom = in.readLine();
                    if (msgFrom.equals("/exit")) {
                        break;
                    }
                    msgBroadcast(msgFrom);
                } catch (IOException e) {
                    closeAll(socket, in, out);
                    break;
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void msgBroadcast(String msgTo) {
        for (ServerThread serverThread : serverThreads) {
            try {
                if (!serverThread.clientUsername.equals(clientUsername)) {
                    serverThread.out.write(msgTo);
                    serverThread.out.newLine();
                    serverThread.out.flush();
                }
            } catch (IOException e) {
                closeAll(socket, in, out);
            }
        }
    }

    public void closeAll(Socket socket, BufferedReader in, BufferedWriter out) {
        if (serverThreads.contains(this)) {
            removeServerThread();
        }
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

    public void removeServerThread() {
        msgBroadcast(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + "\t" + clientUsername + " has left the chat.");
        serverThreads.remove(this);
    }
}
