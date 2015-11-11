package com.lovoo.tutorialbubbledemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lovoo.tutorialbubbles.TutorialScreen;
import com.lovoo.tutorialbubbles.utils.Utils;

/**
 * Demo activity that shows the use of the tutorial bubbles
 */
public class MainActivity extends AppCompatActivity {

    private TutorialScreen buttonTutorial;
    private TutorialScreen fabButtonTutorial;

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button explainButton = (Button) findViewById(R.id.explain_button);
        explainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick ( View v ) {
                // call this to dispplay your tutorial
                buttonTutorial.showTutorial();
            }
        });

        explainButton.post(new Runnable() {
            @Override
            public void run () {
                buttonTutorial = new TutorialScreen.TutorialBuilder(R.layout.button_tutorial_layout, explainButton)
                        .setParentLayout(getWindow().getDecorView())    // parent layout is necessary for layout approach, use decorView or a root relative layout
                        .setDismissible(true)                           // set if this bubble can be dismissed by clicking somewhere outside of its context
                        .addHighlightView(explainButton, false)         // sets the view that should be explained
                        .setOnTutorialLayoutInflatedListener(new TutorialScreen.OnTutorialLayoutInflatedListener() {
                            // you can use this callback to bind the bubble layout and apply logic to it
                            @Override
                            public void onLayoutInflated ( View view ) {
                                // put code here for tutorial
                                view.findViewById(R.id.tutorial_inner_button).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick ( View v ) {
                                        Toast.makeText(MainActivity.this, "Button in bubble clicked.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .build();

            }
        });

        // another example how to further customize the bubble
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.post(new Runnable() {
            @Override
            public void run () {
                fabButtonTutorial = new TutorialScreen.TutorialBuilder(R.layout.fab_tutorial_layout, fab)
                        .setParentLayout(getWindow().getDecorView())
                        .addHighlightView(fab, false)
                        .setTutorialBackgroundColor(getResources().getColor(R.color.transparentRed)) // set another bubble color
                        .setFunnelLength(Utils.dpToPx(getApplicationContext(), 35))                // changes the length of the bubble funnel
                        .setFunnelWidth(Utils.dpToPx(getApplicationContext(), 30))                 // changes the width of the bubble funnel
                        .setTutorialOffsetFromAnchor(Utils.dpToPx(getApplicationContext(), 8))    // sets the distance between anchor and bubble
                        .setOnTutorialLayoutInflatedListener(new TutorialScreen.OnTutorialLayoutInflatedListener() {
                            @Override
                            public void onLayoutInflated ( View view ) {
                                view.findViewById(R.id.tutorial_inner_button).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick ( View v ) {
                                        fabButtonTutorial.dismissTutorial();
                                    }
                                });
                            }
                        })
                        .build();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick ( View view ) {
                fabButtonTutorial.showTutorial();
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
