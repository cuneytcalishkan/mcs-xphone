/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kth.iv1200.xphone.model;

/**
 *
 * @author cuneyt
 */
public class DropCall extends XEvent {

    private BaseStation at;

    public DropCall(Customer customer, BaseStation at) {
        super(customer);
        this.at = at;
    }

    public BaseStation getAt() {
        return at;
    }

    public void setAt(BaseStation at) {
        this.at = at;
    }
}
