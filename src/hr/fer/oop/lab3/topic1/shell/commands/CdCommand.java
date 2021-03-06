package hr.fer.oop.lab3.topic1.shell.commands;

import hr.fer.oop.lab3.topic1.shell.CommandStatus;
import hr.fer.oop.lab3.topic1.shell.Environment;
import hr.fer.oop.lab3.topic1.shell.Terminal;
import hr.fer.oop.lab3.topic1.shell.commands.AbstractCommand;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Luka on 06/12/14.
 */
public class CdCommand extends AbstractCommand {

    public CdCommand() {
        super("cd", "Change directory");
    }

    @Override
    public CommandStatus execute(Environment environment, String inputArguments) {
        Path inputPath = Paths.get(inputArguments);
        Terminal terminal = environment.getActiveTerminal();
        Path oldPath = terminal.getCurrentPath();
        Path relativePath = new File(oldPath.toString(), inputArguments).toPath();
        Path newPath = oldPath;

        if (inputArguments.equals("..")) {
            if (oldPath.getParent() != null)
                newPath = oldPath.getParent();
        }

        else if (Files.exists(relativePath))
            newPath = relativePath;

        else if (Files.exists(inputPath))
            newPath = inputPath;

        terminal.setCurrentPath(newPath);

        return CommandStatus.CONTINUE;
    }
}
