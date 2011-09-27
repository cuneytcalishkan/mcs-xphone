/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kth.iv1200.xphone.model;

/**
 *
 * @author cuneyt
 */
public class Handover extends XEvent {

    private BaseStation from;

    public Handover(Customer customer, BaseStation from) {
        super(customer);
        this.from = from;
    }

    public BaseStation getFrom() {
        return from;
    }

    public void setFrom(BaseStation from) {
        this.from = from;
    }
}
