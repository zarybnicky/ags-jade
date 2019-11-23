
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class Initiator extends Agent {

    private static final long serialVersionUID = 1L;
    private final HashMap<AID, Integer> proposals = new HashMap<>();
    private String conversationId;

    @Override
    protected void setup() {
        SequentialBehaviour s = new SequentialBehaviour(this);
        s.addSubBehaviour(new OneShotRunner(this, () -> {
            conversationId = Double.toHexString(Math.random());
            ACLMessage proposal = new ACLMessage(ACLMessage.CFP);
            proposal.setDefaultEnvelope();
            proposal.setConversationId(conversationId);
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription templateSd = new ServiceDescription();
                templateSd.setType("ags-project");
                template.addServices(templateSd);
                DFAgentDescription[] results = DFService.search(this, template);
                for (DFAgentDescription result : results) {
                    proposal.addReceiver(result.getName());
                }
            } catch (FIPAException ex) {
                ex.printStackTrace();
            }
            System.out.println("Sending proposal");
            send(proposal);
        }));
        s.addSubBehaviour(new TimedBehavior(this, 2000, new ReceiveManyBehavior(this, (ACLMessage msg) -> {
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                proposals.put(msg.getSender(), Integer.parseInt(msg.getContent()));
            }
        })));
        s.addSubBehaviour(new OneShotRunner(this, () -> {
            // Remove the largest and smallest
            AID smallestId = null, largestId = null;
            int smallest = Integer.MAX_VALUE, largest = 0;
            for (Entry<AID, Integer> e : proposals.entrySet()) {
                if (e.getValue() < smallest) {
                    smallestId = e.getKey();
                    smallest = e.getValue();
                }
                if (e.getValue() > largest) {
                    largestId = e.getKey();
                    largest = e.getValue();
                }
            }
            HashSet<AID> rejected = new HashSet<>();
            rejected.add(smallestId); proposals.remove(smallestId);
            rejected.add(largestId);  proposals.remove(largestId);

            if (proposals.isEmpty()) {
                System.out.println("No available proposals");
                this.doDelete();
                return;
            }

            // Search for the smallest remaining
            AID acceptedId = null;
            int accepted = Integer.MAX_VALUE;
            for (Entry<AID, Integer> e : proposals.entrySet()) {
                if (e.getValue() < accepted) {
                    acceptedId = e.getKey();
                    accepted = e.getValue();
                }
            }
            proposals.remove(acceptedId);
            
            // Reject all non-accepted
            rejected.addAll(proposals.keySet());
            for (AID id : rejected) {
                ACLMessage m = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                m.addReceiver(id);
                send(m);
            }
            // Accept only one
            System.out.println("Accepting proposal");
            ACLMessage m = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            m.addReceiver(acceptedId);
            send(m);
        }));
        s.addSubBehaviour(new ReceiveManyBehavior(this, (msg) -> {
            if (msg.getPerformative() == ACLMessage.INFORM) {
                System.out.println("Project successful, quitting");
                doDelete();
            } else if (msg.getPerformative() == ACLMessage.FAILURE) {
                System.out.println("Project failed, restarting");
                s.reset();
            } else if (msg.getPerformative() == ACLMessage.PROPOSE) {
                msg.createReply();
                msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                send(msg);
            }
        }));

        s.block(2000);
        addBehaviour(s);
    }
}
