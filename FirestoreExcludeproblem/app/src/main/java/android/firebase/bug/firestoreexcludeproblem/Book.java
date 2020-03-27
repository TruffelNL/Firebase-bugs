package android.firebase.bug.firestoreexcludeproblem;

import com.google.firebase.firestore.Exclude;

public class Book
{
    private String title;
    private int pages;

    public Book()
    {

    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Exclude
    public int getPages()
    {
        return pages;
    }

    public void setPages(int pages)
    {
        this.pages = pages;
    }
}
