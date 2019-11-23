
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.function.Consumer;

class ReceiveManyBehavior extends CyclicBehaviour {
    
    private final Consumer<ACLMessage> consumer;

    public ReceiveManyBehavior(Agent agent, Consumer<ACLMessage> consumer) {
        super(agent);
        this.consumer = consumer;
    }

    @Override
    public void action() {
        final ACLMessage msg = myAgent.receive();
        if (msg == null) {
            block();
            return;
        }
        consumer.accept(msg);
    }
}
