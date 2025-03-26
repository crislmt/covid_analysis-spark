package com.appliance;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.message.AdjustAttributeMessage;
import com.message.CrashMessage;
import com.message.SetAttributeMessage;
import com.message.WattMessage;

import java.util.Set;

public class LightAppliance extends AbstractActor {
    private Float prevTarget;
    private float wattConsumed;
    private int timer=0;

    @Override
    public Receive createReceive() {
        return On();
    }


    public Receive On() {
        return receiveBuilder()
                .match(SetAttributeMessage.class, this::handleMessage)
                .match(CrashMessage.class, this::simulateCrash)
                .match(AdjustAttributeMessage.class, this::adjustLight)
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
    }
    public void handleMessageWhenOff(SetAttributeMessage sam){

        sender().tell(new WattMessage(wattConsumed),self());
    }
    private void handleMessage(SetAttributeMessage sam){
        float target=sam.getAttribute();
        if(prevTarget==null){
            System.out.println("Light: Initial target change");
            prevTarget=target;
            wattConsumed=target*31;
        }
        else if(prevTarget>=target){
            System.out.println("Light: prevTarget: "+prevTarget+ "current Target:" +target);
            System.out.println("Decreasing light...");
            wattConsumed=(prevTarget-target)*31;
            prevTarget=target;
        }
        else if(prevTarget<target){
            System.out.println("Light: prevTarget: "+prevTarget+ "current Target:" +target);
            System.out.println("increasing light...");
            wattConsumed=(target-prevTarget)*31;
            prevTarget=target;

        }
        WattMessage wm=new WattMessage(wattConsumed);
        sender().tell(wm,self());
    }

    private void adjustLight(AdjustAttributeMessage ms){
        float value=ms.getAttribute();
        if(prevTarget==null){
            System.out.println("Light: No target set");
            wattConsumed=0;
        }
        else if(prevTarget>=value){
            System.out.println("Light: Increasing Light...");
            wattConsumed=(prevTarget-value)*31;
        }
        else if(prevTarget<value){
            System.out.println("Light: Decreasing Light...");
            wattConsumed=(value-prevTarget)*31;
        }
        WattMessage wm=new WattMessage(wattConsumed);
        sender().tell(wm,self());

        if(wattConsumed> 2000){
            System.out.println("Light: Overheating, going to sleep");
            wattConsumed=0;
            getContext().become(Off());

        }
    }

    private void simulateCrash(CrashMessage mes) throws Exception {
        throw new Exception();
    }

    @Override
    public void preStart() throws Exception {
        this.prevTarget=(float)50;
    }

    public static Props props(){
        return Props.create(LightAppliance.class);
    }
}
