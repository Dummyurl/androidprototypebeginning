package amiin.bazouk.application.com.localisationdemo.Forms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import amiin.bazouk.application.com.localisationdemo.MapsActivity;
import amiin.bazouk.application.com.localisationdemo.R;
import amiin.bazouk.application.com.localisationdemo.SellActivity;

public class SellForm extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sell_form_activity);

        ((EditText)findViewById(R.id.price)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    findViewById(R.id.volume).requestFocus();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(SellForm.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(SellForm.this);
                }
                String priceString = ((EditText)findViewById(R.id.price)).getText().toString();
                String volumeString = ((EditText)findViewById(R.id.volume)).getText().toString();
                if(priceString.isEmpty())
                { builder.setTitle("Price empty")
                        .setMessage("The Price cannot be empty")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                    return;
                }
                else if(volumeString.isEmpty())
                {
                    builder.setTitle("Volume empty")
                            .setMessage("The Volume cannot be empty")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert).show();
                    return;
                }
                Intent result = new Intent(SellForm.this, SellActivity.class);
                result.putExtra(MapsActivity.PRICE_SELL_INTENT_VALUE, Double.valueOf(((EditText)findViewById(R.id.price)).getText().toString()));
                result.putExtra(MapsActivity.VOLUME_SELL_INTENT_VALUE, Double.valueOf(((EditText)findViewById(R.id.volume)).getText().toString()));
                setResult(RESULT_OK, result);
                result.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                finish();
            }
        });
    }
}
