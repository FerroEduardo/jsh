package com.ferroeduardo.jsh;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import static com.ferroeduardo.jsh.Commands.*;

public class Shell {

    private String username;
    private String hostname;
    private File cwd;

    private String currentCommand;
    private String[] currentArguments;

    private Map<String, Runnable> nativeCommands = Map.of(
            "logout", logout(),
            "cd", cd(this),
            "pwd", pwd(this),
            "ls", ls(this),
            "touch", touch(this),
            "mkdir", mkdir(this),
            "rmdir", rmdir(this),
            "rm", rm(this),
            "cat", cat(this),
            "exec", exec(this)
    );


    private Shell() {
        initializeFields();
        receiveShellInput();
    }

    private void receiveShellInput() {
        try (final Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.printf(
                        "%s@%s:%s\njsh $ ",
                        username, hostname, cwd.getAbsolutePath().equals(System.getProperty("user.home")) ? "~" : cwd.getAbsolutePath());
                String line = scanner.nextLine();
                String[] split = line.split(" ");

                if (line.isBlank() || split.length == 0) {
                    continue;
                }
                currentCommand = split[0];
                boolean commandExists = nativeCommands.containsKey(currentCommand);
                currentArguments = split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];

                if (commandExists) {
                    if (currentCommand.equals("logout")) {
                        break;
                    }
                    Runnable commandRunnable = nativeCommands.get(currentCommand);
                    commandRunnable.run();
                } else {
                    System.out.println("Command not found");
                }
            }
        }
    }

    private void initializeFields() {
        try {
            this.username = System.getProperty("user.name");
            this.hostname = getHostname().trim();
            this.cwd = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to get current working directory", e);
        }
    }

    public static void start() {
        if (!System.getProperty("os.name").equals("Linux")) {
            System.out.println("Only Linux is supported currently");
            System.exit(-1);
        }
        Locale.setDefault(Locale.US);
        new Shell();
    }

    public static String getHostname() {
        try (Scanner s = new Scanner(Runtime.getRuntime().exec("hostname").getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getCwd() {
        return cwd;
    }

    public void setCwd(File cwd) {
        this.cwd = cwd;
    }

    public String getCurrentCommand() {
        return currentCommand;
    }

    public String[] getCurrentArguments() {
        return currentArguments;
    }
}
