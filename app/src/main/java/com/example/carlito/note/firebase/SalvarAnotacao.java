package com.example.carlito.note.firebase;

/**
 * Created by carlito on 03/11/16.
 */

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class SalvarAnotacao {


    public void novoRegistro(String texto){

        //
        // Caminho da lista de notas
        //  /notas
        //
        DatabaseReference notasReferencia = FirebaseDatabase.getInstance().getReference()
                .child("notas");

        //
        // Método push gera um novo registro
        //
        DatabaseReference novoRegistro = notasReferencia.push();
        novoRegistro.child("anotacao").setValue(texto);

    }

}