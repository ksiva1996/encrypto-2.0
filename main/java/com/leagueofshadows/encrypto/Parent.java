package com.leagueofshadows.encrypto;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;

public class Parent extends AppCompatActivity {

    static ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        pd = new ProgressDialog(this);
        pd.setMessage("Logging in");
        pd.setCancelable(false);
    }

    public static class PlaceholderFragment extends Fragment
    {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final int pos =  getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;
            if(pos==1)
            {
                rootView = inflater.inflate(R.layout.login_fragment,container,false);
                final TextView username = (TextView) rootView.findViewById(R.id.username);
                final TextView password = (TextView)rootView.findViewById(R.id.password);
                Button login = (Button)rootView.findViewById(R.id.login);
                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String u =  username.getText().toString();
                        final String p = password.getText().toString();
                        if(check(u))
                        {
                            if(check(p))
                            {
                                /*Intent i = new Intent(getContext(),MainActivity.class);
                                startActivity(i);*/
                                pd.show();
                                String url = Util.url+"login.php";
                                StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try
                                        {
                                            JSONObject json = new JSONObject(response);
                                            int success = json.getInt(Util.success);
                                            if(success==1)
                                            {
                                                String privateKey = json.getString(Util.privateKey);
                                                String publicKey = json.getString(Util.publicKey);
                                                SharedPreferences sp = getContext().getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
                                                SharedPreferences.Editor edit = sp.edit();
                                                edit.putString(Util.privateKey,privateKey);
                                                edit.putString(Util.publicKey,publicKey);
                                                edit.putString(Util.username,u);
                                                edit.apply();
                                                storeBytes(p);
                                                Toast.makeText(getContext(),"Login successful",Toast.LENGTH_SHORT).show();
                                                pd.dismiss();
                                                Intent i = new Intent(getContext(),MainActivity.class);
                                                startActivity(i);
                                                getActivity().finish();
                                            }
                                            else
                                            {
                                                Toast.makeText(getContext(),"Username or Password incorrect",Toast.LENGTH_SHORT).show();
                                                pd.dismiss();
                                            }
                                        }
                                        catch (JSONException e) {
                                            pd.dismiss();
                                            e.printStackTrace();
                                        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    }
                                })
                                {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        Map<String ,String> params = new HashMap<>();
                                        params.put(Util.username,u);
                                        params.put(Util.password,p);
                                        return params;
                                    }
                                };
                                VolleyHelper.getInstance(getContext()).addToRequestQueue(req);
                            }
                            else {
                                password.setError("password required");
                                password.requestFocus();
                            }
                        }
                        else {
                            username.setError("username required");
                            username.requestFocus();
                        }
                    }
                });
            }
            else
            {
                rootView = inflater.inflate(R.layout.signup_fragment,container,false);
                final TextView name = (TextView) rootView.findViewById(R.id.name);
                final TextView username = (TextView) rootView.findViewById(R.id.username);
                final TextView password = (TextView)rootView.findViewById(R.id.password);
                Button signup = (Button)rootView.findViewById(R.id.signup);
                signup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String u = username.getText().toString();
                        final String p = password.getText().toString();
                        final String n = name.getText().toString();
                        if (check(n))
                        {
                            if (check(u))
                            {
                                if (check(p))
                                {
                                    pd.show();
                                    String url = Util.url + "signup.php";
                                    StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                Log.i("ENCRYPTO",response);
                                                JSONObject json = new JSONObject(response);
                                                int success = json.getInt(Util.success);
                                                if (success == 1)
                                                {
                                                    Toast.makeText(getContext(), "SignUp successful", Toast.LENGTH_SHORT).show();
                                                    SharedPreferences sp = getContext().getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor edit = sp.edit();
                                                    edit.putString(Util.username,u);
                                                    edit.apply();
                                                    Keys keys = new Keys(p,getContext());
                                                    String x[] = keys.createKeys();
                                                    uploadKeys(x,u);
                                                    storeBytes(p);
                                                    pd.dismiss();
                                                    /*Intent i = new Intent(getContext(),MainActivity.class);
                                                    startActivity(i);
                                                    getActivity().finish();*/
                                                }
                                                else
                                                {
                                                    Toast.makeText(getContext(), "Username already exists ", Toast.LENGTH_SHORT).show();
                                                    pd.dismiss();
                                                }
                                            } catch (JSONException e) {
                                                pd.dismiss();
                                                e.printStackTrace();
                                            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                            pd.dismiss();
                                        }
                                    }) {
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {
                                            Map<String, String> params = new HashMap<>();
                                            params.put(Util.name, n);
                                            params.put(Util.username, u);
                                            params.put(Util.password, p);
                                            return params;
                                        }
                                    };
                                    VolleyHelper.getInstance(getContext()).addToRequestQueue(req);
                                } else {
                                    password.setError("password required");
                                    password.requestFocus();
                                }
                            } else {
                                username.setError("username required");
                                username.requestFocus();
                            }
                        }
                        else
                        {
                            name.setError("name required");
                            name.requestFocus();
                        }
                    }
                });
            }

            return rootView;
        }

        private void storeBytes(String p) throws NoSuchPaddingException, NoSuchAlgorithmException {
            KeyUtil keyutil = new KeyUtil(p,getContext());
            keyutil.encryptBytes();
        }

        private void uploadKeys(final String[] x, final String username) {
            pd.show();
            String url = Util.url + "keys.php";
            StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.i("ENCRYPTO",response);
                        JSONObject json = new JSONObject(response);
                        int success = json.getInt(Util.success);
                        if (success == 1)
                        {
                            pd.dismiss();
                        }
                        else
                        {
                            pd.dismiss();
                            //TODO do something here to handle the exception
                        }
                    } catch (JSONException e) {
                        pd.dismiss();
                        //TODO add to queue to update later when there is network
                        e.printStackTrace();
                    }
                    Intent i = new Intent(getContext(),MainActivity.class);
                    startActivity(i);
                    getActivity().finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put(Util.publicKey, x[0]);
                    params.put(Util.privateKey,x[1] );
                    params.put(Util.username,username);
                    Log.i("params",username);
                    return params;
                }
            };
            VolleyHelper.getInstance(getContext()).addToRequestQueue(req);
        }

        private boolean check(String u) {
            return !u.equals("");
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "LOGIN";
                case 1:
                    return "SIGNUP";
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        //setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
