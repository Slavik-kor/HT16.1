package port;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ship.Ship;
import warehouse.Container;
import warehouse.Warehouse;

public class Port {
	private static final Logger LOGGER = LogManager.getLogger();
	private ArrayDeque<Berth> berthList; // очередь причалов
	private Warehouse portWarehouse; // хранилище порта
	private Map<Ship, Berth> usedBerths; // какой корабль у какого причала стоит

	public Port(int berthSize, int warehouseSize) {
		portWarehouse = new Warehouse(warehouseSize); // создаем пустое хранилище
		berthList = new ArrayDeque<Berth>(berthSize); // создаем очередь причалов
		for (int i = 0; i < berthSize; i++) { // заполняем очередь причалов
												// непосредственно самими
												// причалами
			berthList.add(new Berth(i, portWarehouse));
		}
		usedBerths = new HashMap<Ship, Berth>(); // создаем map с занятыми причалами

		LOGGER.warn("Порт создан.");
	}

	public void setContainersToWarehouse(List<Container> containerList) {
		portWarehouse.addContainer(containerList);
	}

	public synchronized boolean lockBerth(Ship ship) {
		boolean result = false;
		Berth berth = berthList.poll(); 			//извлекаем ссылку на причал из очереди причалов

		if (berth != null) {
			usedBerths.put(ship, berth);	      //вставляем в map с занятыми причалами
			result = true;                     
		}

		return result;
	}

	public synchronized boolean unlockBerth(Ship ship) {
		Berth berth = usedBerths.get(ship);

		berthList.offer(berth);
		usedBerths.remove(ship);

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
