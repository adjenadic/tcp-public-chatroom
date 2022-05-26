import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread implements Runnable {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final String clientUsername;

    public static ArrayList<ServerThread> serverThreads = new ArrayList<>(); // Lista svih aktivnih klijenata za slanje

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        clientUsername = in.readLine();
        serverThreads.add(this);

        msgBroadcast("New user: " + clientUsername + ".");
    }

    public void msgBroadcast(String msgTo) throws IOException {
        for (ServerThread serverThread : serverThreads) {
            if (!serverThread.clientUsername.equals(clientUsername)) {
                serverThread.out.write(msgTo);
                serverThread.out.newLine();
                serverThread.out.flush();
            }
        }
    }

    @Override
    public void run() {
        String msgFrom;
        try {
            while (true) {
                try {
                    msgFrom = in.readLine();
                    if (msgFrom.equals("/exit")) {
                        break;
                    }

                    msgBroadcast(msgFrom);
                } catch (IOException e) {
                    break;
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
