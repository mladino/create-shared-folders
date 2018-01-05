package ar.com.marcos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Created by Marcos.Ladino on 10/16/2017.
 */
public class Main {

    public static void main(String... args){
        String source = args[0];
        String threads = args[1];
        String login ="net use \\\\%s T3sting.Fun /user:Administrator";
        String xcopy = "xcopy /e /i /y %s \\\\%s\\c$\\installer";
        String stop = "sc \\\\%1$s stop \"AproposManagementServer\" \n" +
                        "sc \\\\%1$s stop \"AproposApache\" \n"+
                        "sc \\\\%1$s stop \"EnvoxAlarmCenter\" \n"+
                        "sc \\\\%1$s stop \"CtcServer\" \n"+
                        "sc \\\\%1$s stop \"EnvoxServer\" \n"+
                        "sc \\\\%1$s stop \"MediaGateway\" \n";


        ExecutorService executor = Executors.newFixedThreadPool(Integer.valueOf(threads));
        try(Stream<String> lines = Files.lines(Paths.get("servers.txt"))){
            lines.forEach( hostDest -> {
                    Runnable worker = new Thread(hostDest+"-worker") {
                        @Override
                        public void run() {
                            try {
                                System.out.println(this.getName()+": Copying from "+source+" to "+hostDest);
                                Process p = null;
                                String command = String.format(login,hostDest);
                                System.out.println(this.getName()+":Command: "+command);
                                p = Runtime.getRuntime().exec(command);
                                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                String inLine = null;
                                while ((inLine = stdInput.readLine()) != null) {
                                    System.out.println(this.getName()+": "+inLine);
                                }
                                String errorLine =null;

                                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                while ((errorLine = stdError.readLine()) != null) {
                                    System.out.println(this.getName()+": "+errorLine);
                                }


                                command = String.format(xcopy,source,hostDest);
                                System.out.println(this.getName()+":Command: "+command);
                               // p = Runtime.getRuntime().exec(command );
                               stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

 /*                              while ((inLine = stdInput.readLine()) != null) {
                                    System.out.println(inLine);
                                }*/
                                stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                while ((errorLine = stdError.readLine()) != null) {
                                    System.out.println(this.getName()+": "+errorLine);
                                }

                                command = String.format(stop,hostDest);
                                System.out.println(this.getName()+":Command: "+command);
                                p = Runtime.getRuntime().exec(command );

                                stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                                while ((errorLine = stdError.readLine()) != null) {
                                    System.out.println(this.getName()+": "+errorLine);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    executor.execute(worker);
            });

        }catch(Exception e){
            e.printStackTrace();
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");


    }

}
