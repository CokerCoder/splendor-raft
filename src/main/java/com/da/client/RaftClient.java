package com.da.client;

import com.da.node.NodeId;
import com.da.node.nodestatic.Address;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The class implementing interactive console
 */
public class RaftClient {

    private final CommandContext context;
    private final LineReader reader;
    public RaftClient(Map<NodeId, Address> serverMap) {
        context = new CommandContext(serverMap);
        reader = buildReader();
    }

    public void start() {
        int status = 0;
        String prompt = "Splendor-Raft>";

        while (status == 0) {
            String line;
            try {
                line = reader.readLine(prompt);
                status = processCommands(line);
            } catch (UserInterruptException ignored) { }
        }
    }

    private int processCommands(String line) {
        String[] args = line.strip().split(" ");
        String command = args[0];
        if (command.equals(ClientCommand.KV_GET.toString())) {
            context.get(args);
        }
        else if (command.equals(ClientCommand.KV_SET.toString())) {
            context.set(args);
        }
        else if (command.equals(ClientCommand.EXIT.toString())) {
            return 1;
        }
        else {
            System.out.println("Can not find command: " + command);
        }
        return 0;
    }

    private LineReader buildReader() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            Completer commandCompleter = new ArgumentCompleter(
                new StringsCompleter(ClientCommand.all()),
                NullCompleter.INSTANCE
            );
            return LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(commandCompleter)
                    .build();
        } catch (IOException e) {
            System.out.println("Fail to start raft console");
            System.exit(1);
        }
        return null;
    }

    public static void main(String[] args) {
        // TODO check start args
    }

    /**
     * List of available commands for client
     */
    private enum ClientCommand {
        KV_GET("kvstore-get"),
        KV_SET("kvstore-set"),
        EXIT("exit");

        private final String text;

        ClientCommand(String command) {
            text = command;
        }

        public static List<String> all() {
            List<String> values = new ArrayList<>(ClientCommand.values().length);
            for (ClientCommand command : ClientCommand.values()) {
                values.add(command.toString());
            }
            return values;
        }

        @Override
        public String toString() {
            return text;
        }

    }

}
