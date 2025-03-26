package com.sensor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.appliance.Havac;
import com.message.SensorReadMessage;
import com.message.SetupSensorMessage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;


public class TemperatureSensor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SetupSensorMessage.class, this::onSetupMsg).build();
    }

    private void onSetupMsg(SetupSensorMessage msg){
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            float random = (float) (18 + Math.random() * (35 - 18));
            SensorReadMessage newMsg = new SensorReadMessage(random, "temperature");
            sender().tell(newMsg, self());
        }
        /*
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                float random = (float) (18 + Math.random() * (35 - 18));
                SensorReadMessage newMsg = new SensorReadMessage(random,"temperature");
                sender().tell(newMsg, self());
            }
        };
        long period = 30 * 1000; // 30 seconds in milliseconds
        timer.schedule(task, 0, period);*/
    }
    public static Props props(){
        return Props.create(TemperatureSensor.class);
    }
}
