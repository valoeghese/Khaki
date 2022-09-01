package valoeghese.strom.test.simple;

import org.jetbrains.annotations.Nullable;
import valoeghese.strom.utils.GridBox;

import java.util.List;

public class GridBoxTest {
	public static void main(String[] args) {
		GridBox<Integer> box = new GridBox<>(10, 3);
		box.add(4, 6, 4);
		box.add(1, 6, 7);
		box.add(11, 29, 56);

		box.get(1, 4).forEach(System.out::println);


		System.out.printf("Size is %dx%d\n", box.getWidth(), box.getHeight());

		List<Integer>[][] obj = box.toArray();

		System.out.println("As Transposed Array: ");

		for (List<Integer>[] column : obj) {
			for (@Nullable List<Integer> item : column) {
				System.out.print(item == null ? "n " : (item.size() + " "));
			}

			System.out.println();
		}
	}
}
