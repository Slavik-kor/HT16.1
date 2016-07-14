package port;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ship.Ship;
import warehouse.Container;
import warehouse.Warehouse;

public class Port {
	private static final Logger LOGGER = LogManager.getLogger();
	private BlockingQueue<Berth> berthList; // очередь причалов
	private Warehouse portWarehouse; // хранилище порта
	private Map<Ship, Berth> usedBerths; // какой корабль у какого причала стоит

	public Port(int berthSize, int warehouseSize) {
		portWarehouse = new Warehouse(warehouseSize); // создаем пустое
														// хранилище
		berthList = new ArrayBlockingQueue<Berth>(berthSize); // создаем очередь причалов
		for (int i = 0; i < berthSize; i++) { // заполняем очередь причалов
												// непосредственно самими
												// причалами
			berthList.add(new Berth(i, portWarehouse));
		}
		usedBerths = new ConcurrentHashMap<Ship, Berth>(); // создаем объект, который
													// будет
		LOGGER.warn("Порт создан.");
	}

	
	  public void setContainersToWarehouse(List<Container> containerList){
	  portWarehouse.addContainer(containerList); }
	 

	public boolean lockBerth(Ship ship) {
		boolean result = false;
		Berth berth;

		//!!!!!!!!!
		//synchronized (berthList) {
			berth = berthList.poll();
		//}
		
		if (berth != null) {
			result = true;
			usedBerths.put(ship, berth);
		}	
		
		return result;
	}

	public boolean unlockBerth(Ship ship) {
		Berth berth = usedBerths.get(ship);

		synchronized (berthList) {
			berthList.offer(berth);
			usedBerths.remove(ship);	
		}		
		
		return true;
	}

	public Berth getBerth(Ship ship) throws PortException {

		Berth berth = usedBerths.get(ship);
		if (berth == null) {
			throw new PortException("Trying to use Berth without blocking.");
		}
		return berth;
	}
}
