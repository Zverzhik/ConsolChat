package zverzhik.consol.chat;

public class ServerApplication {
    public static void main(String[] args) {
        new Server(8089).start();
    }
}
