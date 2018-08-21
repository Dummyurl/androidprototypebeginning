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

import java.util.List;

import amiin.bazouk.application.com.localisationdemo.BuyActivity;
import amiin.bazouk.application.com.localisationdemo.MapsActivity;
import amiin.bazouk.application.com.localisationdemo.MarkerSold;
import amiin.bazouk.application.com.localisationdemo.R;

public class BuyForm extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_form_activity);

        ((EditText)findViewById(R.id.min_price)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    findViewById(R.id.max_price).requestFocus();
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
                    builder = new AlertDialog.Builder(BuyForm.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(BuyForm.this);
                }
                String minPriceString = ((EditText)findViewById(R.id.min_price)).getText().toString();
                String maxPriceString = ((EditText)findViewById(R.id.max_price)).getText().toString();
                if(minPriceString.isEmpty())
                { builder.setTitle("Minimum Price empty")
                        .setMessage("The Minimum Price cannot be empty")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                    return;
                }
                else if(maxPriceString.isEmpty())
                {
                    builder.setTitle("Maximum Price empty")
                            .setMessage("The Maximum Price cannot be empty")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert).show();
                    return;
                }
                double minPrice = Double.valueOf(minPriceString);
                double maxPrice = Double.valueOf(maxPriceString);
                if(minPrice>maxPrice)
                {
                    builder.setTitle("Maximum Price lower than Minimum Price")
                            .setMessage("The Maximum Price is lower than the Minimum Price")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert).show();
                    return;
                }
                Intent result = new Intent();
                result.putExtra(BuyActivity.PRICE_MIN_BUY_INTENT_VALUE,Double.doubleToRawLongBits(minPrice));
                result.putExtra(BuyActivity.PRICE_MAX_BUY_INTENT_VALUE,Double.doubleToRawLongBits(maxPrice));
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }
}
