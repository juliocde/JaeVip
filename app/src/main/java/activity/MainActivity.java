package activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import config.ConfiguracaoFirebase;
import helper.Base64Custom;
import helper.Preferencias;
import jaevip.com.jaevip.R;
import model.Usuario;

//import com.facebook.FacebookSdk;
//import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {


    private EditText email;
    private EditText senha;
    private ImageView botaoLogar;
    private ImageView botaoFacebook;
    private TextView cadastre;
    private Usuario usuario;
    private FirebaseAuth autenticacao;
    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerUsuario;
    private String identificadorUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Verifica se o usuário já está logado
        verificarUsuarioLogado();

        email = (EditText) findViewById(R.id.id_email_login);
        senha = (EditText) findViewById(R.id.id_senha_login);
        botaoLogar = (ImageView) findViewById(R.id.id_bt_entrar);
        botaoFacebook = (ImageView) findViewById(R.id.id_bt_facebook);
        cadastre = (TextView) findViewById(R.id.id_nao_tem_conta);

        cadastre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCadastroUsuario();
            }
        });

        botaoLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuario = new Usuario();
                usuario.setEmail( email.getText().toString() );
                usuario.setSenha( senha.getText().toString() );
                validarLogin();
            }
        });

    }

    public void abrirCadastroUsuario(){

        Intent intent = new Intent(MainActivity.this, CadastroUsuarioActivity.class);
        startActivity( intent );

    }

    private void validarLogin(){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if( task.isSuccessful() ){


                    identificadorUsuarioLogado = Base64Custom.codificarBase64(usuario.getEmail());

                    firebase = ConfiguracaoFirebase.getFirebase()
                            .child("usuarios")
                            .child( identificadorUsuarioLogado );

                    valueEventListenerUsuario = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Usuario usuarioRecuperado = dataSnapshot.getValue( Usuario.class );

                            Preferencias preferencias = new Preferencias(MainActivity.this);
                            preferencias.salvarDados( identificadorUsuarioLogado, usuarioRecuperado.getNome() );

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };

                    firebase.addListenerForSingleValueEvent( valueEventListenerUsuario );



                    abrirTelaPrincipal();
                    Toast.makeText(MainActivity.this, "Sucesso ao fazer login!", Toast.LENGTH_LONG ).show();
                }else{
                    Toast.makeText(MainActivity.this, "Erro ao fazer login!", Toast.LENGTH_LONG ).show();
                }

            }
        });
    }

    private void abrirTelaPrincipal(){
        Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);
        startActivity( intent );
        finish();
    }

    private void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if( autenticacao.getCurrentUser() != null ){
            abrirTelaPrincipal();
        }
    }
}
