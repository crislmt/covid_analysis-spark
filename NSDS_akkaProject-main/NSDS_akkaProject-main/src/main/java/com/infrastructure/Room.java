package com.infrastructure;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.pattern.Patterns;
import com.appliance.Havac;
import com.appliance.LightAppliance;
import com.message.*;
import com.sensor.LightSensor;
import com.sensor.TemperatureSensor;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Room extends AbstractActor {

    private ActorSystem sys;
    private ActorSelection smartHome;
    private Map<String,Float> values;
    private Map<String, ActorRef> appliances;
    private Map<String, Float> watts;

    private String name;

    private final static SupervisorStrategy strategy = new OneForOneStrategy(
            10,
            Duration.create(10,SECONDS),
            // implement a resume strategy after a crash to keep all the inserted settings in the room
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                    .build());




    public Receive createReceive() {
        return receiveBuilder()
                .match(SetAttributeMessage.class, this::handleSetAttributeMessage)
                .match(SensorReadMessage.class, this::handleSensorReadMessage)
                .match(RoomSetupMessage.class, this::handleSetup)
                .match(AddDeviceMessage.class, this::handleAddDeviceMessage)
                .match(ResponseMessage.class, this::handleResponse)
                .match(CrashMessage.class, this::simulateCrash)
                .match(ReadValueMessage.class, this::handleReadValue)
                .build();
    }

    private void handleSetAttributeMessage(SetAttributeMessage sam){
        if(!appliances.containsKey(sam.getValueName())){
            sender().tell(new ResponseMessage("Room" + name + ": " + "No appliances of " +sam.getValueName() +" in this room"), self());
            return;
        }
        String valueName=sam.getValueName();
        float attribute=sam.getAttribute();
        Duration timeout=Duration.create(10,SECONDS);
        ActorRef appliance=appliances.get(valueName);
        Future future=Patterns.ask(appliance,sam,5000);
        try{
            WattMessage wm=(WattMessage) future.result(timeout,null);
            watts.put(sam.getValueName(), wm.getWatt());
            wm.setWatt(computeRoomEnergy());
            sender().tell(wm,self());
        }
        catch (TimeoutException  | InterruptedException e){
            System.out.println("Room" + name + ": " + "Appliance non risponde");
        }
    }


    private void handleSensorReadMessage(SensorReadMessage srm){
        values.put(srm.getSensorName(),srm.getValue());
        String valueName=srm.getSensorName();
        float attribute=srm.getValue();
        AdjustAttributeMessage sam=new AdjustAttributeMessage(attribute);
        Duration timeout=Duration.create(10,SECONDS);


        ActorRef appliance=appliances.get(valueName);
        Future future=Patterns.ask(appliance,sam,5000);
        try{
            WattMessage wm=(WattMessage) future.result(timeout,null);
            watts.put(valueName, wm.getWatt());
            wm.setWatt(computeRoomEnergy());
            RoomWattMessage rm = new RoomWattMessage(wm.getWatt(), this.name);
            System.out.println("Room " + name + ": watt used: "+ wm.getWatt());
            smartHome.tell(rm,self());
        }
        catch (TimeoutException  | InterruptedException e){
            System.out.println("Room " + name + ": " + "Appliance non risponde");
        }




    }
    private void handleSetup(RoomSetupMessage rsm){
        values=new HashMap<>();
        appliances=new HashMap<>();
        watts=new HashMap<>();
        this.sys=getContext().getSystem();
        this.smartHome=getContext().actorSelection("/user/SmartHome");
        this.name = rsm.getName();
    }

    private void handleAddDeviceMessage(AddDeviceMessage mes){
        System.out.println("Request delivered in Room");
        ResponseMessage rm;
        if(appliances.containsKey(mes.getDeviceType())){
            rm=new ResponseMessage("Appliance of this type already exists for the room selected");
            sender().tell(rm,self());
        }
        else{
            switch (mes.getDeviceType()){
                case "temperature":
                    appliances.put(mes.getDeviceType(), getContext().actorOf(Havac.props(), "havac"));
                    ActorRef tempSensor = getContext().actorOf(TemperatureSensor.props(),"temperatureSensor");
                    tempSensor.tell(new SetupSensorMessage(), self());
                    values.put("temperature",(float)15);
                    watts.put("temperature", (float)0);
                    rm=new ResponseMessage("Request accepted");
                    sender().tell(rm,self());
                    break;
                case "light":
                    appliances.put(mes.getDeviceType(), getContext().actorOf(LightAppliance.props(), "light"));
                    ActorRef lightSensor = getContext().actorOf(LightSensor.props(),"lightSensor");
                    lightSensor.tell(new SetupSensorMessage(), self());
                    values.put("light",(float)50);
                    watts.put("light", (float)0);
                    rm=new ResponseMessage("Request accepted");
                    sender().tell(rm,self());
                    break;
            }
        }
    }

    private float computeRoomEnergy(){
        float sum = 0;
        for(String s: watts.keySet())sum+=watts.get(s);
        return sum;
    }

    private void handleResponse(ResponseMessage mes){
        System.out.println("there is something wrong");
    }

    private void handleReadValue(ReadValueMessage mes){
        String response;
        System.out.println("Reading value");
        if(mes.getSensorType().equals("temperature")){
            response = "There are " + values.get(mes.getSensorType()) + "C";
        } else {
            response = "Light level is " + values.get(mes.getSensorType());
        }
        System.out.println("Value read");
        ResponseMessage rm = new ResponseMessage(response);
        sender().tell(rm, self());
    }
    private void simulateCrash(CrashMessage mes) throws Exception {
        if(mes.getValueName()=="NoValueName"){
            throw new Exception();
        }
        else{
            ActorRef appliance=appliances.get(mes.getValueName());
            appliance.tell(mes,self());
        }
    }

    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }
    public static Props props(){
        return Props.create(Room.class);
    }



}
