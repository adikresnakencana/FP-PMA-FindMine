package com.planhub.findmine.Fragment;


import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.planhub.findmine.Adapter.PostAdapter;
import com.planhub.findmine.Model.Post;
import com.planhub.findmine.Model.User;
import com.planhub.findmine.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class HomeFragment extends Fragment {

    private RecyclerView rvPost;
    private List<Post> postList;
    private List<User> userList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private DocumentSnapshot lastVisibe;

    private PostAdapter postAdapter;

    private static final String TAG = "HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postList = new ArrayList<>();
        userList = new ArrayList<>();
        rvPost = view.findViewById(R.id.rvPost);

        mAuth = FirebaseAuth.getInstance();

        // mengatur recycler view
        postAdapter = new PostAdapter(postList, userList);
        rvPost.setLayoutManager(new LinearLayoutManager(container.getContext()));
        rvPost.setAdapter(postAdapter);
        rvPost.setHasFixedSize(true);


        if (mAuth.getCurrentUser() != null) {

            // menampilkan data pada tabel post di firebase firestore
            firebaseFirestore = FirebaseFirestore.getInstance();

            rvPost.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(-1);

                    if (reachedBottom) {

                        loadMoreQuery();

                    }
                }
            });

            Query query = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);

            query.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, final FirebaseFirestoreException e) {

                    lastVisibe = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                    // menampilkan jika hanya ada data di database
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            Post post = doc.getDocument().toObject(Post.class);
                            postList.add(post);

                            postAdapter.notifyDataSetChanged();

                        }

                    }

                }

            });

        }

        return view;

    }

    private void loadMoreQuery() {

        if (mAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisibe)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // menampilkan jika hanya ada data di database
                        lastVisibe = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                Post post = doc.getDocument().toObject(Post.class);
                                postList.add(post);

                                postAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }
            });
        }

    }
}