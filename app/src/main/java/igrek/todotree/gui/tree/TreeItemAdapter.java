package igrek.todotree.gui.tree;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import igrek.todotree.R;
import igrek.todotree.controller.ItemEditorController;
import igrek.todotree.controller.ItemSelectionController;
import igrek.todotree.controller.ItemTrashController;
import igrek.todotree.controller.TreeController;
import igrek.todotree.datatree.item.TreeItem;
import igrek.todotree.gui.errorhandling.SafeClickListener;

class TreeItemAdapter extends ArrayAdapter<TreeItem> {
	
	private Context context;
	private List<TreeItem> dataSource;
	private List<Integer> selections = null;
	private TreeListView listView;
	
	private HashMap<Integer, View> storedViews;
	
	TreeItemAdapter(Context context, List<TreeItem> dataSource, TreeListView listView) {
		super(context, 0, new ArrayList<TreeItem>());
		this.context = context;
		if (dataSource == null)
			dataSource = new ArrayList<>();
		this.dataSource = dataSource;
		this.listView = listView;
		storedViews = new HashMap<>();
	}
	
	void setDataSource(List<TreeItem> dataSource) {
		this.dataSource = dataSource;
		storedViews = new HashMap<>();
		notifyDataSetChanged();
	}
	
	public TreeItem getItem(int position) {
		return dataSource.get(position);
	}
	
	void setSelections(List<Integer> selections) {
		this.selections = selections;
	}
	
	View getStoredView(int position) {
		if (position >= dataSource.size())
			return null;
		if (!storedViews.containsKey(position))
			return null;
		return storedViews.get(position);
	}
	
	@Override
	public int getCount() {
		return dataSource.size() + 1;
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
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//TODO wększy padding przycisków edycji i usuwania, zmaksymalizowanie obszaru aktywnego przycisków (edycji, przesuwania)
		
		if (position == dataSource.size()) {
			//plusik
			View itemPlus = inflater.inflate(R.layout.item_plus, parent, false);
			
			ImageButton plusButton = (ImageButton) itemPlus.findViewById(R.id.buttonAddNewItem);
			plusButton.setFocusableInTouchMode(false);
			plusButton.setFocusable(false);
			plusButton.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					new ItemEditorController().addItemClicked();
				}
			});
			
			return itemPlus;
		} else {
			final View itemView = inflater.inflate(R.layout.tree_item, parent, false);
			final TreeItem item = dataSource.get(position);
			
			//zawartość tekstowa elementu
			TextView textView = (TextView) itemView.findViewById(R.id.tvItemContent);
			if (!item.isEmpty()) {
				textView.setTypeface(null, Typeface.BOLD);
			} else {
				textView.setTypeface(null, Typeface.NORMAL);
			}
			textView.setText(item.getContent());
			
			// ilość potomków
			TextView tvItemChildSize = (TextView) itemView.findViewById(R.id.tvItemChildSize);
			if (!item.isEmpty()) {
				String contentBuilder = "[" + item.size() + "]";
				tvItemChildSize.setText(contentBuilder);
			} else {
				tvItemChildSize.setText("");
			}
			
			//edycja elementu
			ImageButton editButton = (ImageButton) itemView.findViewById(R.id.buttonItemEdit);
			editButton.setFocusableInTouchMode(false);
			editButton.setFocusable(false);
			if (selections == null && !item.isEmpty()) {
				editButton.setOnClickListener(new SafeClickListener() {
					@Override
					public void onClick() {
						new ItemEditorController().itemEditClicked(item);
					}
				});
			} else {
				editButton.setVisibility(View.GONE);
			}
			
			//wejście w element
			ImageButton goIntoButton = (ImageButton) itemView.findViewById(R.id.buttonItemGoInto);
			goIntoButton.setFocusableInTouchMode(false);
			goIntoButton.setFocusable(false);
			if (selections == null && item.isEmpty()) {
				goIntoButton.setOnClickListener(new SafeClickListener() {
					@Override
					public void onClick() {
						new TreeController().itemGoIntoClicked(position);
					}
				});
			} else {
				goIntoButton.setVisibility(View.GONE);
			}
			// ukryte wejście wgłąb elementu - blokada
			goIntoButton.setVisibility(View.GONE);
			
			//usuwanie elementu
			ImageButton removeButton = (ImageButton) itemView.findViewById(R.id.buttonItemRemove);
			removeButton.setFocusableInTouchMode(false);
			removeButton.setFocusable(false);
			removeButton.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					new ItemTrashController().itemRemoveClicked(position);
				}
			});
			
			//przesuwanie
			final ImageButton moveButton = (ImageButton) itemView.findViewById(R.id.buttonItemMove);
			moveButton.setFocusableInTouchMode(false);
			moveButton.setFocusable(false);
			if (selections == null) {
				moveButton.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
							case MotionEvent.ACTION_DOWN:
								listView.onItemMoveButtonPressed(position, item, itemView, event.getX(), event
										.getY() + moveButton.getTop());
								return false;
							case MotionEvent.ACTION_UP:
								listView.onItemMoveButtonReleased(position, item, itemView, event.getX(), event
										.getY() + moveButton.getTop());
								return true;
						}
						return false;
					}
				});
				moveButton.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						return listView.onItemMoveLongPressed(position, item);
					}
				});
			} else {
				moveButton.setVisibility(View.INVISIBLE);
				moveButton.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
			}
			
			//dodawanie nowego elementu
			ImageButton addButton = (ImageButton) itemView.findViewById(R.id.buttonItemAddHere);
			addButton.setFocusableInTouchMode(false);
			addButton.setFocusable(false);
			if (selections == null) {
				addButton.setOnClickListener(new SafeClickListener() {
					@Override
					public void onClick() {
						new ItemEditorController().addItemHereClicked(position);
					}
				});
			} else {
				addButton.setVisibility(View.GONE);
			}
			
			//checkbox do zaznaczania wielu elementów
			CheckBox cbItemSelected = (CheckBox) itemView.findViewById(R.id.cbItemSelected);
			cbItemSelected.setFocusableInTouchMode(false);
			cbItemSelected.setFocusable(false);
			
			if (selections == null) {
				cbItemSelected.setVisibility(View.GONE);
			} else {
				cbItemSelected.setVisibility(View.VISIBLE);
				if (selections.contains(position)) {
					cbItemSelected.setChecked(true);
				} else {
					cbItemSelected.setChecked(false);
				}
				cbItemSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						new ItemSelectionController().selectedItemClicked(position, isChecked);
					}
				});
			}
			
			//zachowanie widoku
			storedViews.put(position, itemView);
			
			itemView.setOnTouchListener(new TreeItemTouchListener(listView, position));
			
			return itemView;
		}
	}
	
}