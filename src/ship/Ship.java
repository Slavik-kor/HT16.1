package ship;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import org.apache.log4j.Logger;

import port.Berth;
import port.Port;
import port.PortException;
import warehouse.Container;
import warehouse.Warehouse;

public class Ship implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger();
	private volatile boolean stopThread = false;

	private String name;
	private Port port;
	private Warehouse shipWarehouse;

	public Ship(String name, Port port, int shipWarehouseSize) {
		this.name = name;
		this.port = port;
		shipWarehouse = new Warehouse(shipWarehouseSize);
	}

	/*
	 * public void setContainersToWarehouse(List<Container> containerList) {
	 * shipWarehouse.addContainer(containerList); }
	 */

	public String getName() {
		return name;
	}

	public void stopThread() {
		stopThread = true;
	}

	public void run() {
		try {
			while (!stopThread) {
				atSea();
				inPort();
			}
		} catch (InterruptedException e) {
			LOGGER.error("Корабль затонул");
		} catch (PortException e) {
			LOGGER.error("Корабль затонул");
		}
	}

	private void atSea() throws InterruptedException {
		Thread.sleep(1000);
	}

	public void setContainersToWarehouse(List<Container> containerList) {
		shipWarehouse.addContainer(containerList);
	}

	private void inPort() throws PortException, InterruptedException {

		boolean isLockedBerth = false;
		Berth berth = null;
		try {
			isLockedBerth = port.lockBerth(this);
			if (isLockedBerth) {
				berth = port.getBerth(this);
				LOGGER.warn("Корабль " + name + " пришвартовался к причалу " + berth.getId());
				ShipAction action = getNextAction();
				executeAction(action, berth);
			} else {
				LOGGER.warn("Кораблю " + name + " отказано в швартовке к причалу ");
			}
		} finally {
			if (isLockedBerth) {
				port.unlockBerth(this);
				LOGGER.warn("Корабль " + name + " отошел от причала " + berth.getId());
			}
		}

	}

	private void executeAction(ShipAction action, Berth berth) throws InterruptedException {
		switch (action) {
		case LOAD_TO_PORT:
			loadToPort(berth);
			break;
		case LOAD_FROM_PORT:
			loadFromPort(berth);
			break;
		}
	}

	private boolean loadToPort(Berth berth) throws InterruptedException {

		int containersNumberToMove = conteinersCount();
		int realShipSize = shipWarehouse.getRealSize();
		if(containersNumberToMove > realShipSize){
			containersNumberToMove = realShipSize;}
		boolean result = false;

		LOGGER.warn("Корабль " + name + " хочет загрузить " + containersNumberToMove + " контейнеров на склад порта.");

		result = berth.add(shipWarehouse, containersNumberToMove);

		if (!result) {
			LOGGER.warn("Недостаточно места на складе порта для выгрузки кораблем " + name + " "
					+ containersNumberToMove + " контейнеров.");
		} else {
			LOGGER.warn("Корабль " + name + " выгрузил " + containersNumberToMove + " контейнеров в порт.");

		}
		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException {

		int containersNumberToMove = conteinersCount();

		boolean result = false;

		LOGGER.warn("Корабль " + name + " хочет загрузить " + containersNumberToMove + " контейнеров со склада порта.");

		result = berth.get(shipWarehouse, containersNumberToMove);

		if (result) {
			LOGGER.warn("Корабль " + name + " загрузил " + containersNumberToMove + " контейнеров из порта.");
		} else {
			LOGGER.warn("Недостаточно места на на корабле " + name + " для погрузки " + containersNumberToMove
					+ " контейнеров из порта.");
		}

		return result;
	}

	private int conteinersCount() {// !!!!
		Random random = new Random();
		return random.nextInt(20) + 1;

	}

	private ShipAction getNextAction() {
		if(shipWarehouse.getRealSize() == 0){
			return ShipAction.LOAD_FROM_PORT;
		}
		Random random = new Random();
		int value = random.nextInt(4000);
		if (value < 1000) {
			return ShipAction.LOAD_TO_PORT;
		} else if (value < 2000) {
			return ShipAction.LOAD_FROM_PORT;
		}
		return ShipAction.LOAD_TO_PORT;
	}

	enum ShipAction {
		LOAD_TO_PORT, LOAD_FROM_PORT
	}
}
