package com.infrastructure;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.pattern.Patterns;
import com.message.*;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Panel extends AbstractActor {
    private ActorSystem sys;
    private ActorSelection home;
    private float totalWatt;
    private List<String> rooms;
    private Duration timeout=Duration.create(10,SECONDS);


    //Cluster cluster = Cluster.get(getContext().getSystem());

    // leave the cluster when the actor is removed
    @Override
    public void postStop() {
        //cluster.unsubscribe(getSelf());
    }

    @Override
    public void preStart() {
        // join the cluster when the actor is created
        //cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PanelSetUpMessage.class, this::setUp)
                .match(GetWattMessage.class, this::askWatts)
                .match(AddRoomMessage.class, this::addRoom)
                .match(AddDeviceMessage.class, this::addDevice)
                .match(SetAttributeMessage.class, this::setPreference)
                .match(CrashMessage.class, this::simulateCrash)
                .match(ReadValueMessage.class, this::readValues)
                .build();
    }

    public void setUp(PanelSetUpMessage mes) {
        this.sys=getContext().getSystem();
        this.home=getContext().actorSelection("akka://System@192.168.100.5:2551/user/SmartHome");
        this.rooms = new ArrayList<>();
    }

    private void addRoom(AddRoomMessage mes){
        Future future= Patterns.ask(home,mes,10000);
        try {
            ResponseMessage wm=(ResponseMessage) future.result(timeout,null);
            rooms.add(mes.getName());
            sender().tell(wm,self());
        }  catch (TimeoutException | InterruptedException e){
            System.out.println("SmartHome non risponde");
        }
    }

    private void addDevice(AddDeviceMessage mes){
        System.out.println("Request delivered in panel");
        Future future= Patterns.ask(home,mes,10000);
        try {
            ResponseMessage wm=(ResponseMessage) future.result(timeout,null);
            sender().tell(wm,self());
        }  catch (TimeoutException | InterruptedException e){
            System.out.println(e);
            sender().tell(new ResponseMessage("SmartHome non risponde"), self());
        }
    }

    private void setPreference(SetAttributeMessage mes){
        System.out.println("setting the preference");
        if(!rooms.contains(mes.getRoomName())) {
            sender().tell(new ResponseMessage("Room "+mes.getRoomName()+" does not exsist"), self());
            return;
        }
        Future future= Patterns.ask(home,mes,10000);
        try {
            //WattMessage wm=(WattMessage) future.result(timeout,null);
            Object o = future.result(timeout,null);
            if(o.getClass()==WattMessage.class){
                WattMessage wm = (WattMessage)o;
                totalWatt = wm.getWatt();
                sender().tell(new ResponseMessage("Watt used: "+totalWatt),self());
            }else if(o.getClass()==ResponseMessage.class){
                ResponseMessage rm = (ResponseMessage)o;
                System.out.println(rm.getResponse());
            }

        }  catch (TimeoutException | InterruptedException e){
            System.out.println(e);
            sender().tell(new ResponseMessage("SmartHome non risponde"), self());
        }
    }

    private void askWatts(GetWattMessage mes){
        Duration timeout=Duration.create(10,SECONDS);
        Future future= Patterns.ask(home,mes,10000);
        try {
            WattMessage wm=(WattMessage) future.result(timeout,null);
            totalWatt = wm.getWatt();
            sender().tell(new ResponseMessage("Watt used: "+totalWatt), self());
        }  catch (TimeoutException | InterruptedException e){
            sender().tell(new ResponseMessage("SmartHome non risponde"), self());
        }
    }

    private void readValues(ReadValueMessage mes){
        Duration timeout=Duration.create(10,SECONDS);
        Future future= Patterns.ask(home,mes,10000);
        try {
            ResponseMessage rm=(ResponseMessage) future.result(timeout,null);
            sender().tell(rm, self());
        }  catch (TimeoutException | InterruptedException e){
            sender().tell(new ResponseMessage("SmartHome non risponde"), self());
        }
    }

    private void simulateCrash(CrashMessage mes){
        System.out.println("setting the preference");
        home.tell(mes,self());
    }
    public static Props props(){
        return Props.create(Panel.class);
    }

}
