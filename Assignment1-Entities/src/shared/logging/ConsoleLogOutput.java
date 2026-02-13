package shared.logging;

public class ConsoleLogOutput implements LogOutput{
    @Override
    public void log(String level, String message) {

        synchronized (System.out){
            System.out.println(level + " " + message);
        }
    }
}
