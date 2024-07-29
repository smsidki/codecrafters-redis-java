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
      ServerInfo.getInstance().setRole("master");
    } else {
      ServerInfo.getInstance().setRole("slave");
    }

    try(var server = new Server()) {
      server.listen(port);
    }
  }

}
