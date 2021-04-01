import java.net.SocketException;
import java.net.UnknownHostException;

public class Player2 {

    public static void main(String[] args) throws SocketException, UnknownHostException {
        UDPPlayer player2 = new UDPPlayer(6788, 6789);

        player2.startGame(true);
    }

}
