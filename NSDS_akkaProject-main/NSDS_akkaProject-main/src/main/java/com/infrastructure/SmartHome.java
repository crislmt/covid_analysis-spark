package com.infrastructure;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.japi.pf.DeciderBuilder;
import akka.pattern.Patterns;
import com.message.*;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SmartHome extends AbstractActor {
    private ActorRef panel;
    private float totalWatt;
    private Map<String, Float> wattPerRoom;
    private Map<String, ActorRef> rooms;

    private final static SupervisorStrategy strategy = new OneForOneStrategy(
            10,
            Duration.create(10,SECONDS),
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                    .build());

    //Cluster cluster = Cluster.get(getContext().getSystem());

    // leave the cluster when the actor is removed
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetAttributeMessage.class, this::handleSetPreferenceMessage)
                .match(AddRoomMessage.class, this::handleAddRoom)
                .match(AddDeviceMessage.class, this::handleAddDeviceMessage)
                .match(GetWattMessage.class, this::handleGetWattMessage)
                .match(CrashMessage.class, this::simulateCrash)
                .match(RoomWattMessage.class, this::handleRoomWattMessage)
                .match(ReadValueMessage.class, this::handleReadValueMessageMessage)
                .build();
    }



    private void handleAddRoom(AddRoomMessage mes){
        String roomName=mes.getName();
        ResponseMessage rs;
        if(rooms.containsKey(roomName)){
            rs=new ResponseMessage("Room with that name already exists");
        }
        else {
            rs=new ResponseMessage("Room" + roomName + " Created");
            ActorRef room=getContext().actorOf(Room.props(), mes.getName());
            room.tell(new RoomSetupMessage(roomName),self());
            rooms.put(mes.getName(),room);
            wattPerRoom.put(mes.getName(), (float) 0.0);
        }
        panel=sender();
        panel.tell(rs, self());
    }

    private void handleSetPreferenceMessage(SetAttributeMessage mes){
        panel=sender();
        Duration timeout=Duration.create(10,SECONDS);
        ActorRef room=rooms.get(mes.getRoomName());
        Future future= Patterns.ask(room,mes,5000);
        try {
            //WattMessage wm=(WattMessage) future.result(timeout,null);
            Object o = future.result(timeout,null);
            if(o.getClass()==WattMessage.class){
                WattMessage wm = (WattMessage)o;
                if(wattPerRoom.get(mes.getRoomName())!=wm.getWatt()){
                    wattPerRoom.replace(mes.getRoomName(), wm.getWatt());
                    totalWatt=computeTotalEnergy();
                }
                panel.tell(new WattMessage(totalWatt), self());
            }else if(o.getClass()==ResponseMessage.class){
                ResponseMessage rm = (ResponseMessage)o;
                panel.tell(rm, self());
            }

        } catch (TimeoutException | InterruptedException e){
            System.out.println("Room" + mes.getRoomName() + " non risponde");
        }
    }

    private void handleAddDeviceMessage(AddDeviceMessage mes){
            Duration timeout=Duration.create(10,SECONDS);
            ActorRef room=rooms.get(mes.getRoomName());
            ResponseMessage rm;
            if(room==null){
                rm=new ResponseMessage("The room selected does not exists");
                sender().tell(rm,self());
            }
            else {
                Future response=Patterns.ask(room,mes,5000);
                try {
                    rm=(ResponseMessage) response.result(timeout,null);
                    if(rm.getResponse().equals("Appliance of this type already exists for the room selected")){
                        sender().tell(rm,self());
                    }
                    else {
                        System.out.println(panel);
                        rm=new ResponseMessage("Request accepted");
                        sender().tell(rm,self());
                    }
                } catch (TimeoutException | InterruptedException e){
                    sender().tell(new ResponseMessage("room " + mes.getRoomName() + " does not respond"),self());
                }
            }
    }

    private void handleGetWattMessage(GetWattMessage mes){
        System.out.println("Smart Home: Reading watts "+totalWatt);
        sender().tell(new WattMessage(totalWatt), self());
    }

    private void handleRoomWattMessage(RoomWattMessage wm){
        if(wattPerRoom.get(wm.getRoomName())!=wm.getWatt()){
            wattPerRoom.replace(wm.getRoomName(), wm.getWatt());
            totalWatt=computeTotalEnergy();
        }
    }

    private void handleReadValueMessageMessage(ReadValueMessage mes){
        Duration timeout=Duration.create(10,SECONDS);
        ActorRef room=rooms.get(mes.getRoomName());
        ResponseMessage rm;
        if(room==null){
            rm=new ResponseMessage("The room selected does not exists");
            sender().tell(rm,self());
        } else {
            Future response=Patterns.ask(room,mes,5000);
            try {
                rm=(ResponseMessage) response.result(timeout,null);
                System.out.println(rm.getResponse());
                sender().tell(rm,self());
            } catch (TimeoutException | InterruptedException e){
                sender().tell(new ResponseMessage("room does not respond"),self());
            }
        }
    }

    private float computeTotalEnergy(){
        float sum = 0;
        for(String s: wattPerRoom.keySet())sum+=wattPerRoom.get(s);
        return sum;
    }
    private void simulateCrash(CrashMessage mes){
        Duration timeout=Duration.create(10,SECONDS);
        ActorRef room=rooms.get(mes.getRoomName());
        room.tell(mes,self());
    }

    @Override
    public void postStop() {
        //cluster.unsubscribe(getSelf());
    }

    @Override
    public void preStart() {
        // join the cluster when the actor is created
        //cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
        totalWatt = (float)0.0;
        rooms = new HashMap<>();
        wattPerRoom = new HashMap<>();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public static Props props(){
        return Props.create(SmartHome.class);
    }
}
