package com.example.carlito.note.firebase;

/**
 * Created by carlito on 03/11/16.
 */

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import com.example.carlito.note.Anotacao;


public class AtualizarAnotacao {
    public void atualizar(Anotacao anotacao){
        //
        // Caminho da lista de notas
        //  /notas/código-uui
        //
        DatabaseReference notasReferencia = FirebaseDatabase.getInstance().getReference()
                .child("notas").child(anotacao.getUid());
        //
        // Cria o hasmap, estrutura de chave e valor
        //
        Map<String,Object> map = new HashMap<>();
        map.put("anotacao",anotacao.getValor());
        //
        // Manda atualizar no firebase database
        //
        notasReferencia.updateChildren(map);
    }
}