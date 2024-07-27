import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Main {

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    log.info("Logs from your program will appear here!");

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
        log.debug("Shutting down");
        running[0] = false;
        try {
          mainThread.join();
          log.debug("Shut down complete");
        } catch (InterruptedException e) {
          log.error("Main thread interrupted!", e);
        }
      }));

      while (running[0]) {
        if (selector.select(5_000) == 0) {
          continue;
        }

        var keys = selector.selectedKeys();
        var iterator = keys.iterator();
        while (iterator.hasNext()) {
          var key = iterator.next();
          if (key.isAcceptable()) {
            var clientCh = serverCh.accept();
            if (clientCh != null) {
              clientCh.configureBlocking(false);
              clientCh.register(selector, SelectionKey.OP_READ);
              log.debug("Client connected! {}", clientCh.getRemoteAddress());
            }
          } else if (key.isReadable()) {
            try {
              var clientCh = (SocketChannel) key.channel();
              var buffer = ByteBuffer.allocate(30);
              var bytesRead = clientCh.read(buffer);
              if (bytesRead == -1) {
                var clientAddr = clientCh.getRemoteAddress();
                clientCh.close();
                log.debug("Client disconnected! {}", clientAddr);
              } else {
                buffer.flip();
                var cmd = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                buffer.clear();
                log.debug("Command: {}", cmd);

                buffer.put("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                while (buffer.hasRemaining()) {
                  clientCh.write(buffer);
                }
                buffer.clear();
              }
            } catch (Exception e) {
              log.error("Fail processing request", e);
              //noinspection resource
              if (key.channel() != null) {
                try {
                  key.channel().close();
                } catch (Exception ex) {
                  log.error("Fail closing channel", ex);
                }
              }
            }
          }
        }
        iterator.remove();
      }
    } catch (IOException e) {
      log.error("Failed to open selector", e);
    }
  }

}
