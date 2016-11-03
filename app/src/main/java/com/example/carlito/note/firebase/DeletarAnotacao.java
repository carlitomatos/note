package com.example.carlito.note.firebase;

/**
 * Created by carlito on 03/11/16.
 */

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.carlito.note.Anotacao;


public class DeletarAnotacao {


    public void deletar(Anotacao anotacao){

        //
        // Caminho da lista de notas
        //  /notas/código uui
        //
        DatabaseReference notasReferencia = FirebaseDatabase.getInstance().getReference()
                .child("notas").child(anotacao.getUid());

        //
        // Deleta do firebase setando o valor para nulo
        //
        notasReferencia.setValue(null);

    }

}