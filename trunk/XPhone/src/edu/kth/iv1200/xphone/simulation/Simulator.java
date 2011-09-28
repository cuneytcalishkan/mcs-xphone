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
    private double previousArrival;
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
        previousArrival = 0;
        baseStations = new ArrayList<BaseStation>();
        coverage = highwayLength / stations;
        for (int i = 0; i < stations; i++) {
            double pos = ((i + 1) * coverage) - 1;
            baseStations.add(new BaseStation(i + 1, this.channels, this.reserved, pos, coverage));
        }
        this.fel = new TreeMap<Double, XEvent>();

    }

    private XEvent createCall() {
        InitiateCall result = new InitiateCall(new Customer(++totalCalls, previousArrival + rng.nextExp(iaMean),
                rng.nextUniform(highwayLength), rng.nextExp(mean),
                rng.nextNormal(m, stdev)));
        previousArrival = result.getTime();
        return result;
    }

    @Override
    public Statistics call() throws Exception {

        XEvent firstEvent = createCall();
        previousArrival = firstEvent.getTime();
        fel.put(firstEvent.getTime(), firstEvent);
        boolean stopSimulation = false;

        while (!fel.isEmpty()) {

            double key = fel.firstKey();
            XEvent e = fel.remove(key);
            clock = e.getTime();
            if (clock >= length) {
                stopSimulation = true;
            }

            if (e instanceof InitiateCall) {
                if (!stopSimulation) {
                    XEvent ne = createCall();
                    fel.put(ne.getTime(), ne);
                }

                boolean circulate = assignResponsible(e);
                if (circulate) {
                    e.setPosition(0);
                }
                //System.out.println(responsibleStation + " initiate call " + e.getCustomer());
                XEvent next = responsibleStation.initiateCall(e.getCustomer());
                fel.put(next.getTime(), next);
            } else if (e instanceof Handover) {
                boolean circulate = assignResponsible(e);
                Handover h = (Handover) e;
                responsibleStation = h.getFrom();

                if (circulate) {
                    h.setPosition(0);
                }

                //System.out.println(responsibleStation + " handover call " + e.getCustomer());

                XEvent next = responsibleStation.passHandover(e.getCustomer(), baseStations.get(responsibleStation.getId() % stations));
                if (next != null) {
                    fel.put(next.getTime(), next);
                }
            } else if (e instanceof EndCall) {
                assignResponsible(e);
                //System.out.println(responsibleStation + " end call " + e.getCustomer());
                responsibleStation.endCall(e.getCustomer());
            } else if (e instanceof DropCall) {
                assignResponsible(e);
                //System.out.println(responsibleStation + " drop call " + e.getCustomer());
                responsibleStation.dropCall();
            } else if (e instanceof BlockCall) {
                assignResponsible(e);
                //System.out.println(responsibleStation + " block call " + e.getCustomer());
                responsibleStation.blockCall();
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

    private boolean assignResponsible(XEvent e) {
        boolean circulate = true;
        for (BaseStation bs : baseStations) {
            if (e.getPosition() >= bs.getCoverageStart() && e.getPosition() < bs.getCoverageEnd()) {
                responsibleStation = bs;
                circulate = false;
                break;
            }
        }
        return circulate;
    }
}
