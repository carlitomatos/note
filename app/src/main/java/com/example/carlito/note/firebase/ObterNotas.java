package com.example.carlito.note.firebase;

/**
 * Created by carlito on 03/11/16.
 */

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.example.carlito.note.Anotacao;



public class ObterNotas {

    public void todos(final OnObterAnotacoesListener listener){

        //
        // Caminho da lista de notas
        //  /notas
        //
        DatabaseReference notasReferencia = FirebaseDatabase.getInstance().getReference()
                .child("notas");

        //
        // Configura o listener para recuperar os dados
        //
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Anotacao> list = new ArrayList<>();
                //
                // Obtem o iterable
                //
                Iterable<DataSnapshot> i = dataSnapshot.getChildren();
                //
                // Enquanto houver registros
                //
                while(i.iterator().hasNext()){

                    // Obtém o próximo registro
                    DataSnapshot d = i.iterator().next();
                    //
                    Anotacao p = new Anotacao();
                    //
                    // Obtem o valor da tag anotação
                    //
                    p.setValor(d.child("anotacao").getValue().toString());
                    //
                    // Obtem a chave do registro
                    //
                    p.setUid(d.getKey());

                    //
                    // Adiciona na lista
                    //
                    list.add(p);

                }

                // se o listener for diferente de nulo
                if(listener != null){
                    // Envia a lista
                    listener.onAnotacoesObtidas(list);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Se deu algo errado exibe um log =X
                Log.w("TAG", "deu erro ao buscar anotações do firebase", databaseError.toException());
                // ...
            }
        };

        //
        // Seta o listener no firebase database
        // Este listener funciona apenas uma vez, single event
        //
        notasReferencia.addListenerForSingleValueEvent(postListener);


    }

    public interface OnObterAnotacoesListener{

        void onAnotacoesObtidas(List<Anotacao> lista);

    }


}
