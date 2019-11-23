
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;

class TimedBehavior extends WakerBehaviour {
    
    private final Behaviour b;

    public TimedBehavior(Agent myAgent, int timeout, Behaviour b) {
        super(myAgent, timeout);
        this.b = b;
    }

    @Override
    public void onStart() {
        myAgent.addBehaviour(b);
        super.onStart();
    }

    @Override
    protected void onWake() {
        myAgent.removeBehaviour(b);
        super.onWake();
    }
}
