package me.dustin.jex.event.chat;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;

public class EventSendMessage extends Event {

    private String message;

    public EventSendMessage(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
