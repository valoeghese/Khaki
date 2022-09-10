package valoeghese.khaki.test.simple;

import org.jetbrains.annotations.Nullable;
import valoeghese.khaki.utils.GridBox;

import java.util.Set;

public class GridBoxTest {
	public static void main(String[] args) {
		GridBox<Integer> box = new GridBox<>(10, 3, 0, 0);
		box.add(4, 6, 4);
		box.add(1, 6, 7);
		box.add(11, 29, 56);

		box.get(1, 4).forEach(System.out::println);


		System.out.printf("Size is %dx%d\n", box.getWidth(), box.getHeight());

		System.out.println("Original (Pre-Frame) As Transposed Array: ");
		Set<Integer>[][] obj = box.toArray();

		for (Set<Integer>[] column : obj) {
			for (@Nullable Set<Integer> item : column) {
				System.out.print(item == null ? "n " : (item.size() + " "));
			}

			System.out.println();
		}

		System.out.println("Creating GridBox Frame & adding 2 values");

		GridBox<Integer> frame = box.createFrame();
		frame.add(-4, 7, 420);
		frame.add(-14, 7, 21);

		System.out.println("Frame As Transposed Array: ");
		obj = frame.toArray();

		for (Set<Integer>[] column : obj) {
			for (@Nullable Set<Integer> item : column) {
				System.out.print(item == null ? "n " : (item.size() + " "));
			}

			System.out.println();
		}

		System.out.println("Original (Post-Frame) As Transposed Array: ");
		obj = box.toArray();

		for (Set<Integer>[] column : obj) {
			for (@Nullable Set<Integer> item : column) {
				System.out.print(item == null ? "n " : (item.size() + " "));
			}

			System.out.println();
		}
	}
}
