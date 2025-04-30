package com.athena.library.models;

import java.util.Date;

/**
 * Represents a message between users in the library system
 */
public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String senderType; // "STUDENT" or "LIBRARIAN"
    private String receiverId;
    private String receiverName;
    private String receiverType; // "STUDENT" or "LIBRARIAN"
    private String subject;
    private String content;
    private Date sentDate;
    private boolean read;
    private boolean important;

    /**
     * Default constructor
     */
    public Message() {
        this.sentDate = new Date();
        this.read = false;
        this.important = false;
    }

    /**
     * Constructor with essential fields
     */
    public Message(String id, String senderId, String senderName, String senderType,
                   String receiverId, String receiverName, String receiverType,
                   String subject, String content) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderType = senderType;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.receiverType = receiverType;
        this.subject = subject;
        this.content = content;
        this.sentDate = new Date();
        this.read = false;
        this.important = false;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    /**
     * Marks the message as read
     */
    public void markAsRead() {
        this.read = true;
    }

    /**
     * Marks the message as important
     */
    public void markAsImportant() {
        this.important = true;
    }

    /**
     * Checks if the message is from a librarian
     * @return true if the sender is a librarian, false otherwise
     */
    public boolean isFromLibrarian() {
        return "LIBRARIAN".equalsIgnoreCase(senderType);
    }

    /**
     * Checks if the message is from a student
     * @return true if the sender is a student, false otherwise
     */
    public boolean isFromStudent() {
        return "STUDENT".equalsIgnoreCase(senderType);
    }

    /**
     * Gets a formatted string of the sent date
     * @return Formatted date string
     */
    public String getFormattedSentDate() {
        // This could be enhanced with a proper date formatter
        return sentDate.toString();
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", from='" + senderName + " (" + senderType + ")" + '\'' +
                ", to='" + receiverName + " (" + receiverType + ")" + '\'' +
                ", subject='" + subject + '\'' +
                ", sentDate=" + sentDate +
                ", read=" + read +
                '}';
    }
}