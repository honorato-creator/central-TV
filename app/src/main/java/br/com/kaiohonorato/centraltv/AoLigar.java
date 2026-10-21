package br.com.kaiohonorato.centraltv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Abre o painel quando a box termina de ligar. */
public class AoLigar extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent abrir = new Intent(context, MainActivity.class);
        abrir.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(abrir);
    }
}
