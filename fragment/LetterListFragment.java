package ir.nabaksoft.office.fragment;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ir.nabaksoft.office.MainActivity;
import ir.nabaksoft.office.R;
import ir.nabaksoft.office.adapter.LettersAdapter;
import ir.nabaksoft.office.api.ApiUtils;
import ir.nabaksoft.office.api.Constants;
import ir.nabaksoft.office.api.HandledCallback;
import ir.nabaksoft.office.model.Folder;
import ir.nabaksoft.office.model.Letter;
import ir.nabaksoft.office.model.ListModel;
import ir.nabaksoft.office.model.Person;
import ir.nabaksoft.office.tools.SwipeController;
import ir.nabaksoft.office.tools.SwipeControllerActions;
import ir.nabaksoft.office.widget.FoldersView;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Ali on 6/7/2019.
 */

public class LetterListFragment extends BaseFragment implements LettersAdapter.LetterListItemClicked, FoldersView.FolderViewCallback, SwipeRefreshLayout.OnRefreshListener
{
    public final static String ARG_TYPE = "type";
    public final static String ARG_STATE = "state";
    public final static String ARG_FOLDER = "folderId";
    public final static String ARG_IS_DRAFT = "isDraft";

    RecyclerView recyclerView;
    FoldersView ctr_folders;

    LettersAdapter adapter;
    String type;
    int start = 0;
    int limit = 10;
    int allCount;
    SwipeController swipeController;
    List<Letter> loadedLetters = null;
    List<Folder> loadedFolders = null;
    Folder selectedFolder = null;
    SwipeRefreshLayout swipeRefreshLayout;

    @Constants.LetterState int letterState;
    @Constants.LetterType int letterType;
    int folderId;
    boolean isDraft;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_letter_list, container, false);

        recyclerView = view.findViewById(R.id.lettersList);
        ctr_folders = view.findViewById(R.id.foldersDropDown);

        ctr_folders.setOnFolderSelectedListener(this);

        Bundle args = getArguments();
        if(args != null)
        {
            letterType = args.getInt(ARG_TYPE, Constants.TYPE_RECEIVED);
            letterState = args.getInt(ARG_STATE, Constants.STATE_CURRENT);
            folderId = args.getInt(ARG_FOLDER, -1);
            isDraft = args.getBoolean(ARG_IS_DRAFT, false);
        }

        swipeRefreshLayout = view.findViewById(R.id.letter_swipe);
        swipeRefreshLayout.setOnRefreshListener(this);

        if(savedInstanceState != null)
        {
            //اینجا میشه دیتایی که اون پایین ذخیره کردیم رو بگیریم و لود کنیم
            //String data = savedInstanceState.getString("data");
        }

        initList();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);


        if(loadedLetters == null || loadedLetters.size() == 0)
        {
            loadLetters();
        }
        else
        {
            adapter.setLoading(false);
            adapter.setHasMore(allCount > start);
            adapter.addItems(loadedLetters);
        }

        if(loadedFolders == null)
        {
            ctr_folders.loadFolders(letterState, letterType, isDraft);
        }
        else
        {
            ctr_folders.setFolders(loadedFolders);
            ctr_folders.setSelectedFolder(this.selectedFolder);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        //اینجا میشه دیتا رو ذخیره کرد تا بشه دوباره بازیابی کرد. مثلا:
        //outState.putString("data", data);
        super.onSaveInstanceState(outState);
    }

    private void initList()
    {
        adapter = new LettersAdapter(getContext(), new ArrayList<Letter>());
        adapter.setOnItemClickListener(this);
        RecyclerView.LayoutManager lmanager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lmanager);

        DividerItemDecoration decor = new DividerItemDecoration(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL)
        {

        };
        decor.setDrawable(getContext().getResources().getDrawable(R.drawable.list_divider));

        recyclerView.addItemDecoration(decor);
        recyclerView.setAdapter(adapter);


    }

    public void loadLetters()
    {
        adapter.setLoading(true);

        swipeController = new SwipeController(this.getContext(),new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                Toast.makeText(getActivity().getApplicationContext(),"دکمه سمت راست",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLeftClicked(int position) {
                Toast.makeText(getActivity().getApplicationContext(),"دکمه سمت چپ",Toast.LENGTH_LONG).show();
            }
        });
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        ApiUtils.getApi(getContext()).LetterList(start, limit, folderId, letterState, letterType, isDraft)
                .enqueue(new HandledCallback<ListModel<Letter>>(getContext())
                {
                    @Override
                    public void onResponse(Call<ListModel<Letter>> call, Response<ListModel<Letter>> response)
                    {
                        super.onResponse(call, response);
                        if(response.isSuccessful())
                        {
                            allCount = response.body().count;
                            start += response.body().Items.size();
                            adapter.setLoading(false);
                            adapter.setHasMore(allCount > start);
                            adapter.addItems(response.body().Items);

                            if(loadedLetters == null)
                                loadedLetters = new ArrayList<>();

                            loadedLetters.addAll(response.body().Items);
                        }
                        else
                            Toast.makeText(getActivity().getApplicationContext(),"بروز اشکال در اطلاعات دریافتی",Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onFailure(Call<ListModel<Letter>> call, Throwable t)
                    {
                        Toast.makeText(getActivity(),t.getMessage(),Toast.LENGTH_LONG ).show();
                        Log.d("aaaaaaa",t.getMessage());
                        Log.d("aaaaaaa",t.getStackTrace().toString());
                        Toast.makeText(getActivity().getApplicationContext(),
                                "عدم امکان ارتباط. لطفا ارتباط خود با اینترنت را چک نمایید", Toast.LENGTH_LONG).show();
                    }

                });



       recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }


    @Override
    public void onLetterClicked(int position, Letter letter)
    {
        Bundle args = new Bundle();
        String letterType="";
        if(letter.RecipientID>0)
        {
            args.putLong("RecId", letter.RecipientID);
            letterType="RecipientList";
        }
        else
        {
            args.putLong("RecId", letter.LetterID);
        }
        args.putString("LetterType", letterType);
        ((MainActivity)getActivity()).navigator.goTo(LetterViewFragment.class, args, false);
        //Toast.makeText(getContext(),message.id+"-"+message.title,Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onLoadMoreClicked()
    {
        loadLetters();
    }

    @Override
    public void onFolderSelectionChanged(Folder selectedFolder)
    {
        this.folderId = selectedFolder.FolderID;
        this.selectedFolder = selectedFolder;
        this.adapter.clearItems();
        start=0;
        loadLetters();
    }

    @Override
    public void onLoadCompleted(List<Folder> folders)
    {
        this.loadedFolders = folders;
        this.selectedFolder = ctr_folders.getSelectedFolder();
    }

    @Override
    public void onRefresh()
    {
        start = 0;
        limit = 10;
        allCount = 0;
        loadedLetters = null;
        loadedFolders = null;
        if(adapter != null)
        {
            adapter.clearItems();
            adapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        ctr_folders.loadFolders(letterState, letterType, isDraft);
        loadLetters();
    }
}

