package igrek.todotree.gui.treelist;

//TODO: można usunąć, zamienić na zwykłego inta
public class ListScrollOffset {
//    int firstVisiblePosition;
//    int topview;
//    HashMap<Integer, Integer> itemHeights;

    private int absoluteScroll;

    public ListScrollOffset() {
//        this.firstVisiblePosition = 0;
//        this.topview = 0;
//        this.itemHeights = new HashMap<>();
        absoluteScroll = 0;
    }

//    public ListScrollOffset(int firstVisiblePosition, int topview, HashMap<Integer, Integer> itemHeights) {
//        this.firstVisiblePosition = firstVisiblePosition;
//        this.topview = topview;
//        this.itemHeights = itemHeights;
//    }
//
//    public void setFirstVisiblePosition(int firstVisiblePosition) {
//        this.firstVisiblePosition = firstVisiblePosition;
//    }
//
//    public void setTopview(int topview) {
//        this.topview = topview;
//    }
//
//    public void setItemHeights(HashMap<Integer, Integer> itemHeights) {
//        this.itemHeights = itemHeights;
//    }

    public void setAbsoluteScroll(int absoluteScroll){
        this.absoluteScroll = absoluteScroll;
    }

    public void copyFrom(ListScrollOffset copy) {
//        this.firstVisiblePosition = copy.firstVisiblePosition;
//        this.topview = copy.topview;
//        this.itemHeights = copy.itemHeights;

        this.absoluteScroll = copy.absoluteScroll;
    }

//    /**
//     * @param source
//     * @return różnica pozycji scrolla w pikselach, dodatnia wartość - przesunięcie w dół względem source
//     */
//    public int diff(ListScrollOffset source, TreeListView lv){
//        int diff1 = 0;
//        for(int i=this.firstVisiblePosition; i<source.firstVisiblePosition; i++){ //po przewinięciu w górę
//            diff1 -= getItemHeight(source, i, lv);
//        }
//        for(int i=source.firstVisiblePosition; i<this.firstVisiblePosition; i++){ //po przewinięciu w dół
//            diff1 += getItemHeight(source, i, lv);
//        }
//
//        return diff1 - this.topview + source.topview;
//    }

//    private int getItemHeight(ListScrollOffset source, int position, TreeListView lv){
//        if(this.itemHeights.containsKey(position)){
//            return this.itemHeights.get(position);
//        }
//        if(source.itemHeights.containsKey(position)){
//            return source.itemHeights.get(position);
//        }
//        View itemView = lv.getChildAt(position - lv.getFirstVisiblePosition());
//        if(itemView != null){
//            return itemView.getHeight();
//        }
//        Output.log("No item view height in any scroll offset map, pos: "+ position);
//        return 0;
//    }

    public int diff(ListScrollOffset source){
        return this.absoluteScroll - source.absoluteScroll;
    }
}
