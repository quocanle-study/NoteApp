package com.quocanle.noteapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.quocanle.noteapp.databinding.ActivityMainBinding;
import com.quocanle.noteapp.databinding.AddNoteBinding;
import com.quocanle.noteapp.databinding.NoteItemsBinding;
import com.quocanle.noteapp.model.Post;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("posts");
//        firestore = FirebaseFirestore.getInstance();
//
        binding.rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNote();
            }
        });
    }

    protected void addNote() {

        AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
//        View mView = inflater.inflate(R.layout.add_note, null);
        AddNoteBinding bindingAddNote = AddNoteBinding.inflate(getLayoutInflater());
        mDialog.setView(bindingAddNote.getRoot());

        AlertDialog dialog = mDialog.create();
        dialog.setCancelable(true);

        bindingAddNote.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = myRef.push().getKey();
                String author = mAuth.getUid();
                String title = bindingAddNote.etTitle.getText().toString();
                String content = bindingAddNote.etContent.getText().toString();

                myRef.child(id).setValue(new Post(id, author, title, content, getRandomColor()) {

                        })
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Note added", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to add note", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    protected String getRandomColor() {
        Random random = new Random();
        class CColor {
            private String color;
            private String hex;
            public CColor(String color, String hex) {
                this.color = color;
                this.hex = hex;
            }

            public String getColor() {
                return color;
            }

            public void setColor(String color) {
                this.color = color;
            }

            public String getHex() {
                return hex;
            }

            public void setHex(String hex) {
                this.hex = hex;
            }
        }
        ArrayList<CColor> colors = new ArrayList<>();
        colors.add(new CColor("Red", "#FF0000"));
        colors.add(new CColor("Green", "#00FF00"));
        colors.add(new CColor("Blue", "#0000FF"));
        colors.add(new CColor("Yellow", "#FFFF00"));
        colors.add(new CColor("Cyan", "#00FFFF"));
        colors.add(new CColor("Magenta", "#FF00FF"));
        colors.add(new CColor("Orange", "#FFA500"));
        colors.add(new CColor("Purple", "#800080"));
        colors.add(new CColor("Pink", "#FFC0CB"));
        colors.add(new CColor("Brown", "#A52A2A"));
        colors.add(new CColor("White", "#FFFFFF"));
        colors.add(new CColor("Black", "#000000"));
        colors.add(new CColor("Gray", "#808080"));
        colors.add(new CColor("Silver", "#C0C0C0"));
        colors.add(new CColor("Gold", "#FFD700"));
        colors.add(new CColor("Bronze", "#CD7F32"));
        colors.add(new CColor("Copper", "#B87333"));
        colors.add(new CColor("Brass", "#B5A642"));
        return colors.get(random.nextInt(colors.size())).getHex();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(myRef, Post.class)
                .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Post, PostHolder>(options) {
            @Override
            public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                NoteItemsBinding binding = NoteItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new PostHolder(binding);
            }

            @Override
            protected void onBindViewHolder(PostHolder holder, int position, Post model) {
                holder.binding.tvTitle.setText(model.getTitle());
                holder.binding.tvContent.setText(model.getContent());
//                random color
//                holder.binding.layoutNote.setBackgroundColor(Color.argb(255, new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256)));

                holder.binding.layoutNote.setBackgroundColor(Color.parseColor(model.getColor()));

                holder.binding.layoutNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder mDialog = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                        AddNoteBinding bindingAddNote = AddNoteBinding.inflate(getLayoutInflater());
                        mDialog.setView(bindingAddNote.getRoot());

                        bindingAddNote.etTitle.setText(model.getTitle());
                        bindingAddNote.etContent.setText(model.getContent());

                        AlertDialog dialog = mDialog.create();
                        dialog.setCancelable(true);

                        bindingAddNote.btnSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String id = model.getId();
                                String author = mAuth.getUid();
                                String title = bindingAddNote.etTitle.getText().toString();
                                String content = bindingAddNote.etContent.getText().toString();

                                myRef.child(id).setValue(new Post(id, author, title, content, model.getColor()) {

                                })
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(MainActivity.this, "Note updated", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                });

                holder.binding.ivAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                AlertDialog.Builder mDialog = new AlertDialog.Builder(MainActivity.this);
                                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                                AddNoteBinding bindingAddNote = AddNoteBinding.inflate(getLayoutInflater());
                                mDialog.setView(bindingAddNote.getRoot());

                                bindingAddNote.etTitle.setText(model.getTitle());
                                bindingAddNote.etContent.setText(model.getContent());

                                AlertDialog dialog = mDialog.create();
                                dialog.setCancelable(true);

                                bindingAddNote.btnSave.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String id = model.getId();
                                        String author = mAuth.getUid();
                                        String title = bindingAddNote.etTitle.getText().toString();
                                        String content = bindingAddNote.etContent.getText().toString();

                                        myRef.child(id).setValue(new Post(id, author, title, content, model.getColor()) {

                                        })
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(MainActivity.this, "Note updated", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(MainActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();
                                return true;
                            }
                        });
                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                myRef.child(model.getId()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(MainActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                return true;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }

        };

        binding.rvNotes.setAdapter(adapter);
        adapter.startListening();

    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        NoteItemsBinding binding;
        public PostHolder(NoteItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    protected void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug", "signInWithEmail:success");
                        } else {
                                Log.w("Debug", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    protected void createNewUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug", "createUserWithEmail:success");
                        } else {
                            Log.w("Debug", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    protected void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug", "sendPasswordResetEmail:success");
                        } else {
                            Log.w("Debug", "sendPasswordResetEmail:failure", task.getException());
                        }
                    }
                });
    }

    protected void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Debug", "deleteAccount:success");
                        } else {
                            Log.w("Debug", "deleteAccount:failure", task.getException());
                        }
                    }
                });
    }

    protected void logout() {
        mAuth.signOut();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            mAuth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}