package repastcity3.environment;

public class Supermarket extends Building {
	private double score;
	private int score_count;
	@Override
	public String toString() {
		return "Supermarket: " + this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Supermarket))
			return false;
		Supermarket b = (Supermarket) obj;
		return this.identifier.equals(b.identifier);
	}
	public void updateScore(double newScore) {
		this.score_count += 1;
		double score_sum = this.score + newScore;
		this.score = score_sum/score_count;
	}
	
	public double getScore() {
		return this.score;
	}

}
