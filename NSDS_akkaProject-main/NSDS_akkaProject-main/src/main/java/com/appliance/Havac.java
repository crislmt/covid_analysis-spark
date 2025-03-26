package com.appliance;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.message.AdjustAttributeMessage;
import com.message.CrashMessage;
import com.message.SetAttributeMessage;
import com.message.WattMessage;

public class Havac extends AbstractActor {

    private Float prevTarget;
    private float wattConsumed;

    private int timer=0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetAttributeMessage.class, this::handleMessage)
                .match(CrashMessage.class, this::simulateCrash)
                .match(AdjustAttributeMessage.class, this::adjustTemmperature)
                .build();
    }
    public Receive On() {
        return receiveBuilder()
                .match(SetAttributeMessage.class, this::handleMessage)
                .match(CrashMessage.class, this::simulateCrash)
                .match(AdjustAttributeMessage.class, this::adjustTemmperature)
                .build();
    }
    private Receive Off(){
        return receiveBuilder()
                .match(SetAttributeMessage.class, this::handleMessageWhenOff)
                .match(AdjustAttributeMessage.class, this::incrementTimer)
                .build();
    }

    public void incrementTimer(AdjustAttributeMessage sam){
        timer++;
        if(timer>=10){
            timer=0;
            getContext().become(On());
        }
        else {
            sender().tell(new WattMessage(wattConsumed),self());

        }
    }
    public void handleMessageWhenOff(SetAttributeMessage sam){

        sender().tell(new WattMessage(wattConsumed),self());
    }
    private void handleMessage(SetAttributeMessage sam){
        float target=sam.getAttribute();
        if(prevTarget==null){
            System.out.println("Temperature: Initial target change");
            prevTarget=target;
            wattConsumed=target*42;
        }
        else if(prevTarget>=target){
            System.out.println("Temperature: prevTarget: "+prevTarget+ "current Target:" +target);
            System.out.println("Temperature: Decreasing Temperature...");
            wattConsumed=(prevTarget-target)*42;
            prevTarget=target;
        }
        else if(prevTarget<target){
            System.out.println("Temperature: prevTarget: "+prevTarget+ "current Target:" +target);
            System.out.println("Temperature: increasing Temperature...");
            wattConsumed=(target-prevTarget)*42;
            prevTarget=target;

        }
        WattMessage wm=new WattMessage(wattConsumed);
        System.out.println(wm.getWatt());
        sender().tell(wm,self());
    }

    private void adjustTemmperature(AdjustAttributeMessage ms){
        float value=ms.getAttribute();
        if(prevTarget==null){
            System.out.println("Temperature: No target set");
            wattConsumed=0;
        }
        else if(prevTarget>=value){
            System.out.println("Temperature: Increasing Temperature...");
            wattConsumed=(prevTarget-value)*42;
        }
        else if(prevTarget<value){
            System.out.println("Temperature: Decreasing Temperature...");
            wattConsumed=(value-prevTarget)*42;
        }
        WattMessage wm=new WattMessage(wattConsumed);
        sender().tell(wm,self());
        if(wattConsumed> 1300){
            System.out.println("Temperature: Overheating, going to sleep");
            wattConsumed=0;
            getContext().become(Off());
        }
    }

    private void simulateCrash(CrashMessage mes) throws Exception {
        throw new Exception();
    }

    @Override
    public void preStart() throws Exception {
        this.prevTarget=(float)15;
    }

    public static Props props(){
        return Props.create(Havac.class);
    }
}
