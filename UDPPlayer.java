import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class UDPPlayer {
    private final Scanner scanner;
    private final int senderPort;
    private final DatagramSocket senderSocket;
    private final int listenerPort;
    private final DatagramSocket listenerSocket;
    private final InetAddress ipAddress;
    private final ArrayList<String> words = new ArrayList<>();

    public UDPPlayer(int senderPort, int listenerPort) throws SocketException, UnknownHostException {
        this.scanner = new Scanner(System.in);
        this.senderPort = senderPort;
        this.senderSocket = new DatagramSocket();
        this.listenerPort = listenerPort;
        this.listenerSocket = new DatagramSocket(listenerPort);
        this.ipAddress = InetAddress.getLocalHost();
    }

    public void startGame(boolean starter) {
        boolean handShakeStatus = startHandshaking(starter);
        if (!handShakeStatus) {
            System.out.println("Game couldn't start");
            return;
        }
        try {
            listenerSocket.setSoTimeout(10000);
            if (!starter) {
                String message;
                long startTime = new Date().getTime();
                System.out.println("Enter your message");
                message = scanner.nextLine();
                this.words.add(message);
                long finishTime = new Date().getTime();
                if (finishTime - startTime >= 10000) {
                    throw new SocketException();
                }
                sendUDPMessage(message);
            }
            while (true){
                try {
                    getMessage();
                } catch (SocketTimeoutException socketTimeoutException) {
                    System.out.println("Other player weren't able to answer in the given time, you won");
                    break;
                }
                try {
                    sendMessage();
                } catch (SocketException socketException) {
                    System.out.println("You weren't able to answer in the given time, other player won");
                    break;
                }
            }
        } catch (IOException ignored) { }
    }

    private boolean startHandshaking(boolean starter) {
        try {
            if (starter) {
                sendUDPMessage("START HANDSHAKING");
                listenerSocket.setSoTimeout(30000);
                String handShakingResponse = getUDPMessage();
                if (handShakingResponse.toUpperCase().equals("CONNECTED")) {
                    System.out.println("HAND SHAKING SUCCESSFUL");
                    return true;
                } else {
                    return false;
                }
            } else {
                listenerSocket.setSoTimeout(30000);
                String handShakingResponse = getUDPMessage();
                if (handShakingResponse.toUpperCase().equals("START HANDSHAKING")) {
                    sendUDPMessage("CONNECTED");
                    System.out.println("HAND SHAKING SUCCESSFUL");
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException ioException) {
            return false;
        }
    }

    private String getUDPMessage() throws IOException {
        byte[] responseAsBytes = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(responseAsBytes, responseAsBytes.length, ipAddress, listenerPort);
        listenerSocket.receive(responsePacket);
        return new String(responsePacket.getData()).trim();
    }

    private void sendUDPMessage(String message) throws IOException {
        byte[] messageAsBytes = message.getBytes();
        DatagramPacket messagePacket = new DatagramPacket(messageAsBytes,
                messageAsBytes.length,
                ipAddress,
                senderPort);
        senderSocket.send(messagePacket);
    }

    private void getMessage() throws IOException, SocketTimeoutException {
        String response = getUDPMessage();
        words.add(response);
        System.out.println("Response: " + response);
    }

    private void sendMessage() throws IOException, SocketException {
        String message;
        long startTime = new Date().getTime();
        while (true) {
            System.out.println("Enter your message");
            message = scanner.nextLine();
            long finishTime = new Date().getTime();
            if (finishTime - startTime >= 10000) {
                throw new SocketException();
            }
            if (isWordValid(message)) {
                this.words.add(message);
                break;
            } else {
                System.out.println("Invalid message, doesn't apply the rules");
            }
        }
        sendUDPMessage(message);
    }

    private boolean isWordValid(String message) {
        if (message.length() < 2) {
            return false;
        }
        String previousMessage = this.words.get(this.words.size() - 1);
        String lastTwoLetters = previousMessage.substring(previousMessage.length() - 2);
        return !words.contains(message) && lastTwoLetters.equals(message.substring(0, 2));
    }

}