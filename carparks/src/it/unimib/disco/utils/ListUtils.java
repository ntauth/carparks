package it.unimib.disco.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @brief @see List Utility Methods
 *
 */
public final class ListUtils {

	private static boolean checkSizesMatch(List<Boolean> op1, List<Boolean> op2) {
		
		boolean sizesMatch = op1.size() == op2.size();

		return sizesMatch;
	}
	
	public static List<Boolean> bitwiseIntersect(List<Boolean> operand, List<Boolean> bitmask) {
		
		if (!checkSizesMatch(operand, bitmask))
			throw new IllegalArgumentException("Operand and bitmask must be the same size");
			
		List<Boolean> intersection = new ArrayList<>(operand.size());
		
		for (int i = 0; i < operand.size(); i++)
			intersection.add(operand.get(i) && bitmask.get(i));
		
		return intersection;
	}
	
	public static List<Boolean> bitwiseExclusiveUnion(List<Boolean> operand, List<Boolean> bitmask) {
		
		if (!checkSizesMatch(operand, bitmask))
			throw new IllegalArgumentException("Operand and bitmask must be the same size");
			
		List<Boolean> xunion = new ArrayList<>(operand.size());
		
		for (int i = 0; i < operand.size(); i++)
			xunion.add(operand.get(i) ^ bitmask.get(i));
		
		return xunion;
	}
	
	public static List<Boolean> bitwiseNot(List<Boolean> operand) {
		
		List<Boolean> not = new ArrayList<>(operand.size());
		
		for (int i = 0; i < operand.size(); i++)
			not.add(!operand.get(i));
		
		return not;
	}
	
	public static List<Boolean> makeAdjacencyBitmask(int width, int firstTrueBit, int lastTrueBit) {
		
		boolean widthAndBitsMatch = width > 0 && firstTrueBit >= 0 && firstTrueBit <= lastTrueBit
											&& lastTrueBit < width;
		
		if (!widthAndBitsMatch)
			throw new IllegalArgumentException("Bitmask width and bits must be compatible");
		
		List<Boolean> adjacencyBitmask = new ArrayList<>(width);
		
		for (int i = 0; i < width; i++) {
			
			if (i >= firstTrueBit && i <= lastTrueBit)
				adjacencyBitmask.add(true);
			else
				adjacencyBitmask.add(false);
		}
		
		return adjacencyBitmask;
	}
	
	public static int getTrueBitsCount(List<Boolean> intersection) {
		
		return intersection.stream().map(b -> b ? 1 : 0).reduce(0, (x, y) -> x + y);
	}
	
}
