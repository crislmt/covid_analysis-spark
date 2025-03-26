package com.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import com.infrastructure.Panel;
import com.message.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Scanner;

import static java.util.concurrent.TimeUnit.SECONDS;

public class PanelMain {
    public static void main(String[] args){
        Config config = ConfigFactory.load("panel.conf");

        ActorSystem sys = ActorSystem.create("System", config);
        final ActorRef panel = sys.actorOf(Panel.props());
        panel.tell(new PanelSetUpMessage(), ActorRef.noSender());
        //panel.tell(new GetWattMessage(), ActorRef.noSender());
        System.out.println("Turning on the system");
        Scanner scanner = new Scanner(System.in);
        int sel;
        Future future;
        ResponseMessage m;
        while (true){
            Duration timeout = Duration.create(10,SECONDS);
            System.out.println("What do you want to do?");
            System.out.println("1)Add room");
            System.out.println("2)Add appliance");
            System.out.println("3)Set a preference");
            System.out.println("4)Get a read on energy usage");
            System.out.println("5)Get sensor read");
            System.out.println("6)Close");
            System.out.println("7)Simulate a crash4");
            sel=scanner.nextInt();
            switch (sel){
                case 1:{
                    System.out.println("Select a room name");
                    scanner.nextLine();
                    String name = scanner.nextLine();
                    try {
                        //panel.tell(new AddRoomMessage(name), ActorRef.noSender());
                        future= Patterns.ask(panel,new AddRoomMessage(name),5000);
                        m=(ResponseMessage) future.result(Duration.create(10,SECONDS),null);
                        System.out.println(m.getResponse());

                    } catch (Exception e){
                        break;
                    }
                    break;
                }
                case 2:{
                    System.out.println("In which room do ou want to add the appliance?");
                    scanner.nextLine();
                    String roomName = scanner.nextLine();
                    System.out.println("What type of appliance is this?");
                    //scanner.nextLine();
                    String applianceType = scanner.nextLine();
                    try {
                        //panel.tell(new AddDeviceMessage(roomName, applianceType, ApplianceOrSensor.APPLIANCE), ActorRef.noSender());
                        future=Patterns.ask(panel,new AddDeviceMessage(roomName,applianceType),5000);
                        m=(ResponseMessage) future.result(Duration.create(10,SECONDS),null);
                        System.out.println(m.getResponse());
                    } catch (Exception e){
                        break;
                    }
                    break;
                }
                /*
                case 3:{
                    System.out.println("In which room do ou want to add the sensor?");
                    String roomName = scanner.nextLine();
                    System.out.println("What type of sensor is this?");
                    String applianceType = scanner.nextLine();
                    try {
                        panel.tell(new AddDeviceMessage(roomName, applianceType), ActorRef.noSender());
                    } catch (Exception e){
                        break;
                    }
                    break;
                }

                 */
                case 3:{
                    System.out.println("In which room do ou want to set the preference?");
                    scanner.nextLine();
                    String roomName = scanner.nextLine();
                    System.out.println("What type of prefernce is this?");
                    //scanner.nextLine();
                    String applianceType = scanner.nextLine();
                    System.out.println("What is the desired value?");
                    Float value = scanner.nextFloat();
                    scanner.nextLine();
                    try {
                        //panel.tell(new AddDeviceMessage(roomName, applianceType, ApplianceOrSensor.APPLIANCE), ActorRef.noSender());
                        future=Patterns.ask(panel,new SetAttributeMessage(roomName, applianceType, value),5000);
                        m=(ResponseMessage) future.result(Duration.create(10,SECONDS),null);
                        System.out.println(m.getResponse());
                    } catch (Exception e){
                        break;
                    }
                    break;
                }
                case 4:{
                    try {
                        future=Patterns.ask(panel,new GetWattMessage(),5000);
                        m=(ResponseMessage) future.result(Duration.create(10,SECONDS),null);
                        System.out.println(m.getResponse());
                    } catch (Exception e){
                        break;
                    }
                    break;
                }
                case 5:{
                    System.out.println("From which room do ou want to get the read?");
                    scanner.nextLine();
                    String roomName = scanner.nextLine();
                    System.out.println("From what sensor do you want to read?");
                    //scanner.nextLine();
                    String sensorType = scanner.nextLine();
                    try {
                        future=Patterns.ask(panel,new ReadValueMessage(roomName, sensorType),5000);
                        m=(ResponseMessage) future.result(Duration.create(10,SECONDS),null);
                        System.out.println(m.getResponse());
                    } catch (Exception e){
                        System.out.println("No response");
                        break;
                    }
                    break;
                }
                case 6:{
                    System.exit(0);
                    break;
                }
                case 7:{
                    System.out.println("In which room do you want a crash?");
                    scanner.nextLine();
                    String roomName = scanner.nextLine();
                    System.out.println("What type of appliance do you want to crash");
                    String applianceType = scanner.nextLine();
                    panel.tell(new CrashMessage(roomName, applianceType), ActorRef.noSender());
                    break;
                }
            }

        }
    }
}
