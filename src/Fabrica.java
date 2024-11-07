
import java.util.Scanner;

class CintaMontaje {
	private static String[] cinta;
	public int totalBotellas;

	public CintaMontaje(int tamañoCinta, int totalBotellas) {
		cinta = new String[tamañoCinta];
		this.totalBotellas = totalBotellas;
	}

	boolean depositar = true;

	private synchronized void print(String mensaje) {
		System.out.println(mensaje);
	}

	public synchronized void anadirCinta(String botella) throws InterruptedException {
		boolean botellaColocada = false;
		while (!depositar) {
			print("La " + botella + " no se puede agregar porque la cinta está llena.");
			wait();
		}
		print(botella + " agregada a la cinta 🍾");
		for (int i = 0; i < cinta.length; i++) {
			if (cinta[i] == null && !botellaColocada) {
				cinta[i] = botella;
				botellaColocada = true;
			}
		}
		actualizarEspacioDisponible();
		imprimirCinta();
		notifyAll(); // Notifica a todos los hilos que la cinta ha cambiado
	}

	public synchronized void recogerCinta() throws InterruptedException {
		boolean retirada = false;
		while (totalBotellas < 1 ) {
			wait();
		}
		// El recolector sigue intentando retirar botellas mientras haya alguna
		for (int i = 0; i < cinta.length; i++) {
			if (cinta[i] != null && !retirada) {
				String botella = cinta[i];
				cinta[i] = null;
				System.out.println(botella + " eliminada de la cinta por el recolector.");
				retirada = true;
				totalBotellas--;
			}
		}
		actualizarEspacioDisponible();
		imprimirCinta();

		notify(); // Notifica a los hilos que la cinta ha cambiado
	}

	private static void imprimirCinta() {
		System.out.print("Cinta: ");
		for (String espacio : cinta) {
			if (espacio == null) {
				System.out.print("[0] "); // Imprime [0] si el espacio está vacío
			} else {
				System.out.print("[" + espacio + "] "); // Imprime el contenido en formato [elemento]
			}
		}
		System.out.println();
	}

	private void actualizarEspacioDisponible() {
		int huecos = 0;
		for (String espacio : cinta) {
			if (espacio == null) {
				huecos++; // Contamos los espacios vacíos
			}
		}
		depositar = huecos > 0; // Hay espacio si hay al menos un hueco disponible
	}
}

class Productor extends Thread {
	private CintaMontaje cinta;
	private int totalBotellas;

	public Productor(CintaMontaje cinta, int totalBotellas) {
		super();
		this.cinta = cinta;
		this.totalBotellas = totalBotellas;
	}

	@Override
	public void run() {
		try {
			for (int i = 1; i <= totalBotellas; i++) {
				cinta.anadirCinta("Botella " + i);
				Thread.sleep(1000); // Simula el tiempo que tarda el productor en agregar una botella
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class Recolector extends Thread {
	private CintaMontaje cinta;

	public Recolector(CintaMontaje cinta) {
		super();
		this.cinta = cinta;
	}

	@Override
	public void run() {
		try {
			while (cinta.totalBotellas > 0) { // El recolector sigue trabajando hasta que no haya más botellas
				cinta.recogerCinta();
				Thread.sleep(3000); // Simula el tiempo que tarda el recolector en retirar una botella
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

public class Fabrica {
	public static void main(String[] args) throws InterruptedException {

		Scanner teclado = new Scanner(System.in);

		System.out.println("Indica cuántas botellas tiene que producir el productor: ");
		int totalBotellas = teclado.nextInt();
		System.out.println("Indica el tamaño de la cinta: ");
		int tamaño = teclado.nextInt();
		CintaMontaje cinta = new CintaMontaje(tamaño, totalBotellas);

		Productor productor1 = new Productor(cinta, totalBotellas);
		Recolector recolector1 = new Recolector(cinta);

		productor1.start();
		recolector1.start();

		productor1.join();
		recolector1.join();

		teclado.close();
	}
}
