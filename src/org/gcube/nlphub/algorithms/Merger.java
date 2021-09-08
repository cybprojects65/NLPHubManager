package org.gcube.nlphub.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Merger {

	
	
	public List<int []> mergeSegment (int []segment1, int [] segment2) {
		List<int []> merged = new ArrayList<int []>();
		
		if(segment1[0] <= segment2[0]) {
			if(segment1[1] < segment2[0]) {
				//merged = [segment1, segment2];
				merged.add(segment1);
				merged.add(segment2);
			}
			else {
				if(segment1[1] >= segment2[1])
					//merged = [segment1];
					merged.add(segment1);
				else {
					//merged = [[segment1[0], segment2[1]]];
					int [] segment = {segment1[0], segment2[1]};
					merged.add(segment);
				}
			}
		}
		else {
			if(segment2[1] < segment1[0]) {
				//merged = [segment2, segment1];
				merged.add(segment2);
				merged.add(segment1);
			}
			else {
				if(segment2[1] >= segment1[1]) 
					//merged = [segment2];
					merged.add(segment2);
				else {
					//merged = [[segment2[0], segment1[1]]];
					int [] segment = {segment2[0], segment1[1]};
					merged.add(segment);
				}
			}
		}
		return merged;
	}

	public int compareSegment(int [] segment1, int [] segment2) {
		// included or coincident
		if((segment1[0] >= segment2[0]) && (segment1[1] <= segment2[1]))
			return 0;
		if((segment1[0] <= segment2[0]) && (segment1[1] >= segment2[1]))
			return 0;
		// external
		if((segment1[1] < segment2[0]) || ((segment1[0] > segment2[1])))
			return 1;
		// intersecting
		else
			return -1;
	}

	

	public class SegmentsComparator implements Comparator<int []> {
	    

		@Override
		public int compare(int[] o1, int[] o2) {
			return (o1[0] - o2[0]);
		}
	}
	
	/**
	 * mergeAll: merge indices
	 * parameters: indices = Array of Array of indices (index = [start, end])
	 * Example: indices = [[[1,2], [4,5]], [[0,5], [7,11]]];
	 */

	//public List<int[]> mergeAll (List<List<int[]>> indices) {
	public List<int[]> mergeAll (List<int[]> m) {
		//List<int[]> m = new ArrayList<int[]>();
		
		// first of all: creates a 1-dimension array with all data
		//for(int i=0; i<indices.size(); i++) {
			//m.addAll(indices.get(i));
		//}
		
		//
		// second step: sort the array
		// for our purposes a segment is 'lower than' another segment if the left value of the segment 
		// is lower than the left value of the other segment. In other words:
		// [a, b] < [c, d] if a < c
		//
		Collections.sort(m,new SegmentsComparator());
		
		List<int[]> m2 = new ArrayList<int []>();
		
		//
		// merging procedure:
		// the procedure uses the functions: 
		// [1] 'compareSegment'.
		// when two segment are equals or included compareSegment returns 0
		// when two segment are intersecting compareSegment returns -1
		// when two segment are external (no intersection) compareSegment returns 1
		//
		// [2] 'mergeSegment'
		// returns the "union" of two segments
		//
		int[] current = m.get(0);
		for(int i=0; i<m.size(); i++) {
			
			int cfr = compareSegment(current, m.get(i));
			
			if(cfr==0) {}
			else if (cfr == -1) {
				// if segments are the same or intersected the result is the merged segment
				current = mergeSegment(current, m.get(i)).get(0);
			}
			else {
			
				// if segments are external mergeSegment produce two segments: the first is ready to be stored in the output vector
				// the second is to be compared with others
				List<int[]> s = mergeSegment(current, m.get(i));
				m2.add(s.get(0));
				current = s.get(1);
			
			}
		}
		
		if(m2.size() == 0) {
			m2.add(current);
		}
		else if((current[0] != m2.get(m2.size()-1)[0]) || (current[1] != m2.get(m2.size()-1)[1]))
			m2.add(current);
		
		return m2;
	}

		
}
