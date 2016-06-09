/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuelibstringreplacer;

import java.util.Objects;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author uname
 */
public class csvBean implements csvBean_ReadOnly {

    public static final String[] HEADER = {"from","to"} ;
    
    public csvBean() {
    }
    /**
     * 各要素フォーマット定義
     */
    public static final CellProcessor[] processors = new CellProcessor[]{
        new Unique(), // 変換元の文字列。重複禁止
        null // 変換先の文字列。
    };
    /* 各要素の Getter/Setter 定義 */
    private String from, to;

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }


    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.from);
        hash = 31 * hash + Objects.hashCode(this.to);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final csvBean other = (csvBean) obj;
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        if (!Objects.equals(this.to, other.to)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "csvBean{" + "from=" + from + ", to=" + to + '}';
    }

}
