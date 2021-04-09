package mchou.apps.biblio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {
    ImageView logo;
    ConstraintLayout root;
    Button btn1,btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        logo = findViewById(R.id.logo);
        root = findViewById(R.id.root);

        btn1 = findViewById(R.id.btnGoogle);
        btn2 = findViewById(R.id.btnOpenLibrary);

        btn1.setAlpha(0f);
        btn2.setAlpha(0f);
        startAnimation();

    }

    private void startAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        fadeAnim.setDuration(2500);
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                //((ObjectAnimator)animation).getTarget();
                animateLogo();
            }
        });

        animatorSet.play(fadeAnim);
        animatorSet.start();
    }

    private void animateLogo() {
        ConstraintSet finishingConstraintSet= new ConstraintSet();
        finishingConstraintSet.clone(getApplicationContext(), R.layout.activity_start_final);

        TransitionManager.beginDelayedTransition(root);
        finishingConstraintSet.applyTo(root);

        logo.setImageDrawable(getResources().getDrawable(R.drawable.ic_app_logo,null));

        ValueAnimator fade1Anim =ObjectAnimator.ofFloat(btn1, "alpha", 0f, 1f).setDuration(1500);
        ValueAnimator fade2Anim =ObjectAnimator.ofFloat(btn2, "alpha", 0f, 1f).setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(fade1Anim).before(fade2Anim);
        animatorSet.start();
    }

    public void open(View view) {
        Intent intent = new Intent(this, (view == btn1)?MainGActivity.class:MainActivity.class);
        startActivity(intent);
    }

    //boolean set=false;
/*    private void addAnimationOperations() {
        ConstraintSet startingConstraintSet = new ConstraintSet();
        startingConstraintSet.clone(root);
        ConstraintSet finishingConstraintSet= new ConstraintSet();
        finishingConstraintSet.clone(this, R.layout.activity_start_final);

        finishingConstraintSet.applyTo(root);

        //logo.setOnClickListener((v)->{
            *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(root);
                *//**//*ConstraintSet constraint = set ? startingConstraintSet : finishingConstraintSet;
                constraint.applyTo(root);*//**//*
                finishingConstraintSet.applyTo(root);
                //set=!set;
            }*//*
        //});
    }*/
}
