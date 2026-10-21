package br.com.kaiohonorato.centraltv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Central TV: abre o painel em tela cheia e não deixa ninguém sair dele.
 * Manutenção: no controle, aperte  ↑ ↑ ↓ ↓ ← → ← →  para abrir as configurações do Android.
 */
public class MainActivity extends Activity {

    private static final long RECARREGAR_A_CADA = 6L * 60 * 60 * 1000; // pega versão nova do painel
    private static final long TENTAR_DE_NOVO = 15_000;

    private static final int[] SEQUENCIA_MANUTENCAO = {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WebView web;
    private TextView aviso;
    private int posicaoSequencia = 0;
    private long ultimaTecla = 0;
    private boolean falhou = false;

    private final Runnable tentarDeNovo = new Runnable() {
        @Override public void run() { carregar(); }
    };
    private final Runnable recarregarPeriodico = new Runnable() {
        @Override public void run() {
            if (web != null) web.reload();
            handler.postDelayed(this, RECARREGAR_A_CADA);
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FrameLayout raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.parseColor("#1c1d20"));

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // guarda o login do painel
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        web.setBackgroundColor(Color.parseColor("#1c1d20"));
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!falhou) aviso.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError erro) {
                if (Build.VERSION.SDK_INT >= 21 && req.isForMainFrame()) semConexao();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int codigo, String desc, String url) {
                if (url != null && url.equals(view.getUrl())) semConexao();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // tudo abre dentro do painel
            }
        });

        aviso = new TextView(this);
        aviso.setTextColor(Color.parseColor("#d8d6c9"));
        aviso.setTextSize(22);
        aviso.setGravity(Gravity.CENTER);
        aviso.setBackgroundColor(Color.parseColor("#1c1d20"));
        aviso.setText("Abrindo a Central…");

        raiz.addView(web, new FrameLayout.LayoutParams(-1, -1));
        raiz.addView(aviso, new FrameLayout.LayoutParams(-1, -1));
        setContentView(raiz);

        carregar();
        handler.postDelayed(recarregarPeriodico, RECARREGAR_A_CADA);
    }

    private boolean temInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo n = cm != null ? cm.getActiveNetworkInfo() : null;
        return n != null && n.isConnected();
    }

    private void carregar() {
        handler.removeCallbacks(tentarDeNovo);
        if (!temInternet()) { semConexao(); return; }
        falhou = false;
        web.loadUrl(getString(R.string.painel_url));
    }

    private void semConexao() {
        falhou = true;
        aviso.setText("Sem conexão com a internet\nTentando de novo em instantes…");
        aviso.setVisibility(View.VISIBLE);
        handler.removeCallbacks(tentarDeNovo);
        handler.postDelayed(tentarDeNovo, TENTAR_DE_NOVO);
    }

    @Override
    protected void onResume() {
        super.onResume();
        esconderBarras();
        web.onResume();
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) esconderBarras();
    }

    private void esconderBarras() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            long agora = System.currentTimeMillis();
            if (agora - ultimaTecla > 4000) posicaoSequencia = 0;
            ultimaTecla = agora;
            if (e.getKeyCode() == SEQUENCIA_MANUTENCAO[posicaoSequencia]) {
                posicaoSequencia++;
                if (posicaoSequencia == SEQUENCIA_MANUTENCAO.length) {
                    posicaoSequencia = 0;
                    abrirConfiguracoes();
                    return true;
                }
            } else {
                posicaoSequencia = e.getKeyCode() == SEQUENCIA_MANUTENCAO[0] ? 1 : 0;
            }
        }
        return super.dispatchKeyEvent(e);
    }

    private void abrirConfiguracoes() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException ignorar) {
            // box sem tela de configurações padrão
        }
    }

    @Override
    public void onBackPressed() {
        // Voltar nunca sai do painel
        if (web.canGoBack()) web.goBack();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        web.destroy();
        super.onDestroy();
    }
}
