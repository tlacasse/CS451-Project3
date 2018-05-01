package ttt.learning;

public interface AI {

	public Matrix input();

	public Matrix output();

	public Matrix[] getWeights();

	public void setWeights(Matrix[] ws);

	public Matrix calculate(Matrix x);

	public double cost(Matrix y);

	public Matrix[] costPrime(Matrix y);

	public String fileName();

}
