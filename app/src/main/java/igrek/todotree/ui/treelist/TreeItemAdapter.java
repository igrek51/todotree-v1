package igrek.todotree.ui.treelist;

import android.content.Context;
import android.graphics.Rect;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import igrek.todotree.R;
import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.domain.treeitem.LinkTreeItem;
import igrek.todotree.intent.ItemEditorCommand;
import igrek.todotree.intent.ItemSelectionCommand;
import igrek.todotree.ui.errorcheck.SafeClickListener;

class TreeItemAdapter extends ArrayAdapter<AbstractTreeItem> {
	
	private List<AbstractTreeItem> dataSource;
	private Set<Integer> selections = null; // selected indexes
	private TreeListView listView;
	private SparseArray<View> storedViews;
	private LayoutInflater inflater;
	
	TreeItemAdapter(Context context, List<AbstractTreeItem> dataSource, TreeListView listView) {
		super(context, 0, new ArrayList<>());
		if (dataSource == null)
			dataSource = new ArrayList<>();
		this.dataSource = dataSource;
		this.listView = listView;
		storedViews = new SparseArray<>();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	void setDataSource(List<AbstractTreeItem> dataSource) {
		this.dataSource = dataSource;
		storedViews.clear();
		notifyDataSetChanged();
	}
	
	public AbstractTreeItem getItem(int position) {
		return dataSource.get(position);
	}
	
	public List<AbstractTreeItem> getItems() {
		return dataSource;
	}
	
	void setSelections(Set<Integer> selections) {
		this.selections = selections;
	}
	
	View getStoredView(int position) {
		if (position >= dataSource.size())
			return null;
		return storedViews.get(position);
	}
	
	@Override
	public int getCount() {
		return dataSource.size() + 1;
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public long getItemId(int position) {
		if (position < 0)
			return -1;
		if (position >= dataSource.size())
			return -1;
		return (long) position;
	}
	
	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		if (position == dataSource.size()) {
			return getAddItemView(position, parent);
		} else {
			return getItemView(position, parent);
		}
	}
	
	@NonNull
	private View getItemView(int position, @NonNull ViewGroup parent) {
		// get from cache
		if (storedViews.get(position) != null)
			return storedViews.get(position);
		
		final AbstractTreeItem item = dataSource.get(position);
		View itemView;
		if (!item.isEmpty()) {
			itemView = getParentItemView(item, position, parent);
		} else {
			itemView = getSingleItemView(item, position, parent);
		}
		
		// store view
		storedViews.put(position, itemView);
		
		return itemView;
	}
	
	@NonNull
	private View getSingleItemView(AbstractTreeItem item, int position, @NonNull ViewGroup parent) {
		final View itemView = inflater.inflate(R.layout.tree_item_single, parent, false);
		
		//zawartość tekstowa elementu
		TextView textView = itemView.findViewById(R.id.tvItemContent);
		textView.setText(item.getDisplayName());
		
		// link
		if (item instanceof LinkTreeItem) {
			SpannableString content = new SpannableString(item.getDisplayName());
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			textView.setText(content);
		}
		
		//przesuwanie
		final ImageButton moveButton = itemView.findViewById(R.id.buttonItemMove);
		moveButton.setFocusableInTouchMode(false);
		moveButton.setFocusable(false);
		moveButton.setClickable(false);
		increaseTouchArea(moveButton, 20);
		if (selections == null) {
			moveButton.setOnTouchListener((v, event) -> {
				event.setSource(777); // from moveButton
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						listView.getReorder()
								.onItemMoveButtonPressed(position, item, itemView, event.getX(), event
										.getY() + moveButton.getTop());
						return false;
					case MotionEvent.ACTION_MOVE:
						return false;
					case MotionEvent.ACTION_UP:
						listView.getReorder()
								.onItemMoveButtonReleased(position, item, itemView, event.getX(), event
										.getY() + moveButton.getTop());
						return true;
				}
				return false;
			});
		} else {
			moveButton.setVisibility(View.INVISIBLE);
			moveButton.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
		}
		
		//checkbox do zaznaczania wielu elementów
		CheckBox cbItemSelected = itemView.findViewById(R.id.cbItemSelected);
		cbItemSelected.setFocusableInTouchMode(false);
		cbItemSelected.setFocusable(false);
		
		if (selections != null) {
			cbItemSelected.setVisibility(View.VISIBLE);
			if (selections.contains(position)) {
				cbItemSelected.setChecked(true);
			} else {
				cbItemSelected.setChecked(false);
			}
			cbItemSelected.setOnCheckedChangeListener((buttonView, isChecked) -> new ItemSelectionCommand()
					.selectedItemClicked(position, isChecked));
		}
		
		itemView.setOnTouchListener(new TreeItemTouchListener(listView, position));
		
		return itemView;
	}
	
	@NonNull
	private View getParentItemView(AbstractTreeItem item, int position, @NonNull ViewGroup parent) {
		final View itemView = inflater.inflate(R.layout.tree_item_parent, parent, false);
		
		//zawartość tekstowa elementu
		TextView textView = itemView.findViewById(R.id.tvItemContent);
		textView.setText(item.getDisplayName());
		
		// ilość potomków
		TextView tvItemChildSize = itemView.findViewById(R.id.tvItemChildSize);
		String contentBuilder = "[" + item.size() + "]";
		tvItemChildSize.setText(contentBuilder);
		
		//edycja elementu
		ImageButton editButton = itemView.findViewById(R.id.buttonItemEdit);
		editButton.setFocusableInTouchMode(false);
		editButton.setFocusable(false);
		editButton.setClickable(true);
		increaseTouchArea(editButton, 20);
		if (selections == null && !item.isEmpty()) {
			editButton.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					new ItemEditorCommand().itemEditClicked(item);
				}
			});
		} else {
			editButton.setVisibility(View.GONE);
		}
		
		//przesuwanie
		final ImageButton moveButton = itemView.findViewById(R.id.buttonItemMove);
		moveButton.setFocusableInTouchMode(false);
		moveButton.setFocusable(false);
		moveButton.setClickable(false);
		increaseTouchArea(moveButton, 20);
		if (selections == null) {
			moveButton.setOnTouchListener((v, event) -> {
				event.setSource(777); // from moveButton
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						listView.getReorder()
								.onItemMoveButtonPressed(position, item, itemView, event.getX(), event
										.getY() + moveButton.getTop());
						return false;
					case MotionEvent.ACTION_MOVE:
						return false;
					case MotionEvent.ACTION_UP:
						listView.getReorder()
								.onItemMoveButtonReleased(position, item, itemView, event.getX(), event
										.getY() + moveButton.getTop());
						return true;
				}
				return false;
			});
		} else {
			moveButton.setVisibility(View.INVISIBLE);
			moveButton.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
		}
		
		//checkbox do zaznaczania wielu elementów
		CheckBox cbItemSelected = itemView.findViewById(R.id.cbItemSelected);
		cbItemSelected.setFocusableInTouchMode(false);
		cbItemSelected.setFocusable(false);
		
		if (selections != null) {
			cbItemSelected.setVisibility(View.VISIBLE);
			if (selections.contains(position)) {
				cbItemSelected.setChecked(true);
			} else {
				cbItemSelected.setChecked(false);
			}
			cbItemSelected.setOnCheckedChangeListener((buttonView, isChecked) -> new ItemSelectionCommand()
					.selectedItemClicked(position, isChecked));
		}
		
		itemView.setOnTouchListener(new TreeItemTouchListener(listView, position));
		
		return itemView;
	}
	
	@NonNull
	private View getAddItemView(int position, @NonNull ViewGroup parent) {
		// plus
		View itemPlus = inflater.inflate(R.layout.item_plus, parent, false);
		
		ImageButton plusButton = itemPlus.findViewById(R.id.buttonAddNewItem);
		plusButton.setFocusableInTouchMode(false);
		plusButton.setFocusable(false);
		plusButton.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				new ItemEditorCommand().addItemClicked();
			}
		});
		// redirect long click to tree list view
		plusButton.setLongClickable(true);
		plusButton.setOnLongClickListener(v -> listView.onItemLongClick(null, null, position, 0));
		
		return itemPlus;
	}
	
	private void increaseTouchArea(View component, int sidePx) {
		final View parent = (View) component.getParent();  // button: the view you want to enlarge hit area
		parent.post(() -> {
			final Rect rect = new Rect();
			component.getHitRect(rect);
			rect.top -= sidePx;    // increase top hit area
			rect.left -= sidePx;   // increase left hit area
			rect.bottom += sidePx; // increase bottom hit area
			rect.right += sidePx;  // increase right hit area
			parent.setTouchDelegate(new TouchDelegate(rect, component));
		});
	}
}