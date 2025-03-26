package com.sensor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.message.SensorReadMessage;
import com.message.SetupSensorMessage;

public class LightSensor extends AbstractActor{
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder().match(SetupSensorMessage.class, this::onSetupMsg).build();
    }

    private void onSetupMsg(SetupSensorMessage msg){
        while(true) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            float random = (float) (50 + Math.random() * (70 - 30));
            SensorReadMessage newMsg = new SensorReadMessage(random, "light");
            sender().tell(newMsg, self());
        }
    }
    public static Props props(){
        return Props.create(LightSensor.class);
    }
}
