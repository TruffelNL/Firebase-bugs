package android.firebase.bug.firestoreexcludeproblem;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{

    private FirebaseFirestore fireStore;

    private HashMap<String, Book> booksData = new HashMap<>();

    private ArrayAdapter<String> adapterBooksData;

    String id = null;

    //int used for generating book titles
    private int bookNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Book newBook = new Book();
                newBook.setTitle("Book " + bookNumber);
                newBook.setPages(50);
                bookNumber++;
                addBook(newBook);
            }
        });

        fireStore = FirebaseFirestore.getInstance();

        adapterBooksData = new ArrayAdapter<String>(this, R.layout.bookslistviewitem, new ArrayList<String>());
        updateListViewArray();
        ((ListView)findViewById(R.id.booksListview)).setAdapter(adapterBooksData);
        ((ListView)findViewById(R.id.booksListview)).setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                booksData.forEach((key, value) -> {
                    if (value.equals(adapterBooksData.getItem(i))) {
                        id = key;
                    }
                });
                removeBook(id);
            }
        });

        //uncomment to add more testdata
        //addTestData();

        registerFirestoreListener();
    }

    public void registerFirestoreListener()
    {
        fireStore.collection("items").document("user1")
                .collection("books")
                .addSnapshotListener(new EventListener<QuerySnapshot>()
                {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e)
                    {
                        for (DocumentChange documentChange : querySnapshot.getDocumentChanges())
                        {
                            boolean hasPendingWrites = documentChange.getDocument().getMetadata().hasPendingWrites();
                            Book bookChange = documentChange.getDocument().toObject(Book.class);
                            switch(documentChange.getType())
                            {
                                case ADDED:
                                    booksData.put(documentChange.getDocument().getId(), bookChange);
                                    Snackbar.make(MainActivity.this.findViewById(R.id.contentLayout), "Book modified. Title: " + bookChange.getTitle() + " Pages: " + bookChange.getPages(),
                                            Snackbar.LENGTH_LONG).setDuration(1500).show();
                                    break;
                                case MODIFIED:
                                    booksData.replace(documentChange.getDocument().getId(), bookChange);
                                    Snackbar.make(MainActivity.this.findViewById(R.id.contentLayout), "Book modified. Title: " + bookChange.getTitle() + " Pages: " + bookChange.getPages(),
                                            Snackbar.LENGTH_LONG).setDuration(1500).show();
                                    break;
                                case REMOVED:
                                    booksData.remove(documentChange.getDocument().getId());
                                    break;
                            }
                        }

                        updateListViewArray();

                    }
                });
    }

    public void removeBook(String bookId)
    {
        fireStore.collection("items").document("user1").collection("books")
                .document(bookId).delete();

    }

    public void addBook(Book book)
    {
        fireStore.collection("items").document("user1").collection("books").add(book);
    }

    public void addTestData()
    {
        while(bookNumber < 6)
        {
            Book newBook = new Book();
            newBook.setTitle("Book " + bookNumber);
            addBook(newBook);
            bookNumber++;
        }
    }

    public void updateListViewArray()
    {
        adapterBooksData.clear();
        for(Map.Entry<String, Book> bookEntry : booksData.entrySet())
        {
            adapterBooksData.add(bookEntry.getValue().getTitle());
        }

        adapterBooksData.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
