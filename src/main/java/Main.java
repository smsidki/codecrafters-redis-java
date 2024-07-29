import lombok.extern.slf4j.Slf4j;
import memory.ServerInfo;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import server.Server;

@Slf4j
public class Main {

  public static void main(String[] args) throws ParseException {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    log.info("Logs from your program will appear here!");

    var options = new Options();
    var portOption = Option.builder("p").longOpt("port").hasArg().required(false).build();
    var replicaOption = Option.builder("r").longOpt("replicaof").hasArg().required(false).build();
    options.addOption(portOption);
    options.addOption(replicaOption);

    var cliParser = new DefaultParser();
    var cli = cliParser.parse(options, args);

    var port = cli.hasOption("p") ? Integer.parseInt(cli.getOptionValue("p")) : 6379;
    var replica = cli.getOptionValue("r");
    if (replica == null) {
      ServerInfo.role("master");
    } else {
      ServerInfo.role("slave");
    }

    ServerInfo.masterReplOffset(0);
    ServerInfo.masterReplID("8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb");

    try(var server = new Server()) {
      server.listen(port);
    }
  }

}
