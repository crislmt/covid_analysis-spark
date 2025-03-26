package com.main;

import akka.actor.ActorSystem;
import com.infrastructure.SmartHome;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class SmartHomeMain {
    public static void main(String[] args) {
        Config config = ConfigFactory.load("smartHome.conf");

        // create the system where actors have to be created
        ActorSystem sys = ActorSystem.create("System", config);

        // create the remote server
        sys.actorOf(SmartHome.props(), "SmartHome");
    }
}
