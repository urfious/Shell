package hr.fer.oop.lab3.topic1.shell;

import hr.fer.oop.lab3.topic1.*;
import hr.fer.oop.lab3.topic1.shell.commands.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Luka on 05/12/14.
 */
public class MyShell {
    private static SimpleHashtable commands;
    private static Environment environment;

    static {
        commands = new SimpleHashtable();
        environment = new EnvironmentImpl();


        ShellCommand[] cc = {
                new HelpCommand(),
                new QuitCommand(),
                new CdCommand(),
                new TerminalCommand(),
                new TypeCommand(),
                new FilterCommand(),
                new ListCommand(),
                new CopyCommand(),
                new XCopyCommand()
        };

        for (ShellCommand c : cc)
            commands.put(c.getCommandName(), c);
    }

    public MyShell() {}

    public static void main(String[] args) throws IOException {
        environment.writeln("I'm your Terminal, your wish is my order. You may enter commands...");
        Terminal initialTerminal = environment.getOrCreateTerminal(1);
        environment.setActiveTerminal(initialTerminal);
        CommandStatus status = CommandStatus.CONTINUE;
        do {
            Terminal activeTerminal = environment.getActiveTerminal();
            environment.write(activeTerminal.getPrompt());
            String inputLine = environment.readLine();

            String command = getCommandFromInputLine(inputLine);

            String arguments = getInputArgumentsFromInputLine(inputLine);

            ShellCommand shellCommand;
            try {
                shellCommand = (ShellCommand) commands.get(command);
                status = shellCommand.execute(environment, arguments);
            } catch (CommandException e) {
                environment.writeln(e.getMessage());
                continue;
            } catch (IndexOutOfBoundsException e) {
                environment.writeln(e.getMessage());
                continue;
            }catch (NullPointerException e) {
                environment.writeln(e.getMessage());
                continue;
            } catch (IllegalArgumentException e){
                environment.writeln(inputLine + " is not recognized as valid command");
                continue;
            }

        } while (status == CommandStatus.CONTINUE);

        environment.writeln("Thank you for using this shell. Goodbye!");

    }

    private static String getCommandFromInputLine(String inputLine) {
        String[] inputList = inputLine.split(" ");
        String command = inputList[0];
        return command;
    }

    private static String getInputArgumentsFromInputLine(String inputLine) {
        String [] inputList = inputLine.split(" ");

        if (inputList.length < 2) return "";

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 1; i < inputList.length; i++) {
            stringBuilder.append(inputList[i]);
            if (i < inputList.length - 1) stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    public static class EnvironmentImpl implements Environment{
        SimpleHashtable terminals = new SimpleHashtable();
        Terminal activeTerminal;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer;


        @Override
        public String readLine() {
            String line = "";
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return line;
        }

        @Override
        public void write(String text) {
            System.out.print(text);
        }

        @Override
        public void writeln(String text) {
            System.out.println(text);
        }

        @Override
        public Terminal getActiveTerminal() {
            return activeTerminal;
        }

        @Override
        public void setActiveTerminal(Terminal terminal) {
            activeTerminal = terminal;
        }

        @Override
        public Terminal getOrCreateTerminal(int terminalNumber) {
            if (terminals.containsKey(terminalNumber))
                return (Terminal) terminals.get(terminalNumber);

            Terminal newTerminal = new Terminal(terminalNumber);
            terminals.put(terminalNumber, newTerminal);

            return newTerminal;
        }

        @Override
        public Terminal[] listTerminals() {
            int numOfTerminals = terminals.size();
            Terminal[] existingTerminals = new Terminal[numOfTerminals];
            int index = 0;

            for (SimpleHashtable.TableEntry terminalEntry : terminals){
                Terminal terminal = (Terminal) terminalEntry.getValue();
                existingTerminals[index++] = terminal;
            }
            return existingTerminals;
        }

        @Override
        public Iterable<SimpleHashtable.TableEntry> commands() {
            return commands;
        }
    }
}


