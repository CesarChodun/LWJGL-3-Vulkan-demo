package core.employees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public class Evaluator<T>{

	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Evaluates object value.
	 * </p>
	 */
	public interface Judge<T>{
		/**
		 * @param object	- object to be evaluated
		 * @return - value of the object
		 */
		public int calculate(T object);
	}
	
	private Judge<T> judge;
	private List<Integer> values = new ArrayList<Integer>();
	private List<T> objects = new ArrayList<T>(); 
	
	/**
	 * 
	 * @param j - judge object that will calculate value of each element of <i><b>objects</b></i>.
	 * @param objects - objects that will be added to the collection
	 */
	public Evaluator(Judge<T> j,@Nullable T[] objects) {
		judge = j;
		if(objects != null)
			addNew(objects);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Adds new object to the list.
	 * </p>
	 * @param object - object that will be added to the collection
	 */
	public void addNew(T object) {
		int val = judge.calculate(object);
		int pos = Collections.binarySearch(values, val);
		
		if(pos < 0)
			pos = -(pos+1);
		
		if(pos < values.size()) {
			values.add(pos, val);
			objects.add(pos, object);
			return;
		}
		
		values.add(val);
		objects.add(object);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Adds multiple objects to the list.
	 * </p>
	 * @param objects - objects that will be added to the collection
	 */
	public void addNew(T[] objects) {
		for(T t : objects)
			addNew(t);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Removes object from the list.
	 * </p>
	 * @param object - object which should be removed
	 */
	public int remove(T object) {
		int pos = Collections.binarySearch(values, judge.calculate(object));
		
		if(pos < 0) {		
			values.remove(pos);
			objects.remove(pos);
		}
		
		return pos;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Returns objects in the ascending order(value wise).
	 * </p>
	 * @return - objects in ascending order
	 */
	public List<T> getInOrder() {
		return objects;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Returns object with the smallest value.
	 * </p>
	 * @return - object with the smallest value
	 */
	public T getFirst() {
		if(objects.size() == 0)
			return null;
		return objects.get(0);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Returns object with the biggest value.
	 * </p>
	 * @return - object with the biggest value
	 */
	public T getLast() {
		if(objects.size() == 0)
			return null;
		return objects.get(objects.size()-1);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Clears the collection.
	 * </p>
	 */
	public void clear() {
		values.clear();
		objects.clear();
	}
}
