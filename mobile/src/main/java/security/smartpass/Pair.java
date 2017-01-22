package security.smartpass;

/**
 * Created by Ryan on 12/9/16.
 */

public class Pair implements Comparable<Pair>
{
    final int index;
    final char letter;

    public Pair(int aIndex, char aLetter)
    {
        index   = aIndex;
        letter = aLetter;
    }

    @Override
    public int compareTo( final Pair other) {
        return Integer.compare(this.index, other.index);
    }
}

