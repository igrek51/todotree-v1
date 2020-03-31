package igrek.todotree.ui.treelist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import java.util.List;

import igrek.todotree.domain.treeitem.AbstractTreeItem;
import igrek.todotree.info.logger.Logger;
import igrek.todotree.info.logger.LoggerFactory;
import igrek.todotree.intent.TreeCommand;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class TreeListReorder {
	
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	private TreeListView listView;
	
	/** początkowa pozycja kursora przy rozpoczęciu przeciągania (ulega zmianie przy zamianie elementów) */
	private float startTouchY;
	/** aktualna pozycja kursora względem ekranu i listview (bez scrollowania) */
	private float lastTouchY;
	/** położenie scrolla przy rozpoczęciu przeciągania */
	private Integer scrollStart = 0;
	
	/** pozycja aktualnie przeciąganego elementu */
	private Integer draggedItemPos = null;
	/** widok aktualnie przeciąganego elementu */
	private View draggedItemView = null;
	/** oryginalna górna pozycja niewidocznego elementu na liście, którego bitmapa jest przeciągana */
	private Integer draggedItemViewTop = null;
	
	private BitmapDrawable hoverBitmap = null;
	private BitmapDrawable hoverBitmapAnimation = null;
	private Rect hoverBitmapBounds;
	
	private final float ITEMS_REPLACE_COVER = 0.65f;
	
	private final int HOVER_BORDER_THICKNESS = 5;
	private final int HOVER_BORDER_COLOR = 0xccb0b0b0;
	
	public TreeListReorder(TreeListView listView) {
		this.listView = listView;
	}
	
	private void updateHoverBitmap() {
		if (draggedItemViewTop != null && draggedItemPos != null) {
			float dy = lastTouchY - startTouchY;
			hoverBitmapBounds.offsetTo(0, draggedItemViewTop + (int) dy);
			hoverBitmap.setBounds(hoverBitmapBounds);
		}
	}
	
	private BitmapDrawable getAndAddHoverView(View v) {
		int top = v.getTop();
		int left = v.getLeft();
		
		Bitmap b = getBitmapWithBorder(v);
		BitmapDrawable drawable = new BitmapDrawable(listView.getResources(), b);
		
		hoverBitmapBounds = new Rect(left, top, left + v.getWidth(), top + v.getHeight());
		drawable.setBounds(hoverBitmapBounds);
		
		return drawable;
	}
	
	private Bitmap getBitmapWithBorder(View v) {
		Bitmap bitmap = getBitmapFromView(v);
		Canvas can = new Canvas(bitmap);
		
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(HOVER_BORDER_THICKNESS);
		paint.setColor(HOVER_BORDER_COLOR);
		
		can.drawBitmap(bitmap, 0, 0, null);
		can.drawRect(rect, paint);
		
		return bitmap;
	}
	
	private Bitmap getBitmapFromView(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		return bitmap;
	}
	
	private final static TypeEvaluator<Rect> rectBoundsEvaluator = new TypeEvaluator<Rect>() {
		public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
			return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(startValue.bottom, endValue.bottom, fraction));
		}
		
		int interpolate(int start, int end, float fraction) {
			return (int) (start + fraction * (end - start));
		}
	};
	
	
	private void itemDraggingStarted(int position, View itemView) {
		draggedItemPos = position;
		draggedItemView = itemView;
		draggedItemViewTop = itemView.getTop();
		scrollStart = listView.getScrollHandler().getScrollOffset();
		
		hoverBitmap = getAndAddHoverView(draggedItemView);
		draggedItemView.setVisibility(INVISIBLE);
		
		listView.invalidate();
	}
	
	public void setLastTouchY(float lastTouchY) {
		this.lastTouchY = lastTouchY;
	}
	
	public void handleItemDragging() {
		
		float dyTotal = lastTouchY - startTouchY + (listView.getScrollHandler()
				.getScrollOffset() - scrollStart);
		
		if (draggedItemViewTop == null) {
			logger.error("draggedItemViewTop = null");
			return;
		}
		if (draggedItemPos == null) {
			logger.error("draggedItemPos = null");
			return;
		}
		
		int step = 0;
		int deltaH = 0;
		
		if (dyTotal > 0) { //przesuwanie w dół
			while (true) {
				if (draggedItemPos + step + 1 >= listView.getItems().size())
					break;
				int downHeight = listView.getItemHeight(draggedItemPos + step + 1);
				if (downHeight == 0)
					break;
				if (dyTotal - deltaH > downHeight * ITEMS_REPLACE_COVER) {
					step++;
					deltaH += downHeight;
				} else {
					break;
				}
			}
		} else if (dyTotal < 0) { //przesuwanie w górę
			while (true) {
				if (draggedItemPos + step - 1 < 0)
					break;
				int upHeight = listView.getItemHeight(draggedItemPos + step - 1);
				if (upHeight == 0)
					break;
				if (-dyTotal + deltaH > upHeight * ITEMS_REPLACE_COVER) {
					step--;
					deltaH -= upHeight;
				} else {
					break;
				}
			}
		}
		
		if (step != 0) {
			int targetPosition = draggedItemPos + step;
			
			List<AbstractTreeItem> items = new TreeCommand().itemMoved(draggedItemPos, step);
			
			//update mapy wysokości
			if (step > 0) {
				Integer draggedItemHeight = listView.getItemHeight(draggedItemPos);
				for (int i = draggedItemPos; i < targetPosition; i++) {
					Integer nextHeight = listView.getItemHeight(i + 1);
					listView.putItemHeight(i, nextHeight);
				}
				listView.putItemHeight(targetPosition, draggedItemHeight);
			} else if (step < 0) {
				Integer draggedItemHeight = listView.getItemHeight(draggedItemPos);
				for (int i = draggedItemPos; i > targetPosition; i--) {
					Integer nextHeight = listView.getItemHeight(i - 1);
					listView.putItemHeight(i, nextHeight);
				}
				listView.putItemHeight(targetPosition, draggedItemHeight);
			}
			
			listView.getAdapter().setDataSource(items);
			
			startTouchY += deltaH;
			draggedItemViewTop += deltaH;
			
			draggedItemPos = targetPosition;
			if (draggedItemView != null) {
				draggedItemView.setVisibility(VISIBLE);
			}
			draggedItemView = listView.getItemView(draggedItemPos);
			if (draggedItemView != null) {
				draggedItemView.setVisibility(INVISIBLE);
			}
		}
		
		listView.getScrollHandler().handleScrolling();
		
		listView.invalidate();
	}
	
	public void itemDraggingStopped() {
		if (draggedItemPos != null && draggedItemViewTop != null) {
			//wyłączenie automatycznego ustawiania pozycji hover bitmapy
			draggedItemPos = null;
			
			//animacja powrotu do aktualnego połozenia elementu
			// kopia referencji na czas trwania animacji
			hoverBitmapAnimation = hoverBitmap;
			Integer scrollOffset = listView.getScrollHandler().getScrollOffset();
			hoverBitmapBounds.offsetTo(0, draggedItemViewTop - (scrollOffset - scrollStart));
			final Rect hoverBitmapBoundsCopy = new Rect(hoverBitmapBounds);
			final View draggedItemViewCopy = draggedItemView;
			
			draggedItemView = null;
			hoverBitmap = null;
			draggedItemViewTop = null;
			
			ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverBitmapAnimation, "bounds", rectBoundsEvaluator, hoverBitmapBoundsCopy);
			hoverViewAnimator.addUpdateListener(valueAnimator -> listView.invalidate());
			hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					listView.setEnabled(false);
				}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					if (draggedItemViewCopy != null) {
						draggedItemViewCopy.setVisibility(VISIBLE);
					}
					hoverBitmapAnimation = null;
					listView.setEnabled(true);
					listView.invalidate();
				}
			});
			hoverViewAnimator.start();
		} else {
			draggedItemPos = null;
			draggedItemView = null;
			draggedItemViewTop = null;
		}
	}
	
	public void onItemMoveButtonPressed(int position, AbstractTreeItem item, View itemView, float touchX, float touchY) {
		startTouchY = itemView.getTop() + touchY;
		lastTouchY = startTouchY;
		itemDraggingStarted(position, itemView);
	}
	
	public void onItemMoveButtonReleased(int position, AbstractTreeItem item, View itemView, float touchX, float touchY) {
		itemDraggingStopped();
	}
	
	public boolean isDragging() {
		return draggedItemPos != null;
	}
	
	public void setDraggedItemView() {
		View draggedItemViewOld = draggedItemView;
		draggedItemView = listView.getItemView(draggedItemPos);
		if (draggedItemView != null) {
			draggedItemView.setVisibility(View.INVISIBLE);
		}
		if (draggedItemViewOld != draggedItemView && draggedItemViewOld != null) {
			draggedItemViewOld.setVisibility(View.VISIBLE);
		}
	}
	
	public void dispatchDraw(Canvas canvas) {
		if (hoverBitmap != null) {
			updateHoverBitmap();
			hoverBitmap.draw(canvas);
		}
		if (hoverBitmapAnimation != null) {
			hoverBitmapAnimation.draw(canvas);
		}
	}
	
	public Rect getHoverBitmapBounds() {
		return hoverBitmapBounds;
	}
}
