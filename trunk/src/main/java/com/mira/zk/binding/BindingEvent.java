package com.mira.zk.binding;


import java.util.EventObject;

/**
 * Event when binding action is triggered
 *
 * @author Mirzayan Arsen
 */
public class BindingEvent extends EventObject {

    public BindingEvent(Object source) {
        super(source);
    }
}
