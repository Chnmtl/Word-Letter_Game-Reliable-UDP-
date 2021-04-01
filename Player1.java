import java.net.SocketException;
import java.net.UnknownHostException;

public class Player1 {

    public static void main(String[] args) throws SocketException, UnknownHostException {
        UDPPlayer player1 = new UDPPlayer(6789, 6788);

        player1.startGame(false);
    }
}
