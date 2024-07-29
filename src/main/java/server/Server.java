package server;

import command.Request;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Server implements Closeable {


  private boolean listening;
  public Thread serverThread;

  public Server() {
    this.listening = false;
  }

  public void listen(int port) {
    this.listening = true;
    this.serverThread = Thread.currentThread();
    Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    log.debug("Server listening on port {}", port);
    try (
      var selector = Selector.open();
      var serverCh = ServerSocketChannel.open()
    ) {
      serverCh.configureBlocking(false);
      serverCh.bind(new InetSocketAddress(port));
      serverCh.register(selector, SelectionKey.OP_ACCEPT);

      while (this.listening) {
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
              var buffer = ByteBuffer.allocate(1024);
              var bytesRead = clientCh.read(buffer);
              if (bytesRead == -1) {
                var clientAddr = clientCh.getRemoteAddress();
                clientCh.close();
                log.debug("Client disconnected! {}", clientAddr);
              } else {
                buffer.flip();
                var requestLine = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                buffer.clear();
                log.debug("Request line: {}", requestLine);
                var request = Request.builder().reqLine(requestLine).build();

                var response = request.executeCommands();
                if (response.length != 0) {
                  buffer.put(response);
                }
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

  public void close() {
    try {
      if (this.listening) {
        log.debug("Closing server");
        this.listening = false;
        this.serverThread.join();
        log.debug("Server closed");
      } else {
        log.debug("Server already closed");
      }
    } catch (InterruptedException e) {
      log.error("Error closing server: {}", e.getMessage(), e);
    }
  }

}
