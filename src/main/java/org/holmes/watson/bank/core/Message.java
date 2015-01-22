/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.holmes.watson.bank.core;

/**
 *
 * @author Olayinka
 */
public class Message {

    boolean status = false;
    String message;
    String toDo;
    Object attachment;

    private void setStatus(boolean status) {
        this.status = status;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    private void setToDo(String toDo) {
        this.toDo = toDo;
    }

    public boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getToDo() {
        return toDo;
    }

    public Object getAttachment() {
        return attachment;
    }

    public static class MessageBuilder {

        Message message;

        public MessageBuilder(boolean status) {
            message = new Message();
            message.setStatus(status);
        }

        public void message(String message) {
            this.message.setMessage(message);
        }

        public void toDo(String toDo) {
            this.message.setToDo(toDo);
        }

        public void attachment(Object attachment) {
            this.message.setAttachment(attachment);
        }

        public Message build() {
            return message;
        }

    }

    private void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

}
