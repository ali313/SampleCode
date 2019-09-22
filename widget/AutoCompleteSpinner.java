package ir.nabaksoft.office.widget;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.support.annotation.Nullable;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import ir.amp.tools.FontCache;
import ir.amp.tools.MLog;
import ir.amp.tools.utils.NumberUtils;
import ir.nabaksoft.office.R;
import ir.nabaksoft.office.model.ListModel;
import ir.nabaksoft.office.model.RolePerson;
import ir.nabaksoft.office.tools.FontUtil;

/**
 * Created by Ali on 7/9/2019.
 */
public class AutoCompleteSpinner<T> extends RowLayout implements View.OnClickListener
{
    EditText ctr_input;
    ProgressBar ctr_progress;

    PopupWindow popup = null;
    LinearLayout itemsLayout;
    AutoCompleteSpinnerAdapter adapter;
    Drawable mDeviderDrawable;
    int mDeviderHeight = 1;

    Handler handler = new Handler();
    Runnable searchRunnable = null;
    private boolean isRunning = false;
    int maxTokens = Integer.MAX_VALUE;
    int modalWidth;


    List<T> selectedItems = new ArrayList<>();

    public AutoCompleteSpinner(Context context)
    {
        this(context, null);
    }

    public AutoCompleteSpinner(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public AutoCompleteSpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr)
    {
        if (attrs != null)
        {
            //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoCompleteSpinner, defStyleAttr, 0);
            //a.recycle();
        }

        inflate(getContext(), R.layout.cmp_autocomplete_spinner, this);

        FontUtil.set(this);

        ctr_input = findViewById(R.id.autoCompleteSpinner_input);
        ctr_progress = findViewById(R.id.autoComplete_progress);

        ctr_input.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if(actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    if(!isRunning)
                    {
                        handler.removeCallbacks(searchRunnable);
                        doSearch();
                    }
                }
                return false;
            }
        });
        ctr_input.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                ctr_progress.setVisibility(View.GONE);
                String keyword = editable.toString();

                if(keyword.isEmpty()) return;
                if(keyword.length() < 2)
                {
                    return;
                }

                ctr_progress.setVisibility(View.VISIBLE);
                if(searchRunnable != null)
                {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doSearch();

                    }
                };
                handler.postDelayed(searchRunnable, 1000);
            }
        });
    }

    public AutoCompleteSpinnerAdapter getAdapter()
    {
        return adapter;
    }

    public void setAdapter(AutoCompleteSpinnerAdapter adapter)
    {
        this.adapter = adapter;
        this.adapter.setOnLoadItemsCompleted(new AutoCompleteSpinnerAdapter.LoadItemsCompleted()
        {
            @Override
            public void onLoadItemsCompleted(ListModel items)
            {
                showItems(items);
            }
        });
    }

    public void setDeviderRes(@DrawableRes int res)
    {
        mDeviderDrawable = getContext().getResources().getDrawable(res);
    }

    public void setDeviderDrawable(Drawable devider)
    {
        mDeviderDrawable = devider;
    }

    public void setDeviderColor(int color)
    {
        mDeviderDrawable = new ColorDrawable(color);
    }

    public void setDeviderHeight(int height)
    {
        mDeviderHeight = height;
    }

    public int getModalWidth()
    {
        return modalWidth == 0 ? this.getWidth() : modalWidth;
    }

    public void setModalWidth(int modalWidth)
    {
        this.modalWidth = modalWidth;
        if(popup != null)
        {
            popup.setWidth(getModalWidth());
        }
    }

    private void doSearch()
    {
        String keyword = ctr_input.getText().toString();
        if(keyword.isEmpty()) return;
        if(keyword.length() < 2)
        {
            Toast.makeText(getContext(), "لطفا حداقل دو حرف وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        if(adapter == null)
        {
            Log.e("AndroidOffice", "adapter has not been set to AutoCompleteSpinner");
            return;
        }

        adapter.cancelSearch();
        isRunning = true;
        adapter.doSearch(keyword);
    }

    private void showItems(ListModel<T> items)
    {
        ctr_progress.setVisibility(View.GONE);
        isRunning = false;
        if(popup == null)
        {
            popup = new PopupWindow(getContext());
            popup.setWidth(getModalWidth());

            ScrollView sv = new ScrollView(getContext());
            itemsLayout = new LinearLayout(getContext());
            ScrollView.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            sv.addView(itemsLayout, lp);
            itemsLayout.setOrientation(LinearLayout.VERTICAL);

            setPopupPosition();
            popup.setFocusable(true);
            popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);

            popup.setContentView(sv);
            popup.setOutsideTouchable(true);
            popup.setBackgroundDrawable(new ColorDrawable(0xffffffff));
        }

        if(items == null)
            return;

        if(!popup.isShowing())
        {
            setPopupPosition();
            popup.showAsDropDown(this);
        }

        itemsLayout.removeAllViews();
        for(int i = 0; i < items.Items.size(); i++)
        {
            T item = items.Items.get(i);
            View view = adapter.getRowView(i, item, itemsLayout);
            if(view != null)
            {
                view.setAlpha(0);
                view.setScaleX(0.8f);
                view.setScaleY(0.8f);
                itemsLayout.addView(view);
                view.animate().setDuration(200).alpha(1).scaleX(1).scaleY(1).setInterpolator(new AccelerateInterpolator()).setStartDelay(i * 150).start();

                view.setTag(R.id.Tag_AutoCompleteItem, item);
                view.setTag(R.id.Tag_AutoCompleteItemPositon, i);

                boolean exists = false;
                for(T si : selectedItems)
                {
                    if(adapter.isEqual(si, item))
                    {
                        exists = true;
                        break;
                    }
                }

                if(exists)
                {
                    view.setBackgroundColor(0xfff2f2f2);
                    view.setEnabled(false);
                }
                else
                {
                    view.setOnClickListener(this);
                }

                if(mDeviderDrawable != null && i < items.Items.size() - 1)
                {
                    View devider = new View(getContext());
                    LayoutParams dividerLP = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mDeviderHeight);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        devider.setBackground(mDeviderDrawable);
                    }
                    else
                    {
                        devider.setBackgroundDrawable(mDeviderDrawable);
                    }
                    itemsLayout.addView(devider, dividerLP);
                }
            }
        }
    }

    private void setPopupPosition()
    {
        if(popup == null) return;

//        int[] loc = new int[2];
//        this.getLocationInWindow(loc);
//        int height = getRootView().getHeight() - loc[1] - this.getHeight();
        //popup.setHeight(height);
        popup.setHeight(popup.getMaxAvailableHeight(this));
    }

    private void removeItem(int position)
    {
        if(position < 0 || position >= selectedItems.size())
            return;
        T item = selectedItems.get(position);

        selectedItems.remove(position);
        this.removeViewAt(position);

        if(selectedItems.size() < maxTokens)
        {
            ctr_input.setVisibility(View.VISIBLE);
        }
    }

    private void addItem(T item)
    {
        if(selectedItems.size() >= maxTokens)
            return;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_auto_complete, this, false);
        View removeTv = view.findViewById(R.id.item_remove);

        selectedItems.size();
        removeTv.setOnClickListener(this);

        ((LinearLayout)view).addView(adapter.getSelectedItemView(item, ((LinearLayout) view)));

        selectedItems.add(item);
        this.addView(view, getChildCount() - 1);

        if(selectedItems.size() >= maxTokens)
        {
            ctr_input.setVisibility(View.GONE);
        }
    }

    public List<T> getSelectedItems()
    {
        return selectedItems;
    }

    public int getMaxTokens()
    {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens)
    {
        this.maxTokens = maxTokens;
        if(selectedItems.size() > maxTokens)
        {
            for(int i = selectedItems.size() - 1; i >= maxTokens; i--)
            {
                selectedItems.remove(i);
                this.removeViewAt(i);
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if(id == R.id.item_remove)
        {
            View item = (View)view.getParent();
            if(item != null)
            {
                int pos = indexOfChild(item);
                if(pos >= 0)
                {
                    removeItem(pos);
                }
            }
        }
        else
        {

            Object obj = view.getTag(R.id.Tag_AutoCompleteItem);
            Object pos = view.getTag(R.id.Tag_AutoCompleteItemPositon);

            if (obj != null)
            {
                T item = (T) obj;
                addItem(item);

                if (popup != null && popup.isShowing())
                    popup.dismiss();

                ctr_input.setText("");
            }
        }
    }

    public abstract static class AutoCompleteSpinnerAdapter<T>
    {
        public interface LoadItemsCompleted<T>
        {
            void onLoadItemsCompleted(ListModel<T> items);
        }

        LoadItemsCompleted<T> onLoadItemsCompleted;

        SearchThread searchThread;

        public abstract ListModel<T> queryList(String keyword);

        public abstract View getRowView(int position, T object, ViewGroup container);

        public abstract View getSelectedItemView(T object, ViewGroup container);

        public abstract String getTitle(T item);

        public abstract boolean isEqual(T item1, T item2);

        public void setOnLoadItemsCompleted(LoadItemsCompleted<T> listener)
        {
            this.onLoadItemsCompleted = listener;
        }

        protected final void doSearch(String keyword)
        {
            searchThread = new SearchThread();
            searchThread.execute(keyword);
        }

        protected void cancelSearch()
        {
            if(searchThread != null && searchThread.getStatus() == AsyncTask.Status.RUNNING)
            {
                searchThread.cancel(true);
                searchThread = null;
            }
        }


        public class SearchThread extends AsyncTask<String, Void, ListModel<T>>
        {

            @Override
            protected ListModel<T> doInBackground(String... strings)
            {
                return queryList(strings[0]);
            }

            @Override
            protected void onPostExecute(ListModel<T> items)
            {
                if(onLoadItemsCompleted != null)
                    onLoadItemsCompleted.onLoadItemsCompleted(items);
            }
        }


    }
}
