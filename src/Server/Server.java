package Server;
import java.io.*;
import java.net.*;
/**
 *
 * @author Wojciech Mucha
 */
/**
 * 
 * Server - class starts server, accepting new users.
 */
public class Server {
    public static final int portNumber = 12345;
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try{
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server started");
            //loading users from file
            ClientHandler.loadUsersFromFile();
            while(true){
                //accpeting new connection
                socket = serverSocket.accept();
                System.out.println("New client has connected " + socket);
                new ClientHandler(socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server can't be initialized :(");
            System.exit(1);
            //when error ocurred or not at the end of program we close sockets
        } finally {
            try{
                if(socket != null) socket.close();
                if(serverSocket != null) serverSocket.close();
                }
             catch (IOException ex) {
                 System.out.println("Error with closing server socket: " +ex.getMessage());
            }
        }
    }
}
