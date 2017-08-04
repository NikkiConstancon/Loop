package com.reva.loop301.reva;

/**
 * Created by Hristian Vitrychenko on 22/07/2017.
 */

/**
 * The CardNotification class acts as an object that stores
 * information about notifications that come through from the server.
 */
public class CardNotification {

    /**
     * Notification details:
     * title for describing what the notification is about,
     * message for the content of the notification,
     * and colour to represent severity.
     */
    private String title, message, colour;

    /**
     * Constructor for CardNotification (instantiates parameters)
     * @param title holds the title of the notification (what it is about)
     * @param message holds the body of the notification (details)
     * @param colour holds the status of the notification (red - bad, yellow - moderate, white - good)
     */
    public CardNotification(String title, String message, String colour)
    {
        this.title = title;
        this.message = message;
        this.colour = colour;
    }

    /**
     * getter method for colour
     * @return the colour of the notification
     */
    public String getColour() {
        return colour;
    }

    /**
     * getter method for title
     * @return the title of the notification
     */
    public String getTitle() {
        return title;
    }

    /**
     * getter method for message
     * @return the message of the notification
     */
    public String getMessage() {
        return message;
    }

    /**
     * setter method for title
     * @param title holds the title of the notification
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * settter method for message
     * @param message holds the message of the notification
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * setter method for colour
     * @param colour holds the colour of the notification
     */
    public void setColour(String colour) {
        this.colour = colour;
    }
}
