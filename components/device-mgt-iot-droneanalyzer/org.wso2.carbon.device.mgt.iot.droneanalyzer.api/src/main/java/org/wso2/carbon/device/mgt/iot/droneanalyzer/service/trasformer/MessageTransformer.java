package org.wso2.carbon.device.mgt.iot.droneanalyzer.service.trasformer;


import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.MessageConfig;

import java.io.IOException;


public class MessageTransformer {

    private Log log = LogFactory.getLog(MessageTransformer.class);
    private CircularFifoQueue<String> sharedQueue;

    private String outbound_message_format_for_simulator = "{\"quatanium_val\":[%f, %f, %f, %f]," +
            "\"basicParam\":{\"velocity\":[%f, %f, %f], \"global_location\":[%f, %f, %f]},\"battery_level\":%f, \"device_type\":\"IRIS_DRONE\"}";
    private String outbound_message_format_for_iris_drone = "{\"quatanium_val\":[%f, %f, %f]," +
            "\"basicParam\":{\"velocity\":[%f, %f, %f], \"global_location\":[%f, %f, %f]},\"battery_level\":%f," +
            "\"device_type\":\"SIMULATOR\"}";

    public MessageTransformer(){
        sharedQueue = new CircularFifoQueue<String>(DroneConstants.MAXIMUM_BUFFERE_SIZE_OF_SHARED_QUEUE);
    }

    private void messageTranslaterForSimulator(JsonNode inbound_message){
        JsonNode node = inbound_message;
        String outbound_message;

        try {
            JsonNode velocity = node.get(MessageConfig.OUT_BASIC_PARAM_VAL).get(MessageConfig.OUT_BASIC_PARAM_VELOCITY);
            JsonNode global_location = node.get(MessageConfig.OUT_BASIC_PARAM_VAL).get(
                    MessageConfig.OUT_BASIC_PARAM_GLOBAL_LOCATION);
            JsonNode quatanium_vals = node.get(MessageConfig.OUT_QUATANNIM_VAL);
            JsonNode battery_level = node.get(MessageConfig.OUT_BATTERY_LEVEL);
            outbound_message = String.format(outbound_message_format_for_simulator, sTd(quatanium_vals.get(0)),
                    sTd(quatanium_vals.get(1)), sTd(quatanium_vals.get(2)), sTd(quatanium_vals.get(0)),
                    sTd(velocity.get(0)), sTd(velocity.get(1)), sTd(velocity.get(2)), sTd(global_location.get(0)),
                    sTd(global_location.get(1)), sTd(global_location.get(2)), sTd(battery_level));
            sharedQueue.add(outbound_message);
        } catch (Exception e) {
            log.error(e.getMessage()+",\n"+ e);
        }
    }

    private void messageTranslaterForIRISDrone(JsonNode inbound_message){
        JsonNode node = inbound_message;
        String outbound_message;
        try {

            JsonNode velocity = node.get(MessageConfig.OUT_BASIC_PARAM_VAL).get(MessageConfig.OUT_BASIC_PARAM_VELOCITY);
            System.out.println("-------1----------");
            JsonNode global_location = node.get(MessageConfig.OUT_BASIC_PARAM_VAL).get(
                    MessageConfig.OUT_BASIC_PARAM_GLOBAL_LOCATION);
            System.out.println("-------2----------");
            JsonNode quatanium_vals = node.get(MessageConfig.OUT_QUATANNIM_VAL);
            JsonNode battery_level = node.get(MessageConfig.OUT_BATTERY_LEVEL);
            System.out.println("-------3----------");

            outbound_message = String.format(outbound_message_format_for_iris_drone, sTd(quatanium_vals.get(0)),
                    sTd(quatanium_vals.get(1)), sTd(quatanium_vals.get(2)), sTd(velocity.get(0)),
                    sTd(velocity.get(1)), sTd(velocity.get(2)), sTd(global_location.get(0)),
                    sTd(global_location.get(1)), sTd(global_location.get(2)), sTd(battery_level));

            System.out.println("-----------------IRIS_DRONE---------------------"+ outbound_message);

            sharedQueue.add(outbound_message);

        }catch (Exception e) {
            log.error(e.getMessage()+",\n"+ e);
            System.out.println( e.getMessage());
        }
    }

    public void messageTranslater(String inbound_message){
        JsonNode actualMessage = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            actualMessage = objectMapper.readValue(inbound_message, JsonNode.class);
            JsonNode deviceType = actualMessage.get(MessageConfig.IN_DEVICE_TYPE);
            switch (deviceType.getTextValue()) {

                case MessageConfig.IN_IRIS_DRONE:
                    System.out.println("incomming message :" + inbound_message);
                    messageTranslaterForIRISDrone(actualMessage);
                    break;
                case MessageConfig.IN_SIMULATOR:
                    messageTranslaterForSimulator(actualMessage);
                    break;
            }
        } catch (JsonProcessingException e) {
            log.error("Incoming message might be corrupted, "+ e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private double sTd(JsonNode s)
    {
        return Double.parseDouble(s.toString());
    }

    public String getMessage() {
       try{
           if(sharedQueue.isEmpty() || sharedQueue == null){
               return "";
           }
           return sharedQueue.remove();
       }catch(Exception e) {
           log.error("There is no more messages to send or internal server error has been occurred, \n"+ e );
           return "";
       }
    }

    public boolean isEmptyQueue(){
        return sharedQueue != null? sharedQueue.isEmpty():false;
    }

}
