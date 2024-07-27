import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Main {

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    var port = 6379;
    var running = new boolean[]{true};

    try (
      var selector = Selector.open();
      var serverCh = ServerSocketChannel.open()
    ) {
      serverCh.configureBlocking(false);
      serverCh.bind(new InetSocketAddress(port));
      serverCh.register(selector, SelectionKey.OP_ACCEPT);

      var mainThread = Thread.currentThread();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Shutting down");
        running[0] = false;
        try {
          mainThread.join();
          System.out.println("Shut down complete");
        } catch (InterruptedException e) {
          System.err.println("Main thread interrupted! " + e.getMessage());
        }
      }));

      while (running[0]) {
        if (selector.select(5000) == 0) {
          continue;
        }

        var keys = selector.selectedKeys();
        var iterator = keys.iterator();
        while (iterator.hasNext()) {
          var key = iterator.next();
          if (key.isAcceptable()) {
            var clientCh = serverCh.accept();
            clientCh.configureBlocking(false);
            clientCh.register(selector, SelectionKey.OP_READ);
            System.out.println("Client connected! " + clientCh.getRemoteAddress());
          } else if (key.isReadable()) {
            var clientCh = (SocketChannel) key.channel();
            var buffer = ByteBuffer.allocate(30);
            var bytesRead = clientCh.read(buffer);
            if (bytesRead == -1) {
              var clientAddr = clientCh.getRemoteAddress();
              clientCh.close();
              System.out.println("Client disconnected! " + clientAddr);
            } else {
              buffer.flip();
              var cmd = StandardCharsets.UTF_8.decode(buffer).toString().trim();
              buffer.clear();
              System.out.println("Command: " + cmd);

              buffer.put("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
              buffer.flip();
              clientCh.write(buffer);
            }
          }
        }
        iterator.remove();
      }
    } catch (IOException e) {
      System.err.println("Failed to open selector: " + e.getMessage());
    }
  }

}
