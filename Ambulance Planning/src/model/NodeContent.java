package model;

public abstract class NodeContent {

	private int node;

	public NodeContent(int node) {
		this.node = node;
	}

	public abstract int getId();

	public int getNode() {
		return node;
	}

	public void setNode(int node) {
		this.node = node;
	}
}
