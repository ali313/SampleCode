package ir.nabaksoft.office;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ir.amp.tools.utils.ScreenUtils;
import ir.nabaksoft.office.api.Constants;
import ir.nabaksoft.office.fragment.LetterListFragment;
import ir.nabaksoft.office.fragment.MessageListFragment;
import ir.nabaksoft.office.fragment.TestFragment;
import ir.nabaksoft.office.model.Person;
import ir.nabaksoft.office.model.Role;
import ir.nabaksoft.office.tools.GlideApp;
import ir.nabaksoft.office.tools.Navigator;
import ir.nabaksoft.office.widget.CircleImageView;
import ir.nabaksoft.office.widget.DrawerMenuView;
import ir.nabaksoft.office.widget.RoleItemView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    DrawerLayout drawerLayout;
    View drawer;
    FrameLayout ctr_container;
    LinearLayout ctr_menusContainer;
    Person person;

    public Navigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        person = Person.get(this);

        setContentView(R.layout.activity_main);
        navigator = new Navigator(this, R.id.frag_container);

        initViews();
        goToMenu(R.id.drawer_incomeLetters);
        //navigator.goTo(TestFragment.class, null, true);

        /*Bundle args = new Bundle();
        args.putString("type", "income");
        navigator.goTo(LetterListFragment.class, args, true);*/
    }

    private void initViews()
    {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        ctr_container = findViewById(R.id.frag_container);
        ctr_menusContainer = findViewById(R.id.drawer_menusContainer);

        drawer = findViewById(R.id.drawer);
        drawerLayout = findViewById(R.id.drawerLayout);

        DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams)drawer.getLayoutParams();
        lp.width = Math.min((int)(ScreenUtils.screenWidth() * 0.75), ScreenUtils.dpToPxInt(400));
        drawer.setLayoutParams(lp);

        View v = findViewById(R.id.ic_menu);
        v.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(drawerLayout.isDrawerOpen(drawer))
                    drawerLayout.closeDrawer(drawer);
                else
                    drawerLayout.openDrawer(drawer);
            }
        });

        for(int i = 0; i < ctr_menusContainer.getChildCount(); i++)
        {
            View view = ctr_menusContainer.getChildAt(i);
            int id = view.getId();

            if(view instanceof DrawerMenuView)
                view.setOnClickListener(this);

        }
        findViewById(R.id.drawer_exitBtn).setOnClickListener(this);
        initDrawerMenu();
    }

    private void initDrawerMenu()
    {
        //Toast.makeText(getApplicationContext(),"1111111111111111",Toast.LENGTH_LONG).show();

        //setting personImg
        CircleImageView personImg = findViewById(R.id.personImg);
        TextView nameTv = findViewById(R.id.drawer_name);
        final LinearLayout rolesContainer = findViewById(R.id.drawer_rolesList);

        GlideApp.with(this)
                .load(WebUtil.BaseURL + "/Api/Person/PersonImage.ashx")
                .placeholder(R.drawable.avatar)
                .into(personImg);

        nameTv.setText(person.getNameFamily());

        if(person.roles != null)
        {
            int i = 0;
            /*Role role = new Role();
            role.Title = "رییس مرکز تحقیقاتی سیب سیب سیب سی بس یبسیب فلانه جا";
            role.Id=2;
            person.roles.add(role);*/

            for (Role r : person.roles)
            {
                final RoleItemView rview = new RoleItemView(this);
                rview.setTitle(r.Title);
                rview.RoleId=r.Id;
                rview.setChecked(i == 0);
                i++;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rolesContainer.addView(rview, lp);
                rview.setOnClickListener((new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //person.selectedRoleId=rview.RoleId;
                        person.SetSelectedRoleId(rview.RoleId);
                        //Toast.makeText(getApplicationContext(),person.selectedRoleId+"",Toast.LENGTH_LONG).show();
                        goToMenu(R.id.drawer_incomeLetters);
                        for(int index=0; index< rolesContainer.getChildCount(); index++) {
                            View mchild = rolesContainer.getChildAt(index);
                            if(!(mchild instanceof RoleItemView))
                                continue;
                            ((RoleItemView)mchild).setChecked(false);
                        }
                        ((RoleItemView)v).setChecked(true);
                    }
                }
                ));

                View hline = new View(this);
                hline.setBackgroundColor(0xffcccccc);
                LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                rolesContainer.addView(hline, lineLp);
            }
        }
    }

    long lastBackPressTime = 0;
    @Override
    public void onBackPressed()
    {
        if(!navigator.handleBackPress())
        {
            if(System.currentTimeMillis() - lastBackPressTime < 2000)
            {
                super.onBackPressed();
            }
            else
            {
                lastBackPressTime = System.currentTimeMillis();
                Toast.makeText(this, "برای خروج دوباره کلید بازگشت را بزنید", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if(id == R.id.drawer_exitBtn)
        {
            Person.logout(this);
        }
        else if(view instanceof DrawerMenuView)
        {
            goToMenu(view);
        }
    }

    private void goToMenu(View view)
    {
        goToMenu(view.getId());
    }

    private void goToMenu(int menuId)
    {
        //Toast.makeText(getApplicationContext(),(menuId==R.id.drawer_incomeLetters)+"",Toast.LENGTH_LONG).show();

        drawerLayout.closeDrawer(drawer);
        Bundle args = new Bundle();
        switch (menuId)
        {
            case R.id.drawer_incomeLetters:
            {
                args.putInt(LetterListFragment.ARG_TYPE, Constants.TYPE_RECEIVED);
                args.putInt(LetterListFragment.ARG_STATE, Constants.STATE_CURRENT);
                navigator.goTo(LetterListFragment.class, args, true);
                break;
            }
            case R.id.drawer_sentLetters:
            {
                args.putInt(LetterListFragment.ARG_TYPE, Constants.TYPE_SEND);
                args.putInt(LetterListFragment.ARG_STATE, Constants.STATE_CURRENT);
                navigator.goTo(LetterListFragment.class, args, true);
                break;
            }
            case R.id.drawer_archeved_sent:
            {
                args.putInt(LetterListFragment.ARG_TYPE, Constants.TYPE_SEND);
                args.putInt(LetterListFragment.ARG_STATE, Constants.STATE_ARCHIVED);
                navigator.goTo(LetterListFragment.class, args, true);
                break;
            }
            case R.id.drawer_archived_recieved:
            {
                args.putInt(LetterListFragment.ARG_TYPE, Constants.TYPE_RECEIVED);
                args.putInt(LetterListFragment.ARG_STATE, Constants.STATE_ARCHIVED);
                navigator.goTo(LetterListFragment.class, args, true);
                break;
            }
            case R.id.drawer_incomeMessage:
            {
                args.putInt(MessageListFragment.ARG_TYPE, Constants.TYPE_RECEIVED);
                navigator.goTo(MessageListFragment.class, args, true);
                break;
            }
            case R.id.drawer_sentMessages:
            {
                args.putInt(MessageListFragment.ARG_TYPE, Constants.TYPE_SEND);
                navigator.goTo(MessageListFragment.class, args, true);
                break;
            }
        }
    }
}
