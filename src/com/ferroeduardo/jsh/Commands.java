package com.ferroeduardo.jsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Commands {

    protected static Runnable logout() {
        return () -> {
            System.out.println("logout");
        };
    }

    protected static Runnable cd(Shell shell) {
        return () -> {
            String pathname = shell.getCurrentArguments().length == 0 ? "" : shell.getCurrentArguments()[0];
            if (pathname.isBlank()) {
                shell.setCwd(new File(System.getProperty("user.home")));
            } else {
                if (pathname.startsWith("~")) {
                    pathname = System.getProperty("user.home") + "/" + pathname.substring(1);
                    File proposedPath = new File(pathname);
                    if (proposedPath.isDirectory()) {
                        shell.setCwd(new File(pathname));
                    } else {
                        System.out.println("Path is not a directory");
                    }
                } else {
                    shell.setCwd(new File(pathname));
                }
            }
        };
    }

    protected static Runnable pwd(Shell shell) {
        return () -> {
            System.out.println(shell.getCwd().getAbsolutePath());
        };
    }

    protected static Runnable ls(Shell shell) {
        return () -> {
            File[] directoryFiles = shell.getCwd().listFiles();
            boolean onlyFiles = shell.getCurrentArguments()[0].equals("-f");
            if (directoryFiles.length > 0) {
                System.out.println("name -> isDirectory isExecutable isReadable isWritable isHidden totalSpace(MB)");
            }
            for (File file : directoryFiles) {
                if (!file.isFile() && onlyFiles) {
                    continue;
                }
                String name = file.getName();
                boolean isDirectory = file.isDirectory();
                boolean isExecutable = file.canExecute();
                boolean isReadable = file.canRead();
                boolean isWritable = file.canWrite();
                boolean isHidden = file.isHidden();
                long totalSpace = file.length();
                System.out.printf("%s -> %b %b %b %b %b %f%n", name, isDirectory, isExecutable, isReadable, isWritable, isHidden, (totalSpace / 1048576f));
            }
        };
    }

    protected static Runnable touch(Shell shell) {
        return () -> {
            try {
                String path = shell.getCurrentArguments()[0];
                Path filePath = Files.createFile(Path.of(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    protected static Runnable mkdir(Shell shell) {
        return () -> {
            try {
                String path = shell.getCurrentArguments()[0];
                Path folderPath = Files.createDirectories(Path.of(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    protected static Runnable rmdir(Shell shell) {
        return () -> {
            try {
                String path = shell.getCurrentArguments()[0];
                Files.delete(Path.of(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    protected static Runnable rm(Shell shell) {
        return () -> {
            try {
                String path = shell.getCurrentArguments()[0];
                Files.delete(Path.of(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    protected static Runnable cat(Shell shell) {
        return () -> {
            String firstArgument = shell.getCurrentArguments()[0];
            // absolute path
            if (firstArgument.equals("-a")) {
                String secondArgument = shell.getCurrentArguments()[1];
                secondArgument = secondArgument.startsWith("\"") && secondArgument.endsWith("\"") ? secondArgument.substring(1, secondArgument.length() - 1) : secondArgument;
                System.out.println(secondArgument);
                try {
                    File file = new File(secondArgument);
                    if (file.exists() && file.isFile()) {
                        BufferedReader bufferedReader = Files.newBufferedReader(file.toPath());
                        bufferedReader.lines().forEach(System.out::println);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    File cwd = shell.getCwd();
                    File file = new File(cwd.getAbsolutePath() + "/" + firstArgument);
                    if (file.exists() && file.isFile()) {
                        BufferedReader bufferedReader = Files.newBufferedReader(file.toPath());
                        bufferedReader.lines().forEach(System.out::println);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    protected static Runnable exec(Shell shell) {
        return () -> {
            String[] currentArguments = shell.getCurrentArguments();
            String command = String.join("", currentArguments);
            runCommand(shell.getCwd(), command);
        };
    }

    private static void runCommand(File cwd, String command) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.inheritIO();
            builder.command("sh", "-c", command);
            builder.directory(cwd);

            Process process = builder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
