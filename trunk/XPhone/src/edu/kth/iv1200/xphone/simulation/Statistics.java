/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kth.iv1200.xphone.simulation;

/**
 *
 * @author cuneyt
 */
public class Statistics {

    private int replicaId;
    private int droppedCalls;
    private int blockedCalls;
    private int totalCalls;

    public Statistics(int replicaId, int droppedCalls, int blockedCalls, int totalCalls) {
        this.replicaId = replicaId;
        this.droppedCalls = droppedCalls;
        this.blockedCalls = blockedCalls;
        this.totalCalls = totalCalls;
    }

    public double getDroppedCallsPercentage() {
        return (double) droppedCalls / totalCalls;
    }

    public double getBlockedCallsPercentage() {
        return (double) blockedCalls / totalCalls;
    }

    public int getBlockedCalls() {
        return blockedCalls;
    }

    public void setBlockedCalls(int blockedCalls) {
        this.blockedCalls = blockedCalls;
    }

    public int getDroppedCalls() {
        return droppedCalls;
    }

    public void setDroppedCalls(int droppedCalls) {
        this.droppedCalls = droppedCalls;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    public int getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(int totalCalls) {
        this.totalCalls = totalCalls;
    }
}
