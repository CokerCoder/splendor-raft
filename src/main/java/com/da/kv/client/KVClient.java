package com.da.kv.client;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The class implementing interactive console
 */
public class KVClient {
    private final ServerRouter serverRouter;
    private final LineReader reader;

    public KVClient() {
        reader = buildReader();
        serverRouter = new ServerRouter();
    }

    /**
     * Start the client and enter the console
     * @param nodes A list of endpoints with format <node-id>,<host>,<port-raft-node>
     */
    public void start(String[] nodes) {
        buildServerRouter(nodes);
        int status = 0;
        System.out.println("Start the splendor-raft console!");
        String prompt = "Splendor-raft>";

        while (status == 0) {
            String line;
            try {
                line = reader.readLine(prompt);
                status = processCommands(line);
            } catch (UserInterruptException ignored) { }
        }
        serverRouter.close();
    }

    private void buildServerRouter(String[] nodes) {
        for (String node : nodes) {
            String[] info = node.split(",");
            try {
                Address address = new Address(info[1], Integer.parseInt(info[2]));
                serverRouter.add(new NodeId(info[0]), new RPCChannel(address.toString()));
            } catch (NumberFormatException e) {
                System.out.printf("Invalid port: %s\n", node);
            }
        }
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
            System.err.println("Fail to start raft console");
            System.exit(1);
        }
        return null;
    }

    /**
     * Raft commands processor
     * @param line The input commands
     * @return The client status. 1 means exits the console.
     */
    private int processCommands(String line) {
        String[] args = line.strip().split(" ");
        String command = args[0];
        if (command.isBlank()) {
            return 0;
        }
        else if (command.equals(ClientCommand.GET.toString())) {
            get(args);
        }
        else if (command.equals(ClientCommand.SET.toString())) {
            set(args);
        }
        else if (command.equals(ClientCommand.HELP.toString())) {
            showHelper(ClientCommand.values());
        }
        else if (command.equals(ClientCommand.EXIT.toString())) {
            return 1;
        }
        else {
            System.out.println("Can not find command: " + command);
        }
        return 0;
    }

    private void get(String[] args) {
        String key = "";
        for (int i = 1; i < args.length; i++) {
            if (!args[i].isEmpty()) {
                key = args[i];
                break;
            }
        }
        if (key.isEmpty()) {
            showHelper(ClientCommand.GET);
        }
        else {
            byte[] result = serverRouter.get(key);
            System.out.println(Arrays.toString(result));
        }
    }

    private void set(String[] args) {
        String key = "", value = "";
        for (int i = 1; i < args.length; i++) {
            if (!args[i].isEmpty()) {
                if (key.isEmpty()) {
                    key = args[i];
                }
                else {
                    value = args[i];
                    break;
                }
            }
        }
        if (key.isEmpty()) {
            showHelper(ClientCommand.SET);
        }
        else {
            serverRouter.set(key, value.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void showHelper(ClientCommand ...commands) {
        System.out.println("Usage:");
        for (ClientCommand command : commands) {
            System.out.printf("\t%s:\t%s\n", command.text, command.helper);
        }
    }

    /**
     * List of available commands for client
     */
    private enum ClientCommand {
        GET("get", "get <KEY>"),
        SET("set", "set <KEY> <VALUE>"),
        HELP("help", "Show the help messages"),
        EXIT("exit", "Exit the splendor-raft console");

        private final String text;
        private final String helper;

        ClientCommand(String command, String helper) {
            text = command;
            this.helper = helper;
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
