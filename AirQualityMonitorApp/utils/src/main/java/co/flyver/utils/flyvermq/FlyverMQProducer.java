package co.flyver.utils.flyvermq;

import co.flyver.utils.flyvermq.exceptions.FlyverMQException;
import co.flyver.utils.flyvermq.exceptions.NoSuchTopicException;
import co.flyver.utils.flyvermq.exceptions.ProducerAlreadyRegisteredException;

/**
 * Created by Petar Petrov on 1/12/15.
 */

/**
 * Abstract class implementing MQ producer functionallity
 * Every producer must subclass it before registering for the message queue
 */
public abstract class FlyverMQProducer {
    final String TAG = "Producers";
    String topic;
    boolean registered = false;

    FlyverMQProducer() {

    }

    public FlyverMQProducer(String topic) {
        this.topic = topic;
    }

    public void addMessage(FlyverMQMessage msg) {
        if(registered) {
            try {
                FlyverMQ.getInstance().addMessage(this, msg);
            } catch (NoSuchTopicException | FlyverMQException e) {
                e.printStackTrace();
            }
        }

    }

    public void register(boolean removeExisting) throws ProducerAlreadyRegisteredException {
        try {
            FlyverMQ.getInstance().registerProducer(this, topic, removeExisting);
        } catch (FlyverMQException e) {
            e.printStackTrace();
        }
        registered = true;
    }

    public void register(boolean removeExisting, int limit) throws ProducerAlreadyRegisteredException {
        try {
            FlyverMQ.getInstance().registerProducer(this, topic, removeExisting, limit);
        } catch (FlyverMQException e) {
            e.printStackTrace();
        }
        registered = true;
    }

    /**
     * Callback called when the producer is sucessfully registered to the queue
     */
    abstract public void registered();

    /**
     * Callback called when the producer is removed from the queue by any means
     */
    abstract public void unregistered();

    /**
     * Callback called when the producer is requested to pause producing messages
     */
    abstract public void onPause();

    /**
     * Callback called when an already paused producer is requested to resume it's duties
     */
    abstract public void onResume();
}
