/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kth.iv1200.xphone.simulation;

import edu.kth.iv1200.xphone.model.BaseStation;
import edu.kth.iv1200.xphone.model.BlockCall;
import edu.kth.iv1200.xphone.model.Customer;
import edu.kth.iv1200.xphone.model.DropCall;
import edu.kth.iv1200.xphone.model.EndCall;
import edu.kth.iv1200.xphone.model.Handover;
import edu.kth.iv1200.xphone.model.InitiateCall;
import edu.kth.iv1200.xphone.model.RNG;
import edu.kth.iv1200.xphone.model.XEvent;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 *
 * @author cuneyt
 */
public class Simulator implements Callable<Statistics> {

    private int stations;
    private double highwayLength;
    private int channels;
    private int reserved;
    private double mean;
    private double iaMean;
    private double m;
    private double stdev;
    private double length;
    private int id;
    private RNG rng;
    private double warmup;
    private double coverage;
    private double clock;
    private int totalCalls;
    private ArrayList<BaseStation> baseStations;
    private TreeMap<Double, XEvent> fel;
    private BaseStation responsibleStation = null;

    public Simulator(int stations, double highwayLength, int channels,
            int reserved, double length, int id, RNG rng,
            double warmup, double m, double mean, double stdev, double iaMean) {
        this.stations = stations;
        this.highwayLength = highwayLength;
        this.channels = channels - reserved;
        this.reserved = reserved;
        this.length = length;
        this.id = id;
        this.rng = rng;
        this.warmup = warmup;
        this.m = m;
        this.mean = mean;
        this.stdev = stdev;
        this.iaMean = iaMean;
        clock = 0;
        totalCalls = 0;
        baseStations = new ArrayList<BaseStation>();
        coverage = highwayLength / stations;
        for (int i = 0; i < stations; i++) {
            double pos = ((i + 1) * coverage) - 1;
            baseStations.add(new BaseStation(i + 1, this.channels, this.reserved, pos, coverage));
        }
        this.fel = new TreeMap<Double, XEvent>();

    }

    private XEvent createCall() {
        InitiateCall result = new InitiateCall(new Customer(++totalCalls, clock + rng.nextExp(iaMean),
                rng.nextUniform(highwayLength), rng.nextExp(mean),
                rng.nextNormal(m, stdev)));
        return result;
    }

    @Override
    public Statistics call() throws Exception {
        int calls = 0;
        int droppedCalls = 0;
        int blockedCalls = 0;
        int handovers = 0;
        int endCalls = 0;
        int windowSize = 10;

        ArrayList<WarmupStatistics> ws = new ArrayList<WarmupStatistics>();
        FileWriter fw = new FileWriter("data/output" + this.id + ".txt", false);
        double accTime = 0;
        double accDc = 0;
        double accBc = 0;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumFractionDigits(4);
        nf.setMinimumIntegerDigits(4);

        XEvent firstEvent = createCall();
        fel.put(firstEvent.getTime(), firstEvent);
        boolean stopSimulation = false;

        while (!fel.isEmpty()) {

            double key = fel.firstKey();
            XEvent e = fel.remove(key);
            clock = e.getTime();

            if (ws.size() == windowSize) {
                ws.remove(0);
            }
            ws.add(new WarmupStatistics(clock, blockedCalls, droppedCalls));
            accTime = 0;
            accDc = 0;
            accBc = 0;
            for (WarmupStatistics s : ws) {
                accTime += s.getTime();
                accDc += s.getDroppedCalls();
                accBc += s.getBlockedCalls();
            }

            fw.write(nf.format((double) accTime / ws.size()) + "\t" + nf.format((double) accDc / ws.size()) + "\t" + nf.format((double) accBc / ws.size()) + "\n");

            if (clock >= length) {
                stopSimulation = true;
            }

            if (e instanceof InitiateCall) {
                calls++;
                if (!stopSimulation) {
                    XEvent ne = createCall();
                    fel.put(ne.getTime(), ne);
                }
                assignResponsible(e);
                XEvent next = responsibleStation.initiateCall(e.getCustomer());
                fel.put(next.getTime(), next);
            } else if (e instanceof Handover) {
                handovers++;
                Handover h = (Handover) e;
                responsibleStation = h.getFrom();
                h.setPosition(h.getPosition() % (highwayLength * 1000));
                XEvent next = responsibleStation.passHandover(e.getCustomer(), baseStations.get(responsibleStation.getId() % stations));
                fel.put(next.getTime(), next);
            } else if (e instanceof EndCall) {
                endCalls++;
                EndCall ec = (EndCall) e;
                ec.getAt().endCall(e.getCustomer());
            } else if (e instanceof DropCall) {
                droppedCalls++;
                DropCall dc = (DropCall) e;
                dc.getAt().dropCall();
            } else if (e instanceof BlockCall) {
                blockedCalls++;
                BlockCall bc = (BlockCall) e;
                bc.getAt().blockCall();
            }
        }
        fw.close();

        int dc = 0, bc = 0;
        for (BaseStation bs : baseStations) {
            dc += bs.getDroppedCalls();
            bc += bs.getBlockedCalls();
        }
        return new Statistics(id, droppedCalls, blockedCalls, calls, handovers, endCalls);
    }

    private void assignResponsible(XEvent e) {
        for (BaseStation bs : baseStations) {
            if (e.getPosition() >= bs.getCoverageStart() && e.getPosition() < bs.getCoverageEnd()) {
                responsibleStation = bs;
                break;
            }
        }
    }
}
