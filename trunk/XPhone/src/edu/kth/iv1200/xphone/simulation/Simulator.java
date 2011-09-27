/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kth.iv1200.xphone.simulation;

import edu.kth.iv1200.xphone.model.BaseStation;
import edu.kth.iv1200.xphone.model.Customer;
import edu.kth.iv1200.xphone.model.InitiateCall;
import edu.kth.iv1200.xphone.model.RNG;
import edu.kth.iv1200.xphone.model.XEvent;
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

    public Simulator(int stations, double highwayLength, int channels,
            int reserved, double length, int id, RNG rng,
            double warmup, double m, double mean, double stdev) {
        this.stations = stations;
        this.highwayLength = highwayLength;
        this.channels = channels;
        this.reserved = reserved;
        this.length = length;
        this.id = id;
        this.rng = rng;
        this.warmup = warmup;
        this.m = m;
        this.mean = mean;
        this.stdev = stdev;
        this.clock = 0;
        this.totalCalls = 0;
        this.baseStations = new ArrayList<BaseStation>();
        this.coverage = highwayLength / stations;
        for (int i = 0; i < stations; i++) {
            double pos = ((i + 1) * coverage) - 1;
            baseStations.add(new BaseStation(i + 1, this.channels, this.reserved, pos, coverage));
        }
        this.fel = new TreeMap<Double, XEvent>();

    }

    private XEvent createCall() {
        return new InitiateCall(new Customer(++totalCalls, clock + rng.nextUniform(length),
                rng.nextUniform(highwayLength), rng.nextExp(mean),
                rng.nextNormal(m, stdev)));
    }

    @Override
    public Statistics call() throws Exception {

        XEvent firstEvent = createCall();
        fel.put(firstEvent.getTime(), firstEvent);


        while (!fel.isEmpty()) {

            double key = fel.firstKey();
            XEvent e = fel.remove(key);
            clock = e.getTime();


            if (e instanceof InitiateCall) {
                InitiateCall ne = (InitiateCall) createCall();
                fel.put(ne.getTime(), ne);

                InitiateCall ic = (InitiateCall) e;

                BaseStation responsibleStation = null;
                boolean circulate = true;
                for (BaseStation bs : baseStations) {
                    if (ic.getPosition() >= bs.getCoverageStart() && ic.getPosition() < bs.getCoverageEnd()) {
                        responsibleStation = bs;
                        circulate = false;
                        break;
                    }
                }
                if (circulate) {
                    responsibleStation = baseStations.get(0);
                }

                XEvent next = responsibleStation.initiateCall(e.getCustomer());
                fel.put(next.getTime(), next);
            }
        }



        {// end of simulation collect statistics
            int dc = 0, bc = 0;
            for (BaseStation bs : baseStations) {
                dc += bs.getDroppedCalls();
                bc += bs.getBlockedCalls();
            }
            return new Statistics(id, dc, bc, totalCalls);
        }
    }
}
