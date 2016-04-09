package igrek.todotree.gui.treelist;

import java.util.HashMap;

import igrek.todotree.system.output.Output;

public class ListScrollOffset {
    int firstVisiblePosition;
    int topview;
    HashMap<Integer, Integer> itemHeights;

    public ListScrollOffset() {
        this.firstVisiblePosition = 0;
        this.topview = 0;
        this.itemHeights = new HashMap<>();
    }

    public ListScrollOffset(int firstVisiblePosition, int topview, HashMap<Integer, Integer> itemHeights) {
        this.firstVisiblePosition = firstVisiblePosition;
        this.topview = topview;
        this.itemHeights = itemHeights;
    }

    public void setFirstVisiblePosition(int firstVisiblePosition) {
        this.firstVisiblePosition = firstVisiblePosition;
    }

    public void setTopview(int topview) {
        this.topview = topview;
    }

    public void setItemHeights(HashMap<Integer, Integer> itemHeights) {
        this.itemHeights = itemHeights;
    }

    public void copyFrom(ListScrollOffset copy) {
        this.firstVisiblePosition = copy.firstVisiblePosition;
        this.topview = copy.topview;
        this.itemHeights = copy.itemHeights;
    }

    /**
     * @param source
     * @return różnica pozycji scrolla w pikselach, dodatnia wartość - przesunięcie w dół względem source
     */
    public int diff(ListScrollOffset source){
        int diff1 = 0;
        for(int i=this.firstVisiblePosition; i<source.firstVisiblePosition; i++){ //po przewinięciu w górę
            diff1 -= getItemHeight(source, i);
        }
        for(int i=source.firstVisiblePosition; i<this.firstVisiblePosition; i++){ //po przewinięciu w dół
            diff1 += getItemHeight(source, i);
        }

        return diff1 - this.topview + source.topview;
    }

    private int getItemHeight(ListScrollOffset source, int position){
        if(this.itemHeights.containsKey(position)){
            return this.itemHeights.get(position);
        }
        if(source.itemHeights.containsKey(position)){
            return source.itemHeights.get(position);
        }
        Output.log("No item view height in any scroll offset map, pos: "+ position);
        return 0;
    }
}
