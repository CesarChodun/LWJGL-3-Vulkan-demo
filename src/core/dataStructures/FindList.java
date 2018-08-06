package core.dataStructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class FindList<E> {

	public ArrayList<E> data;
	protected Comparator<E> comp;
	
	public FindList(Comparator<E> comp) {
		data = new ArrayList<E>();
		this.comp = comp;
	}
	
	public void add(E obj) {
		data.add(obj);
	}
	
	public void sort() {
		Collections.sort(data, new Comparator<E>() {

			@Override
			public int compare(E o1, E o2) {
				return comp.compare(o1, o2);
			}
			
		});
	}
	
	public void removeDoubles() {
		E last = null;
		int pos = 0;
		int size = data.size()-1;
		
		for(int i = 0; i < size; i++) {
			if(last == null) {
				data.set(pos, data.get(i));
				pos++;
			}
			else if(comp.compare(last, data.get(i)) != 0) {
				data.set(pos, data.get(i));
				pos++;
			}
			last = data.get(i);
		}
		

		for(int i = data.size()-1; i > pos; i--)
			data.remove(i);
	}
	
	protected int smartFind(E val, boolean smaller) {
		
		int o = -1;
		int s = 0;
		int e = data.size()-1;
		
		while(s <= e) {
			int m = (s+e)>>1;
			int r = comp.compare(data.get(m), val);
		
			
			if(r == -1){
				s = m+1;
				if(smaller)
					o = m;
			}
			else {
				e = m-1;
				if(!smaller)
					o = m;
			}
			
			if(r == 0)
				break;
		}
		
		return o;
	}
	
	//12345 3
	
	public int findSmallerOrEqual(E val) {
		return smartFind(val, true);
	}
	
	public int findBiggerOrEqual(E val) {
		return smartFind(val, false);
	}
	
	public int find(E val) {
		int x = smartFind(val, false);
		if(x == -1)
			return -1;
		if(comp.compare(data.get(x), val) != 0)
			return -1;
		return x;
	}
	
	public int size() {
		return data.size();
	}
}
