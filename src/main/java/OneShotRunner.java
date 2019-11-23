
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

class OneShotRunner extends OneShotBehaviour {

    private final Runnable r;

    public OneShotRunner(Agent agent, Runnable r) {
        super(agent);
        this.r = r;
    }

    @Override
    public void action() {
        r.run();
    }
}
