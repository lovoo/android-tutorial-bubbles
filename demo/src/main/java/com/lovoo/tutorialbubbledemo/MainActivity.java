package com.lovoo.tutorialbubbledemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lovoo.tutorialbubbles.TutorialScreen;

public class MainActivity extends AppCompatActivity {

    private TutorialScreen buttonTutorialScreen;

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick ( View view ) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final Button explainButton = (Button) findViewById(R.id.explain_button);
        explainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick ( View v ) {
                buttonTutorialScreen.showTutorial();
            }
        });

        explainButton.post(new Runnable() {
            @Override
            public void run () {
                buttonTutorialScreen = new TutorialScreen.TutorialBuilder(R.layout.button_tutorial_layout, explainButton)
                        .setParentLayout(findViewById(R.id.root_layout))
                        .setDismissible(true)
                        .addHighlightView(explainButton, true)
                        .setOnTutorialLayoutInflatedListener(new TutorialScreen.OnTutorialLayoutInflatedListener() {
                            @Override
                            public void onLayoutInflated ( View view ) {
                                // put code here for tutorial
                                view.findViewById(R.id.tutorial_inner_button).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick ( View v ) {
                                        Toast.makeText(MainActivity.this, "Some buton action", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .build();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu ( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected ( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
