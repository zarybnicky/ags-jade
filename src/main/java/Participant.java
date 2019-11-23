
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class Participant extends Agent {

    @Override
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("vypracování projektu z AGS");
        sd.setType("ags-project");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        SequentialBehaviour s = new SequentialBehaviour(this);
        s.addSubBehaviour(new OneShotRunner(this, () -> {
            final ACLMessage msg = blockingReceive();
            final ACLMessage reply = msg.createReply();
            double state = Math.random();
            if (state < 0.2) {
                System.out.println(getName() + " Refusing to offer");
                reply.setPerformative(ACLMessage.REFUSE);
            } else if (state < 0.9) {
                System.out.println(getName() + " Sending offer");
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(Long.toString(Math.round(Math.random() * 100)));
            } else {
                System.out.println(getName() + " Sending offer late");
                blockingReceive(2000);
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(Long.toString(Math.round(Math.random() * 100)));
            }
            send(reply);
        }));
        s.addSubBehaviour(new OneShotRunner(this, () -> {
            final ACLMessage msg = blockingReceive();
            if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                ACLMessage r = msg.createReply();
                if (Math.random() > 0.2) {
                    System.out.println(getName() + " Contractor successful");
                    r.setPerformative(ACLMessage.INFORM);
                } else {
                    System.out.println(getName() + " Contractor failed");
                    r.setPerformative(ACLMessage.FAILURE);
                }
                send(r);
            } else {
                System.out.println(getName() + " Offer rejected");
            }
            s.reset();
        }));
        addBehaviour(s);
    }
}
