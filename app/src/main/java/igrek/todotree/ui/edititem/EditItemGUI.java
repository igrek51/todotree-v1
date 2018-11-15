package igrek.todotree.ui.edititem;

import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import igrek.todotree.R;
import igrek.todotree.commands.ItemEditorCommand;
import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.ui.GUI;
import igrek.todotree.ui.errorcheck.SafeClickListener;
import igrek.todotree.ui.numkeyboard.NumKeyboardListener;
import igrek.todotree.ui.numkeyboard.NumericKeyboardView;

public class EditItemGUI implements NumKeyboardListener {
	
	private GUI gui;
	
	private ItemEditText etEditItem;
	private Button buttonSaveItem;
	private NumericKeyboardView numericKeyboard;
	
	public EditItemGUI(GUI gui, final AbstractTreeItem item, AbstractTreeItem parent) {
		this.gui = gui;
		
		init(item, parent);
	}
	
	private void init(final AbstractTreeItem item, AbstractTreeItem parent) {
		View editItemContentLayout = gui.setMainContentLayout(R.layout.edit_item_content);
		
		etEditItem = (ItemEditText) editItemContentLayout.findViewById(R.id.etEditItemContent);
		//przycisk zapisu
		buttonSaveItem = (Button) editItemContentLayout.findViewById(R.id.buttonSaveItem);
		//przycisk zapisz i dodaj nowy
		Button buttonSaveAndAdd = (Button) editItemContentLayout.findViewById(R.id.buttonSaveAndAddItem);
		//przycisk zapisz i wejdź
		Button buttonSaveAndGoInto = (Button) editItemContentLayout.findViewById(R.id.buttonSaveAndGoInto);
		//przycisk obrotu ekranu
		final ImageButton rotateScreenBtn = (ImageButton) editItemContentLayout.findViewById(R.id.rotateScreenBtn);
		
		gui.setTitle(parent.getDisplayName());
		
		if (item != null) { //edycja
			etEditItem.setText(item.getDisplayName());
			buttonSaveItem.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					if (numericKeyboard.isVisible()) {
						numericKeyboard.finishTyping();
					}
					hideKeyboards();
					new ItemEditorCommand().saveItem(item, etEditItem.getText().toString());
				}
			});
			buttonSaveAndAdd.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					new ItemEditorCommand().saveAndAddItemClicked(item, etEditItem.getText()
							.toString());
					hideKeyboards();
				}
			});
			buttonSaveAndGoInto.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					new ItemEditorCommand().saveAndGoIntoItemClicked(item, etEditItem.getText()
							.toString());
					hideKeyboards();
				}
			});
		} else { //nowy element
			etEditItem.setText("");
			buttonSaveItem.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					if (numericKeyboard.isVisible()) {
						numericKeyboard.finishTyping();
					}
					hideKeyboards();
					new ItemEditorCommand().saveItem(null, etEditItem.getText().toString());
				}
			});
			buttonSaveAndAdd.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					new ItemEditorCommand().saveAndAddItemClicked(null, etEditItem.getText()
							.toString());
					hideKeyboards();
				}
			});
			buttonSaveAndGoInto.setOnClickListener(new SafeClickListener() {
				@Override
				public void onClick() {
					new ItemEditorCommand().saveAndGoIntoItemClicked(null, etEditItem.getText()
							.toString());
					hideKeyboards();
				}
			});
		}
		
		rotateScreenBtn.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				gui.rotateScreen();
			}
		});
		
		//przycisk anuluj
		Button buttonEditCancel = (Button) editItemContentLayout.findViewById(R.id.buttonEditCancel);
		buttonEditCancel.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				new ItemEditorCommand().cancelEditedItem();
				hideKeyboards();
			}
		});
		
		//przyciski zmiany kursora i zaznaczenia
		ImageButton quickEditGoBegin = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoBegin);
		quickEditGoBegin.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				quickCursorMove(-2);
			}
		});
		ImageButton quickEditGoLeft = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoLeft);
		quickEditGoLeft.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				quickCursorMove(-1);
			}
		});
		ImageButton quickEditSelectAll = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditSelectAll);
		quickEditSelectAll.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				quickEditSelectAll();
			}
		});
		ImageButton quickEditGoRight = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoRight);
		quickEditGoRight.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				quickCursorMove(+1);
			}
		});
		ImageButton quickEditGoEnd = (ImageButton) editItemContentLayout.findViewById(R.id.quickEditGoEnd);
		quickEditGoEnd.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				quickCursorMove(+2);
			}
		});
		
		//klawiatura dodawania godziny
		Button buttonEditInsertTime = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertTime);
		buttonEditInsertTime.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				toggleTypingHour();
			}
		});
		
		//klawiatura dodawania daty
		Button buttonEditInsertDate = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertDate);
		buttonEditInsertDate.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				toggleTypingDate();
			}
		});
		
		//numer
		Button buttonEditInsertNumber = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertNumber);
		buttonEditInsertNumber.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				toggleTypingNumeric();
			}
		});
		
		//dodawanie przedziału
		Button buttonEditInsertRange = (Button) editItemContentLayout.findViewById(R.id.buttonEditInsertRange);
		buttonEditInsertRange.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				quickInsertRange();
				numericKeyboard.resetInput();
			}
		});
		
		etEditItem.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					//zapis przyciskiem OK
					buttonSaveItem.performClick();
					return true;
				}
				return false;
			}
		});
		
		etEditItem.setNumKeyboardListener(this);
		
		//niepotrzebne na focus: etEditItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		etEditItem.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				if (numericKeyboard.isVisible()) {
					showNumericKeyboard();
					numericKeyboard.resetInput();
				} else {
					showAlphanumKeyboard();
				}
			}
		});
		
		numericKeyboard = (NumericKeyboardView) editItemContentLayout.findViewById(R.id.numericKeyboard);
		numericKeyboard.init(this, etEditItem);
		
		//focus na końcu edytowanego tekstu
		etEditItem.requestFocus();
		quickCursorMove(+2);
		
		// delayed execution due to not showing keyboard after action select
		forceKeyboardShow();
	}
	
	public void forceKeyboardShow() {
		new Handler().post(() -> showAlphanumKeyboard());
	}
	
	
	private void quickCursorMove(int direction) {
		if (direction == -2) { //na początek
			
			// WTF ??? - zjebane, ale tak trzeba, by ujarzmić ten przeskakujący kursor
			etEditItem.requestFocus();
			etEditItem.selectAll();
			etEditItem.setSelection(0);
			
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					etEditItem.setSelection(0);
				}
			});
			
		} else if (direction == +2) { //na koniec
			etEditItem.setSelection(etEditItem.getText().length());
		} else if (direction == -1 || direction == +1) {
			int selStart = etEditItem.getSelectionStart();
			int selEnd = etEditItem.getSelectionEnd();
			if (selStart == selEnd) { //brak zaznaczenia
				selStart += direction;
				if (selStart < 0)
					selStart = 0;
				if (selStart > etEditItem.getText().length())
					selStart = etEditItem.getText().length();
				etEditItem.setSelection(selStart);
			} else { //zaznaczenie wielu znaków
				//poszerzenie zaznaczenia
				if (direction == -1) { //w lewo
					selStart--;
					if (selStart < 0)
						selStart = 0;
				} else { //w prawo
					selEnd++;
					if (selEnd > etEditItem.getText().length())
						selEnd = etEditItem.getText().length();
				}
				etEditItem.setSelection(selStart, selEnd);
			}
		}
	}
	
	private void quickEditSelectAll() {
		etEditItem.requestFocus();
		etEditItem.selectAll();
		// konieczne, żeby zaznaczyć cały tekst, a nie tylko przejść z kursorem na początek - WTF ???
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				etEditItem.selectAll();
			}
		});
	}
	
	
	public void quickInsertRange() {
		if (numericKeyboard.isVisible()) {
			numericKeyboard.finishTyping();
		}
		String edited = etEditItem.getText().toString();
		int selStart = etEditItem.getSelectionStart();
		int selEnd = etEditItem.getSelectionEnd();
		String before = edited.substring(0, selStart);
		String after = edited.substring(selEnd);
		//bez podwójnej spacji przed "-"
		if (before.length() > 0 && before.charAt(before.length() - 1) == ' ') {
			edited = before + "- " + after;
			selStart += 2;
		} else {
			edited = before + " - " + after;
			selStart += 3;
		}
		etEditItem.setText(edited);
		etEditItem.setSelection(selStart, selStart);
	}
	
	private void showAlphanumKeyboard() {
		int selEnd = etEditItem.getSelectionEnd();
		int selStart = etEditItem.getSelectionStart();
		
		numericKeyboard.setVisible(false);
		gui.showSoftKeyboard(etEditItem);
		
		etEditItem.setInputType(InputType.TYPE_CLASS_TEXT);
		etEditItem.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);
		
		etEditItem.setSelection(selStart, selEnd);
	}
	
	private void showNumericKeyboard() {
		int selEnd = etEditItem.getSelectionEnd();
		int selStart = etEditItem.getSelectionStart();
		
		gui.hideSoftKeyboard(etEditItem);
		numericKeyboard.setVisible(true);
		
		//        etEditItem.setInputType(InputType.TYPE_NULL);
		
		etEditItem.setSelection(selStart, selEnd);
	}
	
	public void hideKeyboards() {
		int selEnd = etEditItem.getSelectionEnd();
		int selStart = etEditItem.getSelectionStart();
		
		gui.hideSoftKeyboard(etEditItem);
		numericKeyboard.setVisible(false);
		
		etEditItem.setInputType(InputType.TYPE_CLASS_TEXT);
		etEditItem.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE);
		
		etEditItem.setSelection(selStart, selEnd);
	}
	
	private void toggleTypingHour() {
		toggleTyping(1);
	}
	
	private void toggleTypingDate() {
		toggleTyping(2);
	}
	
	private void toggleTypingNumeric() {
		toggleTyping(3);
	}
	
	private void toggleTyping(int mode) {
		if (numericKeyboard.isVisible() && numericKeyboard.getTypingMode() == mode) {
			showAlphanumKeyboard();
		} else {
			showNumericKeyboard();
			numericKeyboard.startTyping(mode);
		}
	}
	
	@Override
	public void onNumKeyboardClosed() {
		showAlphanumKeyboard();
	}
	
	@Override
	public void onSelectionChanged(int selStart, int selEnd) {
		
	}
	
	public boolean editItemBackClicked() {
		hideKeyboards();
		return numericKeyboard.isVisible(); //przechwycenie wciśnięcia przycisku
	}
	
	public void requestSaveEditedItem() {
		buttonSaveItem.performClick();
	}
}
