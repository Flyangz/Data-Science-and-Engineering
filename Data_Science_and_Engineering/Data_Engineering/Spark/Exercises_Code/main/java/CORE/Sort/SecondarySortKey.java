package CORE.Sort;

import scala.Serializable;
import scala.math.Ordered;

public class SecondarySortKey implements Ordered<SecondarySortKey>, Serializable {

    private int first;
    private int second;

    //Ordered的方法
    @Override
    public int compare(SecondarySortKey that) {
        if (this.first - that.getFirst() != 0) return this.first - that.getFirst();
        else return this.second - that.getSecond();
    }

    @Override
    public boolean $less(SecondarySortKey that) {
        if (this.first < that.getFirst()) return true;
        else if (this.first == that.getFirst() && this.second < that.getSecond()) return true;
        return false;
    }

    @Override
    public boolean $less$eq(SecondarySortKey that) {
        if (this.$less(that)) return true;
        else if (this.first == that.getFirst() && this.second == that.getSecond()) return true;
        return false;
    }

    @Override
    public boolean $greater$eq(SecondarySortKey that) {
        if (this.$greater(that)) return true;
        else if (this.first == that.getFirst() && this.second == that.getSecond()) return true;
        return false;
    }

    @Override
    public int compareTo(SecondarySortKey that) {
        if (this.first - that.getFirst() != 0) return this.first - that.getFirst();
        else return this.second - that.getSecond();
    }

    @Override
    public boolean $greater(SecondarySortKey that) {
        if (this.first > that.getFirst()) return true;
        else if (this.first == that.getFirst() && this.second > that.getSecond()) return true;
        return false;
    }

    //类的基本方法

    public SecondarySortKey(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }
}
