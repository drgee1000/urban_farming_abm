package repastcity3.agent.factory;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.Uniform;
import repast.simphony.random.RandomHelper;

public class AgentDataGenerator {
	private List<Item> agentDataItems;
	private double relativeProbSum;
	private static Uniform nRand = RandomHelper.getUniform();

	public AgentDataGenerator(List<AgentData> agentDatas) {
		this.agentDataItems = new ArrayList<>(4);
		double sum = 0;
		for (AgentData agentData : agentDatas) {
			sum += agentData.percentage;
			agentDataItems.add(new Item(sum, agentData));
		}
		this.relativeProbSum = sum;
	}

	public AgentData getNext() {
		double prob = nRand.nextDoubleFromTo(0, this.relativeProbSum);
		for (Item item : this.agentDataItems) {
			if (prob < item.relativeProb)
				return item.agentData;
		}
		// dummpy return, never happened
		return null;
	}

	class Item {
		double relativeProb;
		AgentData agentData;

		public Item(double relativeProb, AgentData agentData) {
			this.relativeProb = relativeProb;
			this.agentData = agentData;
		}

	}
}
