/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kth.iv1200.xphone.main;

import edu.kth.iv1200.xphone.model.RNG;
import edu.kth.iv1200.xphone.simulation.Simulator;
import edu.kth.iv1200.xphone.simulation.Statistics;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cuneyt
 */
public class XPhone {

    public static void main(String[] args) {
        try {
            if (args.length < 6 || args.length > 7) {
                System.out.println("USAGE: java XPhone [seed] [length] [replication] [warm-up] [channel] [reserved] ([debug])");
                System.out.println("seed: The seed for the simulation random number generators");
                System.out.println("length: The time length in seconds of each replication of the simulation");
                System.out.println("replication: The number of replications");
                System.out.println("warm-up: The warm-up period in seconds");
                System.out.println("channel: The number of channels in each base station");
                System.out.println("reserved: The number of reserved channels for handovers in each base station");
                System.out.println("debug (optional): If any value is entered, extra output will be provided");
                System.exit(-1);
            }

            int i = 0;
            long seed = Long.parseLong(args[i++]);
            double length = Double.parseDouble(args[i++]);
            int replication = Integer.parseInt(args[i++]);
            double warmup = Double.parseDouble(args[i++]);
            int channels = Integer.parseInt(args[i++]);
            int reserved = Integer.parseInt(args[i++]);
            boolean debug = false;
            if (args.length == 7) {
                debug = true;
            }
            Properties prop = new Properties();
            prop.load(new FileInputStream("data/xphone.properties"));

            double hwLength = Double.parseDouble(prop.getProperty("highway-length"));
            int stations = Integer.parseInt(prop.getProperty("base-stations"));
            double bcQos = Double.parseDouble(prop.getProperty("blocked-call-qos"));
            double dcQos = Double.parseDouble(prop.getProperty("dropped-call-qos"));
            double expMean = Double.parseDouble(prop.getProperty("exp-mean"));
            double normalM = Double.parseDouble(prop.getProperty("normal-m"));
            double normalStdev = Double.parseDouble(prop.getProperty("normal-stdev"));
            double expIaMean = Double.parseDouble(prop.getProperty("exp-ia-mean"));


            ArrayList<Future<Statistics>> statistics = new ArrayList<Future<Statistics>>();
            ExecutorService es = java.util.concurrent.Executors.newFixedThreadPool(replication);
            RNG rng = new RNG(seed);
            for (int j = 1; j <= replication; j++) {
                Callable<Statistics> sim = new Simulator(stations, hwLength, channels,
                        reserved, length, j, rng, warmup, normalM,
                        expMean, normalStdev, expIaMean);
                Future<Statistics> future = es.submit(sim);
                statistics.add(future);
            }

            DecimalFormat df = new DecimalFormat(".####");
            int accDc = 0;
            int accBc = 0;
            int accTc = 0;
            double accBcp = 0;
            double accDcp = 0;

            for (Future<Statistics> future : statistics) {
                try {
                    Statistics s = future.get();
                    accDc += s.getDroppedCalls();
                    accBc += s.getBlockedCalls();
                    accTc += s.getTotalCalls();
                    accBcp += s.getBlockedCallsPercentage();
                    accDcp += s.getDroppedCallsPercentage();
                    System.out.println("Replica #" + s.getReplicaId());
                    System.out.println("----------------------------------");
                    System.out.println("Total calls: " + s.getTotalCalls());
                    System.out.println("Dropped calls: " + s.getDroppedCalls());
                    System.out.println("Blocked calls: " + s.getBlockedCalls());
                    System.out.println("Dropped calls percentage: " + df.format(s.getDroppedCallsPercentage() * 100));
                    System.out.println("Blocked calls percentage: " + df.format(s.getBlockedCallsPercentage() * 100));
                    System.out.println("----------------------------------");
                } catch (InterruptedException ex) {
                    Logger.getLogger(XPhone.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(XPhone.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("Average total calls: " + df.format(accTc / replication));
            System.out.println("Average total dropped calls: " + df.format(accDc / replication));
            System.out.println("Average total blocked calls: " + df.format(accBc / replication));
            System.out.println("Average dropped calls percentage: " + df.format(accDcp / replication * 100));
            System.out.println("Average blocked calls percentage: " + df.format(accBcp / replication * 100));
        } catch (IOException ex) {
            Logger.getLogger(XPhone.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }
}
