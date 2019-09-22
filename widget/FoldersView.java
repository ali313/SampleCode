package ir.nabaksoft.office.widget;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.support.annotation.Nullable;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.List;

import ir.amp.tools.MLog;
import ir.amp.tools.utils.ScreenUtils;
import ir.nabaksoft.office.R;
import ir.nabaksoft.office.api.ApiUtils;
import ir.nabaksoft.office.api.Constants;
import ir.nabaksoft.office.model.Folder;
import ir.nabaksoft.office.model.ListModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ali on 7/4/2019.
 */
public class FoldersView extends LinearLayout implements View.OnClickListener
{
    public List<Folder> getFolders()
    {
        return folders;
    }

    public interface FolderViewCallback
    {
        void onFolderSelectionChanged(Folder selectedFolder);
        void onLoadCompleted(List<Folder> folders);
    }

    FolderViewCallback folderSelectedListener = null;

    List<Folder> folders;
    Folder selectedFolder = null;
    PopupWindow popup = null;

    TextView tv;
    ProgressBar progressBar;
    ImageView arrowIv;

    public FoldersView(Context context)
    {
        this(context, null);
    }

    public FoldersView(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public FoldersView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr)
    {
        if (attrs != null)
        {
            //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FoldersView, defStyleAttr, 0);
            //a.recycle();
        }

        inflate(getContext(), R.layout.cmp_folders, this);
        setOrientation(LinearLayout.HORIZONTAL);

        tv = findViewById(R.id.folders_dropText);
        progressBar = findViewById(R.id.folders_dropProgress);
        arrowIv = findViewById(R.id.folders_dropArrow);

        setLoading(true);
    }

    public void setOnFolderSelectedListener(FolderViewCallback listener)
    {
        this.folderSelectedListener = listener;
    }

    public FolderViewCallback getOnFolderSelectedListener()
    {
        return this.folderSelectedListener;
    }

    public void loadFolders(@Constants.LetterState int letterStateType, @Constants.LetterType int letterListType, boolean isDraft)
    {
        setFolders(null);
        setLoading(true);

        ApiUtils.getApi(getContext()).getFolders(letterStateType, letterListType, isDraft)
                .enqueue(new Callback<ListModel<Folder>>()
                {
                    @Override
                    public void onResponse(Call<ListModel<Folder>> call, Response<ListModel<Folder>> response)
                    {
                        if(response.isSuccessful())
                        {
                            setFolders(response.body().Items);
                            if(folderSelectedListener != null)
                                folderSelectedListener.onLoadCompleted(response.body().Items);
                        }
                    }

                    @Override
                    public void onFailure(Call<ListModel<Folder>> call, Throwable t)
                    {
                        MLog.e("failed: ", t);
                    }
                });
    }

    public void setFolders(List<Folder> folders)
    {
        this.folders = folders;

        setSelectedFolder(null);
        setLoading(true);
        if(!isEmpty())
        {
            setSelectedFolder(folders.get(0));
            setLoading(false);
        }

        this.popup = null;
    }

    public void setSelectedFolder(Folder folder)
    {
        this.selectedFolder = folder;
        if(folder == null)
            tv.setText("");
        else
        {
            tv.setText(folder.Title);
        }

    }

    public Folder getSelectedFolder()
    {
        return this.selectedFolder;
    }

    private boolean isEmpty()
    {
        return folders == null || folders.isEmpty();
    }

    public void setLoading(boolean isLoading)
    {
        if(isLoading)
        {
            arrowIv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            this.setOnClickListener(null);
        }
        else
        {
            arrowIv.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            this.setOnClickListener(this);
        }
    }

    private View buildTree()
    {
        if(this.folders != null && this.folders.size() > 0)
        {
            TreeNode root = TreeNode.root();

            addChilds(root, this.folders, 0);
            AndroidTreeView tView = new AndroidTreeView(getContext(), root);

            tView.setDefaultViewHolder(FolderNodeHolder.class);
            tView.setDefaultContainerStyle(R.style.TreeNodeStyle);
            tView.setUseAutoToggle(false);
            tView.setDefaultNodeClickListener(new TreeNode.TreeNodeClickListener()
            {
                @Override
                public void onClick(TreeNode node, Object value)
                {
                    setSelectedFolder(((Folder) value));
                    if(popup != null)
                        popup.dismiss();

                    if(folderSelectedListener != null)
                    {
                        folderSelectedListener.onFolderSelectionChanged(selectedFolder);
                    }
                }
            });
            return tView.getView();
        }
        return null;
    }

    private void addChilds(TreeNode parent, List<Folder> childs, int level)
    {
        if(childs == null || childs.size() == 0) return;

        for(Folder f : childs)
        {
            TreeNode node = new TreeNode(f);
            addChilds(node, f.Children, level + 1);
            parent.addChild(node);
        }
    }

    @Override
    public void onClick(View view)
    {
        if(popup == null)
        {
            View v = buildTree();
            if(v == null) return;

            FrameLayout fl = new FrameLayout(getContext());
//            ViewGroup.LayoutParams flp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            fl.setBackgroundColor(0x33000000);
            fl.setClickable(true);
            fl.setFocusable(true);



            ViewGroup.LayoutParams vlp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ScrollView sv = new ScrollView(getContext());
            sv.addView(v, vlp);

            fl.addView(sv);

            v.setBackgroundResource(R.drawable.folders_dropdown_background);
            popup = new PopupWindow(getContext());
            popup.setWidth(this.getWidth());

            int[] loc = new int[2];
            this.getLocationInWindow(loc);


            int height = getRootView().getHeight() - loc[1] - this.getHeight();
            popup.setHeight(height);
            popup.setContentView(fl);
            popup.setFocusable(true);
            popup.setOutsideTouchable(true);
            popup.setBackgroundDrawable(new ColorDrawable(0x00000000));


            fl.setFocusableInTouchMode(true);
            fl.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    popup.dismiss();
                }
            });
        }

        if(popup.isShowing())
        {
            popup.dismiss();
        }
        else
        {
            popup.showAsDropDown(this, 0, -1);
        }
    }

    public static class FolderNodeHolder extends TreeNode.BaseNodeViewHolder<Folder>
    {
        ImageView arrow;
        View padLeft;
        TextView tv;

        public FolderNodeHolder(Context context)
        {
            super(context);
        }

        @Override
        public View createNodeView(final TreeNode node, Folder value) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final View view = inflater.inflate(R.layout.row_folder_item, null, false);

            tv = view.findViewById(R.id.rowFolder_text);
            arrow = view.findViewById(R.id.rowFolder_arrow);
            padLeft = view.findViewById(R.id.rowFolder_padLeft);

            int padSize = ScreenUtils.dpToPxInt(20) * (node.getLevel() - 1);
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight() + padSize, view.getPaddingBottom());

            tv.setText(value.Title);

            if(node.isLeaf())
            {
                arrow.setVisibility(View.INVISIBLE);
            }

            arrow.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    tView.toggleNode(node);
                }
            });

            node.setExpanded(value.Expanded);

            return view;
        }

        @Override
        public void toggle(boolean active)
        {
            arrow.animate().rotation(active ? -90 : 0).setDuration(200).start();
            //arrow.setImageResource(active ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_left);
        }
    }
}
