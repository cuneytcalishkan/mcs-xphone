/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kth.iv1200.xphone.model;

/**
 *
 * @author cuneyt
 */
public class XEvent {

    private Customer customer;

    public XEvent(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setTime(double time) {
        customer.setTime(time);
    }

    public void setSpeed(double speed) {
        customer.setSpeed(speed);
    }

    public void setPosition(double position) {
        customer.setPosition(position);
    }

    public void setId(int id) {
        customer.setId(id);
    }

    public void setDuration(double duration) {
        customer.setDuration(duration);
    }

    public double getTime() {
        return customer.getTime();
    }

    public double getSpeed() {
        return customer.getSpeed();
    }

    public double getPosition() {
        return customer.getPosition();
    }

    public int getId() {
        return customer.getId();
    }

    public double getDuration() {
        return customer.getDuration();
    }
}
