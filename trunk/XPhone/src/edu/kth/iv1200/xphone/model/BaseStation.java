/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kth.iv1200.xphone.model;

import java.util.ArrayList;

/**
 *
 * @author cuneyt
 */
public class BaseStation {

    private int id;
    private ArrayList<Customer> channels;
    private int freeChannels;
    private ArrayList<Customer> handoverChannels;
    private int freeHandoverChannels;
    private double position;
    private double coverageStart;
    private double coverageEnd;
    private double coverage;
    private int droppedCalls;
    private int blockedCalls;

    public BaseStation(int id, int channels, int handoverChannels, double position, double coverage) {
        this.id = id;
        this.channels = new ArrayList<Customer>();
        this.handoverChannels = new ArrayList<Customer>();
        this.position = position * 1000;
        this.coverage = coverage * 1000;
        this.coverageStart = position - coverage / 2;
        this.coverageEnd = position + coverage / 2;
        this.freeChannels = channels;
        this.freeHandoverChannels = handoverChannels;
        this.droppedCalls = 0;
        this.blockedCalls = 0;
    }

    public XEvent initiateCall(Customer c) {
        XEvent result = null;
        if (freeChannels > 0) {
            result = manipulateCall(c);
            channels.add(c);
            freeChannels--;
        } else {
            result = new BlockCall(c);
        }
        return result;
    }

    public XEvent passHandover(Customer c, BaseStation b) {
        XEvent result = null;
        if (channels.contains(c)) {
            channels.remove(c);
            freeChannels++;
            result = b.receiveHandover(c);
        } else if (handoverChannels.contains(c)) {
            handoverChannels.remove(c);
            freeHandoverChannels++;
            result = b.receiveHandover(c);
        }
        return result;
    }

    private XEvent receiveHandover(Customer c) {
        XEvent result = null;
        if (freeChannels > 0) {
            result = manipulateCall(c);
            channels.add(c);
            freeChannels--;
        } else if (freeHandoverChannels > 0) {
            result = manipulateCall(c);
            handoverChannels.add(c);
            freeHandoverChannels--;
        } else {
            result = new DropCall(c);
        }
        return result;

    }

    public void endCall(Customer c) {
        if (channels.contains(c)) {
            channels.remove(c);
            freeChannels++;
        } else if (handoverChannels.contains(c)) {
            handoverChannels.remove(c);
            freeHandoverChannels++;
        }
    }

    private XEvent manipulateCall(Customer c) {
        XEvent result = null;
        double speed = c.getSpeed();
        double callPosition = c.getPosition();
        double duration = c.getDuration();
        double arrivalTime = c.getTime();
        double callDistance = speed * duration;

        if (callPosition + callDistance > coverageEnd) {
            duration -= (coverageEnd - callPosition) / speed;
            arrivalTime += (coverageEnd - callPosition) / speed;
            c.setDuration(duration);
            c.setTime(arrivalTime);
            c.setPosition(coverageEnd);
            result = new Handover(c, this);
        } else {
            arrivalTime += duration;
            c.setTime(arrivalTime);
            result = new EndCall(c);
        }
        return result;
    }

    public void dropCall() {
        droppedCalls++;
    }

    public void blockCall() {
        blockedCalls++;
    }

    public int getBlockedCalls() {
        return blockedCalls;
    }

    public void setBlockedCalls(int blockedCalls) {
        this.blockedCalls = blockedCalls;
    }

    public ArrayList<Customer> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<Customer> channels) {
        this.channels = channels;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    public int getDroppedCalls() {
        return droppedCalls;
    }

    public void setDroppedCalls(int droppedCalls) {
        this.droppedCalls = droppedCalls;
    }

    public int getFreeChannels() {
        return freeChannels;
    }

    public void setFreeChannels(int freeChannels) {
        this.freeChannels = freeChannels;
    }

    public int getFreeHandoverChannels() {
        return freeHandoverChannels;
    }

    public void setFreeHandoverChannels(int freeHandoverChannels) {
        this.freeHandoverChannels = freeHandoverChannels;
    }

    public ArrayList<Customer> getHandoverChannels() {
        return handoverChannels;
    }

    public void setHandoverChannels(ArrayList<Customer> handoverChannels) {
        this.handoverChannels = handoverChannels;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public double getCoverageEnd() {
        return coverageEnd;
    }

    public void setCoverageEnd(double coverageEnd) {
        this.coverageEnd = coverageEnd;
    }

    public double getCoverageStart() {
        return coverageStart;
    }

    public void setCoverageStart(double coverageStart) {
        this.coverageStart = coverageStart;
    }
}