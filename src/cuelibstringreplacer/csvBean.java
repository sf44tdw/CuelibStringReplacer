/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuelibstringreplacer;

import java.beans.*;
import java.io.Serializable;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.StrMinMax;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author uname
 */
public class csvBean implements Serializable {

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";
    private String sampleProperty;
    private PropertyChangeSupport propertySupport;

    public csvBean() {
        propertySupport = new PropertyChangeSupport(this);
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

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
