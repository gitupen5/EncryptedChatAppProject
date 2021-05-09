package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.chatify.Adapters.MessageAdapter;
import com.example.chatify.Adapters.UserAdapter;
import com.example.chatify.Model.Chat;
import com.example.chatify.Model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MessageActivity extends AppCompatActivity {

    TextView username;
    ImageView imageView;

    //Sending Message initialisation.
    RecyclerView recyclerView;
    EditText msg_edit_text;
    ImageButton sendBtn;


    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerViewMsg;

    String userid;

    //Encryption Initialisation.
    /*private byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec = new SecretKeySpec(encryptionKey, "AES");*/


    //New Initialization of AES:
    private static SecretKeySpec secretKey;
    private static byte[] key;

    //new AES
    public static void setKey(String myKey)
    {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //New Enc
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encrypt(String strToEncrypt, String secret)
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey); //initializing the encryption.
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    //New Dec

    public static String decrypt(String strToDecrypt, String secret)
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        imageView = findViewById(R.id.imageView_profile);
        username = findViewById(R.id.username);

        //Message and send btn init.
        sendBtn = findViewById(R.id.btn_send);
        msg_edit_text = findViewById(R.id.text_send);


        //RecyclerView chat activity.
        recyclerViewMsg = findViewById(R.id.recycler_view);
        recyclerViewMsg.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMsg.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        userid = intent.getStringExtra("userid");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid);


        //Encryption:
        /*try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");*/

        //New


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                username.setText(user.getUsername());

                if(user.getImageURL().equals("default")){
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(imageView);
                }

                ReadMessage(fuser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Sending Message.
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            final String secretKey = "ssshhhhhhhhhhh!!!!";
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                //encrypting messages:
                String msg = encrypt(msg_edit_text.getText().toString(), secretKey);

                if(!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg);
                }
                else{
                    Toast.makeText(MessageActivity.this, "Please write a message!", Toast.LENGTH_SHORT).show();
                }
                msg_edit_text.setText("");
            }
        });
    }

    //Encryption:
    /*private String AESEncryptionMethod(String string){

        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec); //initializing encryption mode.
            encryptedByte = cipher.doFinal(stringByte); //encrypting the bytes.
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }



        String returnString = null;

        *//*try {
            returnString = new String(encryptedByte, "ISO-8859-1"); //converting those encrypted bytes into string.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*//*
        returnString = new String(encryptedByte, StandardCharsets.UTF_8);
        return returnString;

    }*/



    //Decryption:
    /*public String AESDecryptionMethod(String string)  {
        //byte[] EncryptedByte = string.getBytes("ISO-8859-1");
        byte[] EncryptedByte = string.getBytes(StandardCharsets.UTF_8);
        String decryptedString = string;

        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return decryptedString;
    }
*/

    //Message to be send on database.
    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

      //  final String secretKey = "ssshhhhhhhhhhh!!!!";

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);


        //AESDecryptionMethod(message);

        reference.child("Chats").push().setValue(hashMap);



        //Adding user to chat fragment, Latest Chat with contact.
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid()).child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void ReadMessage(final String myid, final String userid, final String imageurl){

        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                try {
                    for(DataSnapshot snapshot2 : snapshot.getChildren()){
                        Chat chat = snapshot2.getValue(Chat.class);

                        if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid)
                                || chat.getReceiver().equals(userid) && chat.getSender().equals(myid))
                        {
                            mChat.add(chat);
                        }

                        messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
                        recyclerViewMsg.setAdapter(messageAdapter);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Function to check weather user is offline or online.
    private void CheckStatus(String status){
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        CheckStatus("Offline");
    }
    private void setSupportActionBar(Toolbar toolbar) {

    }
}