package port;

import warehouse.Warehouse;

public class Berth {

	private int id;
	private Warehouse portWarehouse;

	public Berth(int id, Warehouse warehouse) {
		this.id = id;
		portWarehouse = warehouse;
	}

	public int getId() {
		return id;
	}

	public boolean add(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;

		synchronized (portWarehouse) {

			if (portWarehouse.addContainer(shipWarehouse.getContainer(numberOfConteiners))) {
				result = true;
			}
		}

		return result;

	}

	public boolean get(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;

		synchronized (portWarehouse) {
			if (shipWarehouse.addContainer(portWarehouse.getContainer(numberOfConteiners))) {
				result = true;
			}
		}
		return result;
	}

}
