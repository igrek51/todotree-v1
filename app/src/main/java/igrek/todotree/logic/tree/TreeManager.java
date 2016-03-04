package igrek.todotree.tree;

import igrek.todotree.tree.exceptions.NoSuperItemException;

public class TreeManager {
    TreeItem rootItem;
    TreeItem currentItem;
    TreeItem editItem = null;

    public TreeManager(){
        rootItem = new TreeItem(null, "root");
        currentItem = rootItem;
    }

    //  NAWIGACJA

    public void goUp() throws NoSuperItemException {
        if(currentItem == rootItem){
            throw new NoSuperItemException();
        }else if(currentItem.getParent() == null){
            throw new IllegalStateException("parent = null. To się nie powinno zdarzyć");
        }else{
            currentItem = currentItem.getParent();
        }
    }

    public void goInto(int childIndex){
        goTo(currentItem.getChild(childIndex));
    }

    public void goTo(TreeItem child){
        currentItem = child;
    }

    public void goToRoot(){
        goTo(rootItem);
    }

    //  DODAWANIE / USUWANIE ELEMENTÓW

    public void addToCurrent(TreeItem newItem){
        currentItem.add(newItem);
    }

    public void addAndEdit(TreeItem newItem){
        addToCurrent(newItem);
        editItem(newItem);
    }

    public void addNewItemAndEdit(){
        addAndEdit(new TreeItem(currentItem));
    }

    public void deleteFromCurrent(int location){
        currentItem.remove(location);
    }

    //  EDYCJA

    public void saveItemContent(TreeItem item, String content){
        item.setContent(content);
    }

    public void saveItemContent(String content){
        if(editItem != null) {
            editItem.setContent(content);
        }
    }

    public void editItem(TreeItem item){
        //TODO włączenie ekranu edycji elementu
    }
}
