package hr.fer.oop.lab3.topic1.shell;

import hr.fer.oop.lab3.topic1.*;
import hr.fer.oop.lab3.topic1.shell.Exceptions.CommandException;
import hr.fer.oop.lab3.topic1.shell.Exceptions.KeyNotFoundException;
import hr.fer.oop.lab3.topic1.shell.commands.*;

import java.io.*;
import java.util.Iterator;

/**
 * Created by Luka on 05/12/14.
 */
public class MyShell {
    private static SimpleHashtable<String, ShellCommand> commands;
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
                new XCopyCommand(),
                new DirCommand()
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
//		1. look up if command is our custom command
//		2. if not, try to run a system command:
//			- look up in /usr/bin and try to run from there
//		3. if nothing found, throw exception
                shellCommand = (ShellCommand) commands.get(command);
                status = shellCommand.execute(environment, arguments);
            } catch (KeyNotFoundException e) {
                if (SystemCommand.exists(command))
                    SystemCommand.execute(environment, inputLine);

                else
                    environment.writeln("command doesn't exist!");

                continue;


            } catch (CommandException e) {
                environment.writeln(e.getMessage());
                continue;
            } catch (IndexOutOfBoundsException e) {
                environment.writeln(e.getMessage());
                continue;
            } catch (NullPointerException e) {
                environment.writeln(e.getMessage());
                continue;
            } catch (IllegalArgumentException e) {
                environment.writeln(inputLine + " is not recognized as valid command");
                continue;
            } catch (Exception e) {
                environment.writeln(e.getMessage());
                continue;
            }

        } while (status == CommandStatus.CONTINUE);

        environment.writeln("Thank you for using this shell. Goodbye!");

    }

    public static String getCommandFromInputLine(String inputLine) {
        String[] inputList = inputLine.split(" ");
        String command = inputList[0];
        return command;
    }

    public static String getInputArgumentsFromInputLine(String inputLine) {
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
        SimpleHashtable<Integer, Terminal> terminals = new SimpleHashtable();
        Terminal activeTerminal;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));


        @Override
        public String readLine() {
            String line = null;
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
            try {
                writer.write(text);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void writeln(String text) {
            write(text + "\n");
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

            for (Object terminalObject : terminals){
                SimpleHashtable.TableEntry terminalEntry = (SimpleHashtable.TableEntry) terminalObject;
                Terminal terminal = (Terminal) terminalEntry.getValue();
                existingTerminals[index++] = terminal;
            }
            return existingTerminals;
        }

        @Override
        public Iterable<ShellCommand> commands() {
            return new Iterable<ShellCommand>() {
                @Override
                public Iterator<ShellCommand> iterator() {
                    return new Iterator<ShellCommand>() {
                        Iterator<SimpleHashtable.TableEntry> commandsTableEntryIterator = commands.iterator();
                        @Override
                        public boolean hasNext() {
                            return commandsTableEntryIterator.hasNext();
                        }

                        @Override
                        public ShellCommand next() {
                            return (ShellCommand) commandsTableEntryIterator.next().getValue();
                        }
                    };
                }
            };
        }
    }
}


