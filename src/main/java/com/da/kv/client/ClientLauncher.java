package com.da.kv.client;

import org.apache.commons.cli.*;

public class ClientLauncher {

    private static final KVClient kvClient = new KVClient();

    public static void main(String[] args) {
        Options options = setOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("splendor-raft [OPTION]", options);
            return;
        }

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmdLine = parser.parse(options, args);
            String addresses = cmdLine.getOptionValue("gc");
            kvClient.start(addresses.split(" "));
        } catch (ParseException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    private static Options setOptions() {
        Options options = new Options();
        options.addOption(Option.builder("gc")
                .hasArgs()
                .argName("node-endpoint")
                .required()
                .desc("group config, required when starts with group-member mode. format: <node-endpoint> <node-endpoint>..., " +
                        "format of node-endpoint: <node-id>,<host>,<port-raft-node>, eg: A,localhost,8000 B,localhost,8010")
                .build());
        return options;
    }
}
