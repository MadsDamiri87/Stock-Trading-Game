package shared.logging;

public class Logger {

    private static Logger instance;
    private LogOutput output;

    private Logger(){
        output = new ConsoleLogOutput();
    }

    public static synchronized Logger getInstance(){
        if (instance == null){
            instance = new Logger();
        }return instance;
    }

    public synchronized void log(String level, String message){
        output.log(level, message);
    }

    public synchronized void setOutput(LogOutput output){
        this.output = output;
    }

}
