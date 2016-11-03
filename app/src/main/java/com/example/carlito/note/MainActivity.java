package com.example.carlito.note;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import com.example.carlito.note.firebase.*;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = "tag";
    private GoogleApiClient mGoogleApiClient;
    private Toolbar mToolbar;
    private InterstitialAd mInterstitialAd;
    private EditText mEditText;
    private TextView mTextViewCodigo;
    private TextView mTextViewAnotacao;

    private Anotacao mUltimaAnotacao;
    /*
   Iniciar uma nova instância da Activity
    */
    public static void startNewInstance(AppCompatActivity activity){
        Intent intent = new Intent(activity,MainActivity.class);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);
        configureGoogleApiClient();
        configurarDrawer();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();
                }
            }
        });

        mEditText = (EditText) findViewById(R.id.edit_text);
        mTextViewCodigo = (TextView) findViewById(R.id.codigo_anotacao);
        mTextViewAnotacao = (TextView) findViewById(R.id.anotacao);


        //
        // Configurar o Firebase Database para ativar modo offline
        //
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        //
        // Ação do botão salvar
        //
        findViewById(R.id.bt_salvar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarNovoRegistro();
            }
        });


        //
        // Ação do botão atualizar
        //
        findViewById(R.id.bt_atualizar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarRegistro();
            }
        });


        //
        // Ação do botão deletar
        //
        findViewById(R.id.bt_deletar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletarRegistro();
            }
        });


        //
        // Busca as anotações do firebase
        //
        obterTodos();

        configureAds();
        configureInterstitialAd();
    }
    private void configurarDrawer(){
        // Configura a library Glide para carregar a imagem no Drawer
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }
            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }
        });
        // Obtem o usuário logado
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Cria o header com os detalhes do usuário
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(user.getDisplayName())
                                .withEmail(user.getEmail())
                                .withIcon(user.getPhotoUrl())
                )
                .build();
        // Cria o item de logout com o listener
        SecondaryDrawerItem item1 = new SecondaryDrawerItem().withName("Sign out");
        item1.withOnDrawerItemClickListener( new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                signOut();
                return true;
            }
        });
        // Cria o drawer
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .addDrawerItems(new PrimaryDrawerItem().withName("Contatos"))
                .addDrawerItems(new PrimaryDrawerItem().withName("Mapa"))
                .addDrawerItems(new PrimaryDrawerItem().withName("Configurações"))
                .addDrawerItems(new DividerDrawerItem())
                .addDrawerItems(item1)
                .withAccountHeader(headerResult)
                .build();
    }
    private void configureGoogleApiClient(){
        // Cria a opções de login do Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Cria uma nova instância do cliente do Google Api com as opções criadas acima
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
    private void signOut(){
        // Realiza o logout no Firebase
        FirebaseAuth.getInstance().signOut();
        // Realiza o logout na Api do Google
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // Logout realizado, volta para a tela de Login
                        LoginActivity.startNewInstance(MainActivity.this);
                        Toast.makeText(MainActivity.this,"Até a próxima!",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void configureAds() {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
    private void configureInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_interstitial_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                Toast.makeText(getApplicationContext(),"Fechou o ADS",Toast.LENGTH_SHORT).show();
            }
        });
        requestNewInterstitial();
    }
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void deletarRegistro(){

        // Se nao for nulo

        if(mUltimaAnotacao != null){
            //
            // Manda deletar
            //
            DeletarAnotacao deletarAnotacao = new DeletarAnotacao();
            deletarAnotacao.deletar(mUltimaAnotacao);
            //
            // Recarregar lista
            //
            obterTodos();
        }
    }

    private void atualizarRegistro(){

        // Se nao for nulo

        if(mUltimaAnotacao != null){


            //
            // Atualiza o valor da anotação com base no que foi digitado no edittext
            //
            mUltimaAnotacao.setValor(mEditText.getText().toString());

            //
            // Manda deletar
            //
            AtualizarAnotacao atualizarAnotacao = new AtualizarAnotacao();
            atualizarAnotacao.atualizar(mUltimaAnotacao);
            obterTodos();
            //
            // Recarregar lista
            //
            obterTodos();
        }
    }

    private void salvarNovoRegistro() {

        //
        // Salva uma nova anotação
        //

        SalvarAnotacao salvar = new SalvarAnotacao();
        salvar.novoRegistro(mEditText.getText().toString());

        // Limpa o campo de texto
        mEditText.setText("");

        // Atualiza a lista
        obterTodos();
    }

    private void obterTodos(){
        ObterNotas obterNotas = new ObterNotas();
        obterNotas.todos(new ObterNotas.OnObterAnotacoesListener() {
            @Override
            public void onAnotacoesObtidas(List<Anotacao> lista) {

                //
                // Se existir anotacoes, pega a ultima e exibe em tela
                //
                if(lista.size() > 0){
                    mUltimaAnotacao = lista.get(lista.size() - 1);
                    exibirAnotacao();
                }else{
                    // Seta para nulo a ultima anotação
                    mUltimaAnotacao = null;
                    //
                    // Limpa os valores da tela
                    //
                    mTextViewAnotacao.setText("");
                    mTextViewCodigo.setText("");
                }

            }
        });
    }

    private void exibirAnotacao(){
        //
        // Exibe em tela as informações da anotação
        //
        mTextViewAnotacao.setText(mUltimaAnotacao.getValor());
        mTextViewCodigo.setText(mUltimaAnotacao.getUid());
    }

}