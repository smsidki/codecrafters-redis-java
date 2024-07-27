import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Map;

public class Main {

  static final String CH_TYPE = "channelType";
  static final String CLIENT_CH = "clientChannel";
  static final String SERVER_CH = "serverChannel";

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    var port = 6379;
    var running = new boolean[]{true};

    var mainThread = Thread.currentThread();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      running[0] = false;
      try {
        mainThread.join();
      } catch (InterruptedException e) {
        System.err.println("Main thread interrupted!" + e.getMessage());
      }
    }));

    var selectorRef = (Selector) null;
    var chRef = (ServerSocketChannel) null;
    try {
      final var ch = chRef = ServerSocketChannel.open();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> close(ch)));

      ch.bind(new InetSocketAddress(port));
      ch.configureBlocking(false);

      final var selector = selectorRef = Selector.open();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> close(selector)));

      var serverSelectionKey = ch.register(selector, SelectionKey.OP_ACCEPT);
      serverSelectionKey.attach(Map.of(CH_TYPE, SERVER_CH));

      while (running[0]) {
        if (selector.select() == 0) {
          continue;
        }

        var keys = selector.selectedKeys();
        var iterator = keys.iterator();
        while (iterator.hasNext()) {
          var key = iterator.next();
          if (isServerCh(key)) {
            //noinspection resource
            var serverCh = (ServerSocketChannel) key.channel();
            var clientCh = (SocketChannel) serverCh.accept();
            if (clientCh != null) {
              clientCh.configureBlocking(false);
              var clientKey = clientCh.register(selector, SelectionKey.OP_READ, SelectionKey.OP_WRITE);
              clientKey.attach(Map.of(CH_TYPE, CLIENT_CH));

              var buffer = CharBuffer.wrap("+PONG\r\n");
              while (buffer.hasRemaining()) {
                clientCh.write(Charset.defaultCharset().encode(buffer));
              }
              buffer.clear();
            }
          } else {
            var clientCh = (SocketChannel) key.channel();
            var buffer = ByteBuffer.allocate(20);
            var bytesRead = 0;
            if (key.isReadable()) {
              if ((bytesRead = clientCh.read(buffer)) > 0) {
                buffer.flip();
                System.out.println("Buffer: " + Charset.defaultCharset().decode(buffer));
                buffer.clear();
              }
              if (bytesRead < 0) {
                clientCh.close();
              }
            }
          }
        }
        iterator.remove();
      }
    } catch (IOException ex) {
      System.err.println("Could not open server socket! " + ex.getMessage());
    } finally {
      close(selectorRef);
      close(chRef);
    }
  }

  static boolean isServerCh(SelectionKey key) {
    return SERVER_CH.equals(((Map<?, ?>) key.attachment()).get(CH_TYPE));
  }

  static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        System.err.println("Could not close server socket! " + e.getMessage());
      }
    }
  }

}
